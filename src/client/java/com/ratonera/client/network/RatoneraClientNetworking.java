package com.ratonera.client.network;

import com.ratonera.client.screen.TransferConfirmationScreen;
import com.ratonera.network.payload.TransferPromptPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class RatoneraClientNetworking {
	private static boolean registered;

	private RatoneraClientNetworking() {
	}

	public static void register() {
		if (registered) {
			return;
		}

		ClientPlayNetworking.registerGlobalReceiver(TransferPromptPayload.TYPE, (payload, context) ->
			context.client().execute(() -> context.client().setScreen(
				new TransferConfirmationScreen(context.client().screen, payload.requestId(), payload.senderName(), payload.amount())
			))
		);

		registered = true;
	}
}