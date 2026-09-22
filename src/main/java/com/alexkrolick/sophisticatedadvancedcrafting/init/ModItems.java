package com.alexkrolick.sophisticatedadvancedcrafting.init;

import com.alexkrolick.sophisticatedadvancedcrafting.SophisticatedAdvancedCraftingMod;
import com.alexkrolick.sophisticatedadvancedcrafting.compat.backpacks.BackpackCompat;
import com.alexkrolick.sophisticatedadvancedcrafting.compat.storage.StorageCompat;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
	private ModItems() {
	}

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SophisticatedAdvancedCraftingMod.MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
			SophisticatedAdvancedCraftingMod.MOD_ID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_TABS.register("main",
			() -> CreativeModeTab.builder().title(Component.translatable("itemGroup.sophisticated_advanced_crafting"))
					.icon(ModItems::tabIcon).displayItems((params, output) -> {
						if (ModList.get().isLoaded(SophisticatedAdvancedCraftingMod.BACKPACKS_MOD_ID)) {
							output.accept(BackpackCompat.BACKPACK_ADVANCED_CRAFTING_UPGRADE.get());
						}
						if (ModList.get().isLoaded(SophisticatedAdvancedCraftingMod.STORAGE_MOD_ID)) {
							output.accept(StorageCompat.STORAGE_ADVANCED_CRAFTING_UPGRADE.get());
						}
					}).build());

	private static ItemStack tabIcon() {
		if (ModList.get().isLoaded(SophisticatedAdvancedCraftingMod.BACKPACKS_MOD_ID)) {
			return new ItemStack(BackpackCompat.BACKPACK_ADVANCED_CRAFTING_UPGRADE.get());
		}
		if (ModList.get().isLoaded(SophisticatedAdvancedCraftingMod.STORAGE_MOD_ID)) {
			return new ItemStack(StorageCompat.STORAGE_ADVANCED_CRAFTING_UPGRADE.get());
		}
		return ItemStack.EMPTY;
	}

	public static void register(IEventBus modBus) {
		ITEMS.register(modBus);
		CREATIVE_TABS.register(modBus);
	}
}
