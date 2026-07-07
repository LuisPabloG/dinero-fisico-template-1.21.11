package com.ratonera.registry;

import com.ratonera.RatoneraMod;
import com.ratonera.block.ShopBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
	public static final Block SHOP_BLOCK = register("shop_block",
		BlockBehaviour.Properties.of()
			.strength(2.0f)
			.requiresCorrectToolForDrops()
	);

	private ModBlocks() {
	}

	public static void register() {
		RatoneraMod.LOGGER.info("Bloques de tienda registrados.");
	}

	private static Block register(String path, BlockBehaviour.Properties properties) {
		var id = RatoneraMod.id(path);
		var blockKey = ResourceKey.create(Registries.BLOCK, id);
		var itemKey = ResourceKey.create(Registries.ITEM, id);

		var block = new ShopBlock(properties.setId(blockKey));
		var registered = Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
		Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(registered, new Item.Properties().setId(itemKey)));
		return registered;
	}
}
