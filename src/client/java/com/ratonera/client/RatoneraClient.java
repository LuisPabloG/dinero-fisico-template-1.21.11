package com.ratonera.client;

import com.ratonera.client.network.RatoneraClientNetworking;
import com.ratonera.client.screen.ShopScreen;
import com.ratonera.registry.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class RatoneraClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		RatoneraClientNetworking.register();
		MenuScreens.register(ModMenuTypes.SHOP, ShopScreen::new);
	}
}