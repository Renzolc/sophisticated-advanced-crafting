package com.alexkrolick.sophisticatedadvancedcrafting.compat.backpacks;

import com.alexkrolick.sophisticatedadvancedcrafting.init.ModItems;
import com.alexkrolick.sophisticatedadvancedcrafting.upgrade.AdvancedCraftingUpgradeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.Config;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerRegistry;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeWrapper;

/**
 * Registers the backpacks advanced crafting upgrade only when Sophisticated Backpacks is present.
 */
public final class BackpackCompat {
	private BackpackCompat() {
	}

	public static final DeferredHolder<Item, AdvancedCraftingUpgradeItem> BACKPACK_ADVANCED_CRAFTING_UPGRADE = ModItems.ITEMS.register(
			"backpack_advanced_crafting_upgrade", () -> new AdvancedCraftingUpgradeItem(Config.SERVER.maxUpgradesPerStorage));

	public static final UpgradeContainerType<CraftingUpgradeWrapper, CraftingUpgradeContainer> BACKPACK_ADVANCED_CRAFTING_TYPE =
			new UpgradeContainerType<>(CraftingUpgradeContainer::new);

	public static void init(IEventBus modBus) {
		modBus.addListener(BackpackCompat::registerContainers);
	}

	private static void registerContainers(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registries.MENU)) {
			UpgradeContainerRegistry.register(BACKPACK_ADVANCED_CRAFTING_UPGRADE.getId(), BACKPACK_ADVANCED_CRAFTING_TYPE);
		}
	}
}
