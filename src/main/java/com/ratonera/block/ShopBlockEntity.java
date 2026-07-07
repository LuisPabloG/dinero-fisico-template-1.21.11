package com.ratonera.block;

import com.ratonera.registry.ModBlockEntities;
import com.ratonera.registry.ModItems;
import com.ratonera.util.QuetzalInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ShopBlockEntity extends BlockEntity implements MenuProvider {
	private static final int CASH_SLOTS = 9;

	private final SimpleContainer productContainer;
	private final SimpleContainer cashRegister;
	private int price;
	private UUID owner;

	public ShopBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SHOP, pos, state);
		this.price = 0;
		this.productContainer = new SimpleContainer(1) {
			@Override
			public void setChanged() {
				super.setChanged();
				ShopBlockEntity.this.setChanged();
			}
		};
		this.cashRegister = new SimpleContainer(CASH_SLOTS) {
			@Override
			public void setChanged() {
				super.setChanged();
				ShopBlockEntity.this.setChanged();
			}
		};
	}

	public Container getProductContainer() {
		return productContainer;
	}

	public Container getCashRegister() {
		return cashRegister;
	}

	public ItemStack getProduct() {
		return productContainer.getItem(0);
	}

	public void setProduct(ItemStack stack) {
		productContainer.setItem(0, stack);
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = Math.max(0, price);
		setChanged();
	}

	@Nullable
	public UUID getOwner() {
		return owner;
	}

	public void setOwner(@Nullable UUID owner) {
		this.owner = owner;
		setChanged();
	}

	public boolean isOwner(Player player) {
		return owner != null && owner.equals(player.getUUID());
	}

	public void buyItem(Player buyer) {
		if (level == null || level.isClientSide()) return;

		ItemStack product = getProduct();
		if (product.isEmpty()) {
			buyer.displayClientMessage(Component.translatable("message.ratonera.shop_sin_stock"), false);
			return;
		}

		if (price <= 0) {
			buyer.displayClientMessage(Component.translatable("message.ratonera.shop_sin_precio"), false);
			return;
		}

		if (isOwner(buyer)) {
			buyer.displayClientMessage(Component.translatable("message.ratonera.shop_owner_compra"), false);
			return;
		}

		if (!QuetzalInventoryHelper.puedeRetirarQuetzales(buyer.getInventory(), price)) {
			buyer.displayClientMessage(Component.translatable("message.ratonera.shop_fondos_insuficientes"), false);
			return;
		}

		if (!QuetzalInventoryHelper.retirarQuetzales(buyer.getInventory(), price)) {
			buyer.displayClientMessage(Component.translatable("message.ratonera.shop_error_cobro"), false);
			return;
		}

		ItemStack toGive = product.copy();

		if (!buyer.getInventory().add(toGive)) {
			buyer.drop(toGive, false);
			buyer.displayClientMessage(Component.translatable("message.ratonera.shop_suelo"), false);
		}

		depositEarnings(price);

		buyer.getInventory().setChanged();
		if (buyer instanceof ServerPlayer sp) {
			sp.containerMenu.broadcastChanges();
		}
		setChanged();
		buyer.displayClientMessage(Component.translatable("message.ratonera.shop_compra_exitosa", product.getHoverName(), price), false);
	}

	private void depositEarnings(int amount) {
		int restante = amount;

		for (int slot = 0; slot < cashRegister.getContainerSize() && restante > 0; slot++) {
			ItemStack stack = cashRegister.getItem(slot);
			int valor = ModItems.getValor(stack.getItem());

			if (valor > 0 && restante >= valor) {
				int posibles = restante / valor;
				int aPoner = Math.min(posibles, stack.getMaxStackSize() - stack.getCount());
				stack.grow(aPoner);
				restante -= aPoner * valor;
			}
		}

		if (restante > 0) {
			for (int slot = 0; slot < cashRegister.getContainerSize() && restante > 0; slot++) {
				if (cashRegister.getItem(slot).isEmpty()) {
					ItemStack billete = createBillStack(restante);
					if (!billete.isEmpty()) {
						cashRegister.setItem(slot, billete);
						restante -= ModItems.getValor(billete.getItem()) * billete.getCount();
					} else {
						break;
					}
				}
			}
		}
	}

	private ItemStack createBillStack(int amount) {
		var denominaciones = new com.ratonera.item.BilleteItem[]{
			ModItems.BILLETE_200,
			ModItems.BILLETE_100,
			ModItems.BILLETE_50,
			ModItems.BILLETE_20,
			ModItems.BILLETE_10,
			ModItems.BILLETE_5,
			ModItems.BILLETE_1
		};

		for (var billete : denominaciones) {
			int valor = billete.getValorQuetzales();
			if (amount >= valor) {
				int count = Math.min(64, amount / valor);
				return new ItemStack(billete, count);
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (level != null && !level.isClientSide()) {
			NonNullList<ItemStack> drops = NonNullList.create();
			ItemStack product = getProduct();
			if (!product.isEmpty()) drops.add(product);
			for (int i = 0; i < cashRegister.getContainerSize(); i++) {
				ItemStack stack = cashRegister.getItem(i);
				if (!stack.isEmpty()) drops.add(stack);
			}
			for (ItemStack stack : drops) {
				level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, stack));
			}
		}
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		NonNullList<ItemStack> productItems = NonNullList.create();
		productItems.add(getProduct());
		ContainerHelper.saveAllItems(output, productItems);

		output.putInt("Price", price);

		if (owner != null) {
			output.putLong("OwnerMost", owner.getMostSignificantBits());
			output.putLong("OwnerLeast", owner.getLeastSignificantBits());
		}

		ValueOutput cashOutput = output.child("CashRegister");
		NonNullList<ItemStack> cashItems = NonNullList.create();
		for (int i = 0; i < cashRegister.getContainerSize(); i++) {
			cashItems.add(cashRegister.getItem(i));
		}
		ContainerHelper.saveAllItems(cashOutput, cashItems);
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		NonNullList<ItemStack> productItems = NonNullList.withSize(1, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, productItems);
		if (!productItems.isEmpty()) {
			setProduct(productItems.get(0));
		}

		price = input.getIntOr("Price", 0);

		long most = input.getLongOr("OwnerMost", 0L);
		long least = input.getLongOr("OwnerLeast", 0L);
		if (most != 0L && least != 0L) {
			owner = new UUID(most, least);
		}

		input.child("CashRegister").ifPresent(cashInput -> {
			NonNullList<ItemStack> cashItems = NonNullList.withSize(CASH_SLOTS, ItemStack.EMPTY);
			ContainerHelper.loadAllItems(cashInput, cashItems);
			for (int i = 0; i < cashItems.size() && i < cashRegister.getContainerSize(); i++) {
				cashRegister.setItem(i, cashItems.get(i));
			}
		});
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.ratonera.shop");
	}

	@Override
	public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
		return new com.ratonera.screen.ShopScreenHandler(syncId, inv, this);
	}
}
