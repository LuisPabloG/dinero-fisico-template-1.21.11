package com.ratonera.client.screen;

import com.ratonera.network.payload.ShopBuyPayload;
import com.ratonera.screen.ShopScreenHandler;
import com.ratonera.util.QuetzalInventoryHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ShopScreen extends AbstractContainerScreen<ShopScreenHandler> {
	private Button buyButton;

	private static final int SLOT_X = 80;
	private static final int SLOT_Y = 20;
	private static final int SLOT_SIZE = 16;

	public ShopScreen(ShopScreenHandler handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = 166;
		this.inventoryLabelY = 9999;
	}

	@Override
	protected void init() {
		super.init();
		this.buyButton = this.addRenderableWidget(
			Button.builder(Component.translatable("screen.ratonera.shop_comprar"), button -> this.purchase())
				.bounds(this.leftPos + 44, this.topPos + 48, 88, 20)
				.build()
		);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		int x = this.leftPos;
		int y = this.topPos;

		guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);

		guiGraphics.fill(x, y, x + this.imageWidth, y + 14, 0xFF555555);
		int titleWidth = this.font.width(this.title);
		guiGraphics.drawString(this.font, this.title, x + (this.imageWidth - titleWidth) / 2, y + 3, 0xFFFFFFFF, false);

		int slotBgX = x + SLOT_X - 1;
		int slotBgY = y + SLOT_Y - 1;
		guiGraphics.fill(slotBgX, slotBgY, slotBgX + SLOT_SIZE + 2, slotBgY + SLOT_SIZE + 2, 0xFF8B8B8B);
		guiGraphics.fill(slotBgX + 1, slotBgY + 1, slotBgX + SLOT_SIZE + 1, slotBgY + SLOT_SIZE + 1, 0xFF373737);

		int price = this.menu.getPrice();
		Component priceText = Component.translatable("screen.ratonera.shop_precio", price);
		guiGraphics.drawString(this.font, priceText, x + 10, y + 42, 0xFF000000, true);

		guiGraphics.fill(x, y + 80, x + this.imageWidth, y + 81, 0xFFA0A0A0);

		guiGraphics.drawString(this.font, Component.translatable("screen.ratonera.shop_inventario"), x + 8, y + 84, 0xFF404040, true);

		if (this.minecraft != null && this.minecraft.player != null) {
			int total = QuetzalInventoryHelper.contarQuetzales(this.minecraft.player.getInventory());
			Component totalText = Component.translatable("screen.ratonera.total_quetzales", total);
			guiGraphics.drawString(this.font, totalText, x + 10, y + 68, 0xFF555555, true);
		}
	}

	@Override
	protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		super.renderTooltip(guiGraphics, mouseX, mouseY);

		if (this.buyButton != null && this.buyButton.isMouseOver(mouseX, mouseY)) {
			if (this.menu.getPrice() <= 0) {
				guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("screen.ratonera.shop_sin_precio"), mouseX, mouseY);
			}
		}

		int slotScreenX = this.leftPos + SLOT_X;
		int slotScreenY = this.topPos + SLOT_Y;
		if (mouseX >= slotScreenX && mouseX < slotScreenX + SLOT_SIZE && mouseY >= slotScreenY && mouseY < slotScreenY + SLOT_SIZE) {
			guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("screen.ratonera.shop_producto"), mouseX, mouseY);
		}
	}

	private void purchase() {
		ClientPlayNetworking.send(new ShopBuyPayload(this.menu.getShopPos()));
	}
}
