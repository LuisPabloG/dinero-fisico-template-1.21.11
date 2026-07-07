package com.ratonera.registry;

import com.ratonera.RatoneraMod;
import com.ratonera.screen.ShopScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
	public static final MenuType<ShopScreenHandler> SHOP = register("shop", ShopScreenHandler::new);

	private ModMenuTypes() {
	}

	private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> MenuType<T> register(
		String path,
		MenuType.MenuSupplier<T> factory
	) {
		return Registry.register(BuiltInRegistries.MENU, RatoneraMod.id(path), new MenuType<>(factory, FeatureFlagSet.of()));
	}

	public static void register() {
		RatoneraMod.LOGGER.info("Menu types de tienda registrados.");
	}
}
