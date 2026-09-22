package com.alexkrolick.sophisticatedadvancedcrafting.compat.backpacks;

import com.alexkrolick.sophisticatedadvancedcrafting.upgrade.AdvancedCraftingUpgradeTab;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.SBPButtonDefinitions;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeGuiManager;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;

public final class BackpackClientCompat {
	private BackpackClientCompat() {
	}

	public static void registerTab() {
		UpgradeGuiManager.registerTab(BackpackCompat.BACKPACK_ADVANCED_CRAFTING_TYPE,
				(CraftingUpgradeContainer uc, Position p, StorageScreenBase<?> s) -> new AdvancedCraftingUpgradeTab(uc, p, s,
						SBPButtonDefinitions.SHIFT_CLICK_TARGET, SBPButtonDefinitions.REFILL_CRAFTING_GRID,
						Component.translatable("gui.sophisticated_advanced_crafting.tab.backpack_advanced_crafting"),
						Component.translatable("gui.sophisticated_advanced_crafting.tab.backpack_advanced_crafting.tooltip")));
	}
}
