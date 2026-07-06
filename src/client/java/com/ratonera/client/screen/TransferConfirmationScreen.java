package com.ratonera.client.screen;

import com.ratonera.network.payload.TransferDecisionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TransferConfirmationScreen extends Screen {
	private final Screen parentScreen;
	private final String requestId;
	private final String senderName;
	private final int amount;
	private boolean responded;

	public TransferConfirmationScreen(Screen parentScreen, String requestId, String senderName, int amount) {
		super(Component.translatable("screen.ratonera.confirmacion_titulo"));
		this.parentScreen = parentScreen;
		this.requestId = requestId;
		this.senderName = senderName;
		this.amount = amount;
	}

	@Override
	protected void init() {
		int left = this.width / 2 - 100;
		int top = this.height / 2 - 30;

		this.addRenderableWidget(
			Button.builder(Component.translatable("screen.ratonera.aceptar"), button -> this.sendDecision(true))
				.bounds(left, top + 40, 96, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(Component.translatable("screen.ratonera.rechazar"), button -> this.sendDecision(false))
				.bounds(left + 104, top + 40, 96, 20)
				.build()
		);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		guiGraphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int centerX = this.width / 2;
		int top = this.height / 2 - 42;

		guiGraphics.drawCenteredString(this.font, this.title, centerX, top, 0xFFFFFF);
		guiGraphics.drawCenteredString(this.font, Component.translatable("screen.ratonera.confirmacion_desde", this.senderName), centerX, top + 18, 0xFFFFFF);
		guiGraphics.drawCenteredString(this.font, Component.translatable("screen.ratonera.confirmacion_monto", this.amount), centerX, top + 32, 0xD6F0D6);
	}

	@Override
	public void onClose() {
		if (!this.responded) {
			this.responded = true;
			ClientPlayNetworking.send(new TransferDecisionPayload(this.requestId, false));
		}

		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parentScreen);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void sendDecision(boolean accepted) {
		this.responded = true;
		ClientPlayNetworking.send(new TransferDecisionPayload(this.requestId, accepted));
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parentScreen);
		}
	}
}