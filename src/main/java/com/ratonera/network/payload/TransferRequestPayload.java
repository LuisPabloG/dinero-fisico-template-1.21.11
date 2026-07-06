package com.ratonera.network.payload;

import com.ratonera.RatoneraMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TransferRequestPayload(String targetName, int amount) implements CustomPacketPayload {
	public static final Type<TransferRequestPayload> TYPE = new Type<>(RatoneraMod.id("transfer_request"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TransferRequestPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		TransferRequestPayload::targetName,
		ByteBufCodecs.INT,
		TransferRequestPayload::amount,
		TransferRequestPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}