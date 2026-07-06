package com.ratonera;

import com.ratonera.network.RatoneraNetworking;
import com.ratonera.registry.ModItems;
import com.ratonera.transfer.TransferManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RatoneraMod implements ModInitializer {
	public static final String MOD_ID = "ratonera";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.register();
		RatoneraNetworking.registerCommon();
		TransferManager.initialize();
		LOGGER.info("Ratonera inicio correctamente.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}