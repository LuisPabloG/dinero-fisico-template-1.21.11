package com.ratonera.network.payload;

import com.ratonera.RatoneraMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TransferPromptPayload(String requestId, String senderName, int amount) implements CustomPacketPayload {
	public static final Type<TransferPromptPayload> TYPE = new Type<>(RatoneraMod.id("transfer_prompt"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TransferPromptPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		TransferPromptPayload::requestId,
		ByteBufCodecs.STRING_UTF8,
		TransferPromptPayload::senderName,
		ByteBufCodecs.INT,
		TransferPromptPayload::amount,
		TransferPromptPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}