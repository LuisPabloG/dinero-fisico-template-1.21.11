package com.ratonera.network.payload;

import com.ratonera.RatoneraMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ShopBuyPayload(BlockPos pos) implements CustomPacketPayload {
	public static final Type<ShopBuyPayload> TYPE = new Type<>(RatoneraMod.id("shop_buy"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ShopBuyPayload> CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		ShopBuyPayload::pos,
		ShopBuyPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
