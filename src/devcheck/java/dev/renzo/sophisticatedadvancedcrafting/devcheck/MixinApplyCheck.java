package dev.renzo.sophisticatedadvancedcrafting.devcheck;

import dev.renzo.sophisticatedadvancedcrafting.SophisticatedAdvancedCraftingMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Dev-run only. Confirms mixin targets load and the injected hooks exist.
 * This source set is not packaged into the mod jar.
 * Registered from the mod constructor by reflection, because {@code @EventBusSubscriber}
 * no longer selects the mod bus.
 */
public final class MixinApplyCheck {
	private MixinApplyCheck() {
	}

	public static void register(IEventBus modBus) {
		modBus.addListener(MixinApplyCheck::onCommonSetup);
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(MixinApplyCheck::verify);
	}

	private static void verify() {
		String side = FMLEnvironment.getDist() == Dist.CLIENT ? "client" : "server";
		requireMethod("net.minecraft.recipebook.ServerPlaceRecipe", "sophisticatedAdvancedCrafting$mixinApplied");
		requireClass("net.minecraft.world.inventory.CraftingMenu");
		if (FMLEnvironment.getDist() == Dist.CLIENT) {
			requireMethod("net.minecraft.client.gui.screens.recipebook.RecipeBookComponent", "sac$initVisuals");
			requireClass("net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent");
			requireClass("net.minecraft.client.gui.screens.recipebook.RecipeBookPage");
		}
		SophisticatedAdvancedCraftingMod.LOGGER.info("SAC devcheck: mixins applied ({})", side);
	}

	private static void requireClass(String name) {
		try {
			Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("SAC devcheck failed to load " + name, e);
		}
	}

	private static void requireMethod(String className, String method) {
		try {
			Class<?> type = Class.forName(className);
			type.getDeclaredMethod(method);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("SAC devcheck: mixin hook " + className + "#" + method + " is missing", e);
		}
	}
}
