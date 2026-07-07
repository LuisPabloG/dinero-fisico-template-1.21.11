package com.ratonera.registry;

import com.ratonera.RatoneraMod;
import com.ratonera.block.ShopBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
	public static final BlockEntityType<ShopBlockEntity> SHOP = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		RatoneraMod.id("shop"),
		FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, ModBlocks.SHOP_BLOCK).build()
	);

	private ModBlockEntities() {
	}

	public static void register() {
		RatoneraMod.LOGGER.info("Block entities de tienda registrados.");
	}
}
