package com.ratonera.client.screen;

import com.ratonera.network.payload.ShopBuyPayload;
import com.ratonera.registry.ModMenuTypes;
import com.ratonera.screen.ShopScreenHandler;
import com.ratonera.util.QuetzalInventoryHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ShopScreen extends AbstractContainerScreen<ShopScreenHandler> {
	private static final Identifier HOPPER_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/hopper.png");
	private static final int HOPPER_HEIGHT = 133;

	private Button buyButton;

	public ShopScreen(ShopScreenHandler handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
		this.imageWidth = 176;
		this.imageHeight = 166;
		this.inventoryLabelY = this.imageHeight - 94;
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
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		int x = this.leftPos;
		int y = this.topPos;
		int h = Math.min(this.imageHeight, HOPPER_HEIGHT);

		guiGraphics.blit(HOPPER_TEXTURE, x, y, this.imageWidth, h, 0.0F, 0.0F, this.imageWidth, h);

		if (this.imageHeight > HOPPER_HEIGHT) {
			guiGraphics.fill(x, y + HOPPER_HEIGHT, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
		guiGraphics.drawString(this.font, Component.translatable("screen.ratonera.shop_inventario"), this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

		int price = this.menu.getPrice();
		Component priceText = Component.translatable("screen.ratonera.shop_precio", price);
		guiGraphics.drawString(this.font, priceText, 44, 38, 0x3F3F3F, false);

		if (this.minecraft != null && this.minecraft.player != null) {
			int total = QuetzalInventoryHelper.contarQuetzales(this.minecraft.player.getInventory());
			Component totalText = Component.translatable("screen.ratonera.total_quetzales", total);
			guiGraphics.drawString(this.font, totalText, 44, 74, 0x555555, false);
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
	}

	private void purchase() {
		ClientPlayNetworking.send(new ShopBuyPayload(this.menu.getShopPos()));
	}
}
