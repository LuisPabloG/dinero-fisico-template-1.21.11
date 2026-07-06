package com.ratonera.client;

import com.ratonera.client.network.RatoneraClientNetworking;
import net.fabricmc.api.ClientModInitializer;

public class RatoneraClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		RatoneraClientNetworking.register();
	}
}