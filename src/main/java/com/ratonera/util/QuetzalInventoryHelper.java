package com.ratonera.util;

import com.ratonera.item.BilleteItem;
import com.ratonera.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class QuetzalInventoryHelper {
	private static final BilleteDenominacion[] DENOMINACIONES_DESC = new BilleteDenominacion[] {
		new BilleteDenominacion(ModItems.BILLETE_200, 200),
		new BilleteDenominacion(ModItems.BILLETE_100, 100),
		new BilleteDenominacion(ModItems.BILLETE_50, 50),
		new BilleteDenominacion(ModItems.BILLETE_20, 20),
		new BilleteDenominacion(ModItems.BILLETE_10, 10),
		new BilleteDenominacion(ModItems.BILLETE_5, 5),
		new BilleteDenominacion(ModItems.BILLETE_1, 1)
	};

	private QuetzalInventoryHelper() {
	}

	public static int contarQuetzales(Inventory inventory) {
		int total = 0;

		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			total += ModItems.getValor(stack.getItem()) * stack.getCount();
		}

		return total;
	}

	public static boolean puedeRetirarQuetzales(Inventory inventory, int cantidad) {
		return cantidad > 0 && encontrarPlanRetiro(inventory, cantidad) != null;
	}

	public static boolean retirarQuetzales(Inventory inventory, int cantidad) {
		int[] planRetiro = encontrarPlanRetiro(inventory, cantidad);

		if (planRetiro == null) {
			return false;
		}

		for (int indiceDenominacion = 0; indiceDenominacion < DENOMINACIONES_DESC.length; indiceDenominacion++) {
			BilleteDenominacion denominacion = DENOMINACIONES_DESC[indiceDenominacion];
			int restantesPorRetirar = planRetiro[indiceDenominacion];

			if (restantesPorRetirar == 0) {
				continue;
			}

			for (int slot = 0; slot < inventory.getContainerSize() && restantesPorRetirar > 0; slot++) {
				ItemStack stack = inventory.getItem(slot);

				if (stack.getItem() != denominacion.item()) {
					continue;
				}

				int aRetirar = Math.min(restantesPorRetirar, stack.getCount());
				stack.shrink(aRetirar);

				if (stack.isEmpty()) {
					inventory.setItem(slot, ItemStack.EMPTY);
				}

				restantesPorRetirar -= aRetirar;
			}
		}

		inventory.setChanged();
		return true;
	}

	public static int depositarQuetzales(ServerPlayer player, int cantidad) {
		int restante = cantidad;
		int montoSoltado = 0;

		for (BilleteDenominacion denominacion : DENOMINACIONES_DESC) {
			int numeroBilletes = restante / denominacion.valor();
			restante %= denominacion.valor();

			while (numeroBilletes > 0) {
				int tamanoStack = Math.min(64, numeroBilletes);
				ItemStack stack = new ItemStack(denominacion.item(), tamanoStack);
				player.getInventory().add(stack);

				if (!stack.isEmpty()) {
					montoSoltado += stack.getCount() * denominacion.valor();
					player.drop(stack, false);
				}

				numeroBilletes -= tamanoStack;
			}
		}

		player.getInventory().setChanged();
		player.containerMenu.broadcastChanges();
		return montoSoltado;
	}

	private static int[] encontrarPlanRetiro(Inventory inventory, int cantidad) {
		if (cantidad <= 0) {
			return null;
		}

		int[] disponibles = contarDisponibles(inventory);
		int[] seleccion = new int[DENOMINACIONES_DESC.length];

		return buscarCombinacion(0, cantidad, disponibles, seleccion) ? seleccion : null;
	}

	private static int[] contarDisponibles(Inventory inventory) {
		int[] disponibles = new int[DENOMINACIONES_DESC.length];

		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			Item item = inventory.getItem(slot).getItem();

			for (int indiceDenominacion = 0; indiceDenominacion < DENOMINACIONES_DESC.length; indiceDenominacion++) {
				if (item == DENOMINACIONES_DESC[indiceDenominacion].item()) {
					disponibles[indiceDenominacion] += inventory.getItem(slot).getCount();
					break;
				}
			}
		}

		return disponibles;
	}

	private static boolean buscarCombinacion(int indiceDenominacion, int restante, int[] disponibles, int[] seleccion) {
		if (restante == 0) {
			return true;
		}

		if (indiceDenominacion >= DENOMINACIONES_DESC.length) {
			return false;
		}

		BilleteDenominacion denominacion = DENOMINACIONES_DESC[indiceDenominacion];
		int maximoUso = Math.min(disponibles[indiceDenominacion], restante / denominacion.valor());

		for (int usar = maximoUso; usar >= 0; usar--) {
			seleccion[indiceDenominacion] = usar;

			if (buscarCombinacion(indiceDenominacion + 1, restante - usar * denominacion.valor(), disponibles, seleccion)) {
				return true;
			}
		}

		seleccion[indiceDenominacion] = 0;
		return false;
	}

	private record BilleteDenominacion(BilleteItem item, int valor) {
	}
}