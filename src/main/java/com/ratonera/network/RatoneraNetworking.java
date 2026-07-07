package com.ratonera.network;

import com.ratonera.block.ShopBlockEntity;
import com.ratonera.network.payload.ShopBuyPayload;
import com.ratonera.network.payload.TransferDecisionPayload;
import com.ratonera.network.payload.TransferPromptPayload;
import com.ratonera.network.payload.TransferRequestPayload;
import com.ratonera.transfer.TransferManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class RatoneraNetworking {
	private static boolean commonRegistered;

	private RatoneraNetworking() {
	}

	public static void registerCommon() {
		if (commonRegistered) {
			return;
		}

		PayloadTypeRegistry.playC2S().register(TransferRequestPayload.TYPE, TransferRequestPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(TransferDecisionPayload.TYPE, TransferDecisionPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ShopBuyPayload.TYPE, ShopBuyPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(TransferPromptPayload.TYPE, TransferPromptPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(TransferRequestPayload.TYPE, (payload, context) ->
			TransferManager.handleTransferRequest(context.player(), payload)
		);
		ServerPlayNetworking.registerGlobalReceiver(TransferDecisionPayload.TYPE, (payload, context) ->
			TransferManager.handleTransferDecision(context.player(), payload)
		);
		ServerPlayNetworking.registerGlobalReceiver(ShopBuyPayload.TYPE, (payload, context) -> {
			var player = context.player();
			player.level().getServer().execute(() -> {
				if (player.level().getBlockEntity(payload.pos()) instanceof ShopBlockEntity shop) {
					shop.buyItem(player);
				}
			});
		});

		commonRegistered = true;
	}
}