package com.alexkrolick.sophisticatedadvancedcrafting.client;

import com.alexkrolick.sophisticatedadvancedcrafting.SophisticatedAdvancedCraftingMod;
import com.alexkrolick.sophisticatedadvancedcrafting.compat.backpacks.BackpackClientCompat;
import com.alexkrolick.sophisticatedadvancedcrafting.compat.storage.StorageClientCompat;
import com.alexkrolick.sophisticatedadvancedcrafting.upgrade.AdvancedCraftingUpgradeTab;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;

public final class ClientSetup {
	private ClientSetup() {
	}

	public static void init(IEventBus modBus) {
		modBus.addListener(ClientSetup::onClientSetup);
		NeoForge.EVENT_BUS.addListener(ClientSetup::onKeyPressed);
		NeoForge.EVENT_BUS.addListener(ClientSetup::onCharTyped);
		NeoForge.EVENT_BUS.addListener(ClientSetup::onMouseClicked);
	}

	private static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			if (ModList.get().isLoaded(SophisticatedAdvancedCraftingMod.BACKPACKS_MOD_ID)) {
				BackpackClientCompat.registerTab();
			}
			if (ModList.get().isLoaded(SophisticatedAdvancedCraftingMod.STORAGE_MOD_ID)) {
				StorageClientCompat.registerTab();
			}
		});
	}

	private static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
		findOpenAdvancedTab(event.getScreen()).ifPresent(tab -> {
			if (tab.handleKeyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
				event.setCanceled(true);
			}
		});
	}

	private static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
		findOpenAdvancedTab(event.getScreen()).ifPresent(tab -> {
			if (tab.handleCharTyped(event.getCodePoint(), event.getModifiers())) {
				event.setCanceled(true);
			}
		});
	}

	/**
	 * Forward clicks to the floating recipe book even when it sits outside the upgrade tab's
	 * widget bounds (book is rendered by the tab but can float beside/above/below it).
	 */
	private static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
		findOpenAdvancedTab(event.getScreen()).ifPresent(tab -> {
			if (tab.handleRecipeBookMouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
				event.setCanceled(true);
			}
		});
	}

	private static java.util.Optional<AdvancedCraftingUpgradeTab> findOpenAdvancedTab(Screen screen) {
		if (!(screen instanceof StorageScreenBase<?> storageScreen)) {
			return java.util.Optional.empty();
		}
		return storageScreen.getUpgradeSettingsControl().getOpenTab()
				.filter(AdvancedCraftingUpgradeTab.class::isInstance)
				.map(AdvancedCraftingUpgradeTab.class::cast);
	}
}
