package com.alexkrolick.sophisticatedadvancedcrafting.compat.storage;

import com.alexkrolick.sophisticatedadvancedcrafting.init.ModItems;
import com.alexkrolick.sophisticatedadvancedcrafting.upgrade.AdvancedCraftingUpgradeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerRegistry;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeWrapper;

/**
 * Registers the storage advanced crafting upgrade only when Sophisticated Storage is present.
 */
public final class StorageCompat {
	private StorageCompat() {
	}

	public static final DeferredHolder<Item, AdvancedCraftingUpgradeItem> STORAGE_ADVANCED_CRAFTING_UPGRADE = ModItems.ITEMS.register(
			"storage_advanced_crafting_upgrade", () -> new AdvancedCraftingUpgradeItem(Config.SERVER.maxUpgradesPerStorage));

	public static final UpgradeContainerType<CraftingUpgradeWrapper, CraftingUpgradeContainer> STORAGE_ADVANCED_CRAFTING_TYPE =
			new UpgradeContainerType<>(CraftingUpgradeContainer::new);

	public static void init(IEventBus modBus) {
		modBus.addListener(StorageCompat::registerContainers);
	}

	private static void registerContainers(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registries.MENU)) {
			UpgradeContainerRegistry.register(STORAGE_ADVANCED_CRAFTING_UPGRADE.getId(), STORAGE_ADVANCED_CRAFTING_TYPE);
		}
	}
}
