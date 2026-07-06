package com.ratonera.registry;

import com.ratonera.RatoneraMod;
import com.ratonera.item.BilleteItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItems {
	public static final BilleteItem BILLETE_1 = register("billete_1", 1);
	public static final BilleteItem BILLETE_5 = register("billete_5", 5);
	public static final BilleteItem BILLETE_10 = register("billete_10", 10);
	public static final BilleteItem BILLETE_20 = register("billete_20", 20);
	public static final BilleteItem BILLETE_50 = register("billete_50", 50);
	public static final BilleteItem BILLETE_100 = register("billete_100", 100);
	public static final BilleteItem BILLETE_200 = register("billete_200", 200);

	private ModItems() {
	}

	public static void register() {
		RatoneraMod.LOGGER.info("Billetes de quetzales registrados.");
	}

	public static int getValor(Item item) {
		return item instanceof BilleteItem billete ? billete.getValorQuetzales() : 0;
	}

	private static BilleteItem register(String path, int valorQuetzales) {
		Identifier itemId = RatoneraMod.id(path);
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, itemId);
		BilleteItem item = new BilleteItem(valorQuetzales, new Item.Properties().stacksTo(64).setId(itemKey));

		return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
	}
}