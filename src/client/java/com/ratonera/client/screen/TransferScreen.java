package com.ratonera.client.screen;

import com.ratonera.network.payload.TransferRequestPayload;
import com.ratonera.util.QuetzalInventoryHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TransferScreen extends Screen {
	private final Screen parentScreen;
	private EditBox targetNameBox;
	private EditBox amountBox;
	private int formLeft;
	private int formTop;
	private int formWidth;

	public TransferScreen(Screen parentScreen) {
		super(Component.translatable("screen.ratonera.transferir_titulo"));
		this.parentScreen = parentScreen;
	}

	@Override
	protected void init() {
		this.formWidth = 200;
		this.formLeft = (this.width - this.formWidth) / 2;
		this.formTop = this.height / 2 - 55;

		this.targetNameBox = new EditBox(this.font, this.formLeft, this.formTop + 20, this.formWidth, 20, Component.translatable("screen.ratonera.destino"));
		this.targetNameBox.setMaxLength(16);
		this.addRenderableWidget(this.targetNameBox);

		this.amountBox = new EditBox(this.font, this.formLeft, this.formTop + 60, this.formWidth, 20, Component.translatable("screen.ratonera.cantidad"));
		this.amountBox.setMaxLength(9);
		this.addRenderableWidget(this.amountBox);

		this.addRenderableWidget(
			Button.builder(Component.translatable("screen.ratonera.enviar"), button -> this.submitTransfer())
				.bounds(this.formLeft, this.formTop + 100, 96, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(Component.translatable("gui.cancel"), button -> this.onClose())
				.bounds(this.formLeft + 104, this.formTop + 100, 96, 20)
				.build()
		);

		this.setInitialFocus(this.targetNameBox);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		guiGraphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
		guiGraphics.fill(this.formLeft - 14, this.formTop - 26, this.formLeft + this.formWidth + 14, this.formTop + 132, 0x88000000);
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int centerX = this.width / 2;
		int totalQuetzales = this.minecraft != null && this.minecraft.player != null
			? QuetzalInventoryHelper.contarQuetzales(this.minecraft.player.getInventory())
			: 0;

		guiGraphics.drawCenteredString(this.font, this.title, centerX, this.formTop - 18, 0xFFFFFF);
		guiGraphics.drawCenteredString(this.font, Component.translatable("screen.ratonera.total_quetzales", totalQuetzales), centerX, this.formTop - 4, 0xD6F0D6);
		guiGraphics.drawString(this.font, Component.translatable("screen.ratonera.destino"), this.formLeft, this.formTop + 8, 0xFFFFFF, false);
		guiGraphics.drawString(this.font, Component.translatable("screen.ratonera.cantidad"), this.formLeft, this.formTop + 48, 0xFFFFFF, false);

		if (this.targetNameBox != null && this.targetNameBox.getValue().isEmpty()) {
			guiGraphics.drawString(this.font, Component.translatable("screen.ratonera.destino_hint"), this.formLeft + 5, this.formTop + 26, 0x7A7A7A, false);
		}

		if (this.amountBox != null && this.amountBox.getValue().isEmpty()) {
			guiGraphics.drawString(this.font, Component.translatable("screen.ratonera.cantidad_hint"), this.formLeft + 5, this.formTop + 66, 0x7A7A7A, false);
		}
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parentScreen);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void submitTransfer() {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.player == null) {
			return;
		}

		String targetName = this.targetNameBox.getValue().trim();
		String amountText = this.amountBox.getValue().trim();

		if (targetName.isEmpty()) {
			minecraft.player.displayClientMessage(Component.translatable("message.ratonera.error_destino_vacio"), false);
			return;
		}

		int amount;

		try {
			amount = Integer.parseInt(amountText);
		} catch (NumberFormatException exception) {
			minecraft.player.displayClientMessage(Component.translatable("message.ratonera.error_cantidad_invalida"), false);
			return;
		}

		if (amount <= 0) {
			minecraft.player.displayClientMessage(Component.translatable("message.ratonera.error_cantidad_invalida"), false);
			return;
		}

		ClientPlayNetworking.send(new TransferRequestPayload(targetName, amount));
		this.onClose();
	}
}