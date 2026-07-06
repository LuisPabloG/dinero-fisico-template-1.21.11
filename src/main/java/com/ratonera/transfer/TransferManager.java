package com.ratonera.transfer;

import com.ratonera.network.payload.TransferDecisionPayload;
import com.ratonera.network.payload.TransferPromptPayload;
import com.ratonera.network.payload.TransferRequestPayload;
import com.ratonera.util.QuetzalInventoryHelper;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class TransferManager {
	private static final Map<UUID, PendingTransfer> PENDING_BY_ID = new HashMap<>();
	private static final Map<UUID, UUID> PENDING_BY_SENDER = new HashMap<>();
	private static final Map<UUID, UUID> PENDING_BY_RECIPIENT = new HashMap<>();
	private static boolean initialized;

	private TransferManager() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> handleDisconnect(handler.player));
		initialized = true;
	}

	public static void handleTransferRequest(ServerPlayer sender, TransferRequestPayload payload) {
		MinecraftServer server = sender.level().getServer();

		if (server == null) {
			return;
		}

		String targetName = payload.targetName().trim();
		int amount = payload.amount();

		if (targetName.isEmpty()) {
			notify(sender, Component.translatable("message.ratonera.error_destino_vacio"));
			return;
		}

		if (amount <= 0) {
			notify(sender, Component.translatable("message.ratonera.error_cantidad_invalida"));
			return;
		}

		if (PENDING_BY_SENDER.containsKey(sender.getUUID())) {
			notify(sender, Component.translatable("message.ratonera.error_transferencia_saliente_pendiente"));
			return;
		}

		ServerPlayer recipient = findOnlinePlayer(server, targetName);

		if (recipient == null) {
			notify(sender, Component.translatable("message.ratonera.error_jugador_no_encontrado", targetName));
			return;
		}

		if (recipient.getUUID().equals(sender.getUUID())) {
			notify(sender, Component.translatable("message.ratonera.error_destino_propio"));
			return;
		}

		if (PENDING_BY_RECIPIENT.containsKey(recipient.getUUID())) {
			notify(sender, Component.translatable("message.ratonera.error_destinatario_ocupado", recipient.getScoreboardName()));
			return;
		}

		int totalDisponible = QuetzalInventoryHelper.contarQuetzales(sender.getInventory());

		if (totalDisponible < amount) {
			notify(sender, Component.translatable("message.ratonera.error_fondos_insuficientes", amount, totalDisponible));
			return;
		}

		if (!QuetzalInventoryHelper.retirarQuetzales(sender.getInventory(), amount)) {
			notify(sender, Component.translatable("message.ratonera.error_billetes_exactos", amount));
			return;
		}

		syncInventory(sender);

		UUID requestId = UUID.randomUUID();
		PendingTransfer pending = new PendingTransfer(
			requestId,
			sender.getUUID(),
			recipient.getUUID(),
			sender.getScoreboardName(),
			recipient.getScoreboardName(),
			amount
		);

		PENDING_BY_ID.put(requestId, pending);
		PENDING_BY_SENDER.put(sender.getUUID(), requestId);
		PENDING_BY_RECIPIENT.put(recipient.getUUID(), requestId);

		ServerPlayNetworking.send(recipient, new TransferPromptPayload(requestId.toString(), pending.senderName(), amount));
		notify(sender, Component.translatable("message.ratonera.transferencia_solicitada", amount, recipient.getScoreboardName()));
		notify(recipient, Component.translatable("message.ratonera.transferencia_recibida", pending.senderName(), amount));
	}

	public static void handleTransferDecision(ServerPlayer recipient, TransferDecisionPayload payload) {
		MinecraftServer server = recipient.level().getServer();

		if (server == null) {
			return;
		}

		UUID requestId;

		try {
			requestId = UUID.fromString(payload.requestId());
		} catch (IllegalArgumentException exception) {
			notify(recipient, Component.translatable("message.ratonera.error_solicitud_invalida"));
			return;
		}

		PendingTransfer pending = PENDING_BY_ID.get(requestId);

		if (pending == null || !pending.recipientId().equals(recipient.getUUID())) {
			notify(recipient, Component.translatable("message.ratonera.error_solicitud_expirada"));
			return;
		}

		removePending(requestId);

		ServerPlayer sender = server.getPlayerList().getPlayer(pending.senderId());

		if (payload.accepted()) {
			int droppedAmount = QuetzalInventoryHelper.depositarQuetzales(recipient, pending.amount());
			syncInventory(recipient);
			notify(recipient, Component.translatable("message.ratonera.transferencia_aceptada_receptor", pending.amount(), pending.senderName()));

			if (droppedAmount > 0) {
				notify(recipient, Component.translatable("message.ratonera.transferencia_sobrante_suelo", droppedAmount));
			}

			if (sender != null) {
				notify(sender, Component.translatable("message.ratonera.transferencia_aceptada_emisor", recipient.getScoreboardName(), pending.amount()));
			}
			return;
		}

		notify(recipient, Component.translatable("message.ratonera.transferencia_rechazada_receptor", pending.senderName(), pending.amount()));

		if (sender != null) {
			int droppedAmount = QuetzalInventoryHelper.depositarQuetzales(sender, pending.amount());
			syncInventory(sender);
			notify(sender, Component.translatable("message.ratonera.transferencia_rechazada_emisor", recipient.getScoreboardName(), pending.amount()));

			if (droppedAmount > 0) {
				notify(sender, Component.translatable("message.ratonera.transferencia_sobrante_suelo", droppedAmount));
			}
		}
	}

	private static void handleDisconnect(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();

		if (server == null) {
			return;
		}

		UUID outgoingId = PENDING_BY_SENDER.get(player.getUUID());

		if (outgoingId != null) {
			PendingTransfer pending = removePending(outgoingId);

			if (pending != null) {
				int droppedAmount = QuetzalInventoryHelper.depositarQuetzales(player, pending.amount());
				syncInventory(player);

				if (droppedAmount > 0) {
					notify(player, Component.translatable("message.ratonera.transferencia_sobrante_suelo", droppedAmount));
				}

				ServerPlayer recipient = server.getPlayerList().getPlayer(pending.recipientId());

				if (recipient != null) {
					notify(recipient, Component.translatable("message.ratonera.transferencia_cancelada_emisor", pending.senderName(), pending.amount()));
				}
			}
		}

		UUID incomingId = PENDING_BY_RECIPIENT.get(player.getUUID());

		if (incomingId != null) {
			PendingTransfer pending = removePending(incomingId);

			if (pending != null) {
				ServerPlayer sender = server.getPlayerList().getPlayer(pending.senderId());

				if (sender != null) {
					int droppedAmount = QuetzalInventoryHelper.depositarQuetzales(sender, pending.amount());
					syncInventory(sender);
					notify(sender, Component.translatable("message.ratonera.transferencia_cancelada_destinatario", pending.recipientName(), pending.amount()));

					if (droppedAmount > 0) {
						notify(sender, Component.translatable("message.ratonera.transferencia_sobrante_suelo", droppedAmount));
					}
				}
			}
		}
	}

	private static PendingTransfer removePending(UUID requestId) {
		PendingTransfer pending = PENDING_BY_ID.remove(requestId);

		if (pending != null) {
			PENDING_BY_SENDER.remove(pending.senderId());
			PENDING_BY_RECIPIENT.remove(pending.recipientId());
		}

		return pending;
	}

	private static ServerPlayer findOnlinePlayer(MinecraftServer server, String name) {
		String buscado = name.toLowerCase(Locale.ROOT);

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.getScoreboardName().toLowerCase(Locale.ROOT).equals(buscado)) {
				return player;
			}
		}

		return null;
	}

	private static void notify(ServerPlayer player, Component message) {
		player.sendSystemMessage(message);
	}

	private static void syncInventory(ServerPlayer player) {
		player.getInventory().setChanged();
		player.containerMenu.broadcastChanges();
	}
}