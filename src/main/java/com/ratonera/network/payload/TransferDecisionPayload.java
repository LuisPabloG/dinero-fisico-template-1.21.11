package com.ratonera.network.payload;

import com.ratonera.RatoneraMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TransferDecisionPayload(String requestId, boolean accepted) implements CustomPacketPayload {
	public static final Type<TransferDecisionPayload> TYPE = new Type<>(RatoneraMod.id("transfer_decision"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TransferDecisionPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		TransferDecisionPayload::requestId,
		ByteBufCodecs.BOOL,
		TransferDecisionPayload::accepted,
		TransferDecisionPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}