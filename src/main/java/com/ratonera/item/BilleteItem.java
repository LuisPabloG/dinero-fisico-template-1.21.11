package com.ratonera.item;

import net.minecraft.world.item.Item;

public class BilleteItem extends Item {
	private final int valorQuetzales;

	public BilleteItem(int valorQuetzales, Properties properties) {
		super(properties);
		this.valorQuetzales = valorQuetzales;
	}

	public int getValorQuetzales() {
		return valorQuetzales;
	}
}