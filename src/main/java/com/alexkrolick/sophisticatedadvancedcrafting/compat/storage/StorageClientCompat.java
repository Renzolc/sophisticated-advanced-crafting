package com.alexkrolick.sophisticatedadvancedcrafting.compat.storage;

import com.alexkrolick.sophisticatedadvancedcrafting.upgrade.AdvancedCraftingUpgradeTab;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeGuiManager;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageButtonDefinitions;

public final class StorageClientCompat {
	private StorageClientCompat() {
	}

	public static void registerTab() {
		UpgradeGuiManager.registerTab(StorageCompat.STORAGE_ADVANCED_CRAFTING_TYPE,
				(CraftingUpgradeContainer uc, Position p, StorageScreenBase<?> s) -> new AdvancedCraftingUpgradeTab(uc, p, s,
						StorageButtonDefinitions.SHIFT_CLICK_TARGET, StorageButtonDefinitions.REFILL_CRAFTING_GRID,
						Component.translatable("gui.sophisticated_advanced_crafting.tab.storage_advanced_crafting"),
						Component.translatable("gui.sophisticated_advanced_crafting.tab.storage_advanced_crafting.tooltip")));
	}
}
