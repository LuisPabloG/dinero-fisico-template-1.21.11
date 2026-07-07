package com.ratonera.screen;

import com.ratonera.block.ShopBlockEntity;
import com.ratonera.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ShopScreenHandler extends AbstractContainerMenu {
	private static final int PRODUCT_SLOTS = 1;
	private static final int PLAYER_SLOTS = 36;
	private static final int TOTAL_SLOTS = PRODUCT_SLOTS + PLAYER_SLOTS;

	private static final int DATA_COUNT = 4; // price, posX, posY, posZ

	private final Container productContainer;
	private final ContainerData shopData;
	private final BlockPos pos;

	public ShopScreenHandler(int syncId, Inventory playerInventory) {
		this(syncId, playerInventory, new SimpleContainer(PRODUCT_SLOTS), new SimpleContainerData(DATA_COUNT), BlockPos.ZERO);
	}

	public ShopScreenHandler(int syncId, Inventory playerInventory, ShopBlockEntity shop) {
		this(syncId, playerInventory, shop.getProductContainer(), createData(shop), shop.getBlockPos());
	}

	private ShopScreenHandler(int syncId, Inventory playerInventory, Container productContainer, ContainerData shopData, BlockPos pos) {
		super(ModMenuTypes.SHOP, syncId);
		this.productContainer = productContainer;
		this.shopData = shopData;
		this.pos = pos;

		checkContainerSize(productContainer, PRODUCT_SLOTS);
		productContainer.startOpen(playerInventory.player);

		this.addSlot(new Slot(productContainer, 0, 80, 20) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public boolean mayPickup(Player player) {
				return false;
			}
		});

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}

		this.addDataSlots(shopData);
	}

	private static ContainerData createData(ShopBlockEntity shop) {
		BlockPos p = shop.getBlockPos();
		return new SimpleContainerData(DATA_COUNT) {{
			set(0, shop.getPrice());
			set(1, p.getX());
			set(2, p.getY());
			set(3, p.getZ());
		}};
	}

	public int getPrice() {
		return shopData.get(0);
	}

	public BlockPos getShopPos() {
		return pos.equals(BlockPos.ZERO) ? new BlockPos(shopData.get(1), shopData.get(2), shopData.get(3)) : pos;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		if (slotIndex == 0) return ItemStack.EMPTY;

		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);

		if (slot.hasItem()) {
			ItemStack current = slot.getItem();
			stack = current.copy();

			if (slotIndex < PRODUCT_SLOTS + 27) {
				if (!this.moveItemStackTo(current, PRODUCT_SLOTS + 27, TOTAL_SLOTS, false)) {
					return ItemStack.EMPTY;
				}
			} else {
				if (!this.moveItemStackTo(current, PRODUCT_SLOTS, PRODUCT_SLOTS + 27, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (current.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			}

			slot.setChanged();
		}

		return stack;
	}

	@Override
	public boolean stillValid(Player player) {
		return productContainer.stillValid(player);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		productContainer.stopOpen(player);
	}
}
