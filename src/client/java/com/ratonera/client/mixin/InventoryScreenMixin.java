package com.ratonera.client.mixin;

import com.ratonera.client.screen.TransferScreen;
import com.ratonera.util.QuetzalInventoryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
	private static final int RATONERA_WIDGET_WIDTH = 110;
	private static final int RATONERA_WIDGET_HEIGHT = 20;
	private static final int RATONERA_MARGIN = 6;
	private static final int RATONERA_TEXT_OFFSET_Y = 6;
	private static final int RATONERA_BUTTON_OFFSET_Y = 20;
	private static final int RATONERA_TEXT_COLOR = 0xFFF7E7A2;
	private static final int RATONERA_TEXT_BACKGROUND = 0x88000000;

	@Unique
	private Button ratonera$transferButton;

	@Inject(method = "init", at = @At("TAIL"))
	private void ratonera$addTransferButton(CallbackInfo ci) {
		AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this;
		int overlayX = ratonera$getOverlayX(accessor);
		int overlayY = ratonera$getOverlayY(accessor);

		this.ratonera$transferButton = ((ScreenInvoker) this).ratonera$addRenderableWidget(
			Button.builder(Component.translatable("screen.ratonera.transferir"), button -> {
				Minecraft minecraft = Minecraft.getInstance();

				if (minecraft.player != null) {
					minecraft.setScreen(new TransferScreen((Screen) (Object) this));
				}
			})
				.bounds(overlayX, overlayY + RATONERA_BUTTON_OFFSET_Y, RATONERA_WIDGET_WIDTH, RATONERA_WIDGET_HEIGHT)
				.build()
		);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void ratonera$renderQuetzalTotal(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();

		if (minecraft.player == null) {
			return;
		}

		AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this;
		int overlayX = ratonera$getOverlayX(accessor);
		int overlayY = ratonera$getOverlayY(accessor);

		if (this.ratonera$transferButton != null) {
			this.ratonera$transferButton.setPosition(overlayX, overlayY + RATONERA_BUTTON_OFFSET_Y);
		}

		Component totalText = Component.translatable(
			"screen.ratonera.total_quetzales",
			QuetzalInventoryHelper.contarQuetzales(minecraft.player.getInventory())
		);
		int textWidth = minecraft.font.width(totalText);
		int textLeft = overlayX + Math.max(0, (RATONERA_WIDGET_WIDTH - textWidth) / 2);
		int textTop = overlayY + RATONERA_TEXT_OFFSET_Y;

		guiGraphics.fill(overlayX - 2, textTop - 2, overlayX + RATONERA_WIDGET_WIDTH + 2, textTop + 11, RATONERA_TEXT_BACKGROUND);
		guiGraphics.drawString(minecraft.font, totalText, textLeft, textTop, RATONERA_TEXT_COLOR, true);
	}

	@Unique
	private int ratonera$getOverlayX(AbstractContainerScreenAccessor accessor) {
		Screen screen = (Screen) (Object) this;
		int preferredRight = accessor.ratonera$getLeftPos() + accessor.ratonera$getImageWidth() + RATONERA_MARGIN;
		int clampedRight = Math.min(preferredRight, screen.width - RATONERA_WIDGET_WIDTH - RATONERA_MARGIN);

		if (clampedRight >= accessor.ratonera$getLeftPos() + accessor.ratonera$getImageWidth()) {
			return clampedRight;
		}

		return accessor.ratonera$getLeftPos() + RATONERA_MARGIN;
	}

	@Unique
	private int ratonera$getOverlayY(AbstractContainerScreenAccessor accessor) {
		return accessor.ratonera$getTopPos();
	}
}