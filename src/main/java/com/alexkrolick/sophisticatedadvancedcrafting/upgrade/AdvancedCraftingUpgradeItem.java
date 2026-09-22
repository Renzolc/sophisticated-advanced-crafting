package com.alexkrolick.sophisticatedadvancedcrafting.upgrade;

import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeCountLimitConfig;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeItem;

import java.util.List;

/**
 * Advanced crafting upgrade: same crafting wrapper/container as stock, conflicts with any
 * {@link CraftingUpgradeItem} (stock backpacks/storage crafting upgrades included).
 */
public class AdvancedCraftingUpgradeItem extends CraftingUpgradeItem {
	private static final List<UpgradeConflictDefinition> CONFLICTS = List.of(new UpgradeConflictDefinition(CraftingUpgradeItem.class::isInstance, 0,
			Component.translatable("gui.sophisticated_advanced_crafting.error.crafting_upgrade_exists")));

	public AdvancedCraftingUpgradeItem(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
		super(upgradeTypeLimitConfig);
	}

	@Override
	public List<UpgradeConflictDefinition> getUpgradeConflicts() {
		return CONFLICTS;
	}
}
