package dev.renzo.sophisticatedadvancedcrafting;

import dev.renzo.sophisticatedadvancedcrafting.client.ClientSetup;
import dev.renzo.sophisticatedadvancedcrafting.compat.backpacks.BackpackCompat;
import dev.renzo.sophisticatedadvancedcrafting.compat.storage.StorageCompat;
import dev.renzo.sophisticatedadvancedcrafting.init.ModItems;
import dev.renzo.sophisticatedadvancedcrafting.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(SophisticatedAdvancedCraftingMod.MOD_ID)
public class SophisticatedAdvancedCraftingMod {
	public static final String MOD_ID = "sophisticated_advanced_crafting";
	public static final String BACKPACKS_MOD_ID = "sophisticatedbackpacks";
	public static final String STORAGE_MOD_ID = "sophisticatedstorage";
	public static final Logger LOGGER = LogUtils.getLogger();

	public SophisticatedAdvancedCraftingMod(IEventBus modBus) {
		ModItems.register(modBus);

		boolean backpacks = ModList.get().isLoaded(BACKPACKS_MOD_ID);
		boolean storage = ModList.get().isLoaded(STORAGE_MOD_ID);

		if (backpacks) {
			BackpackCompat.init(modBus);
			LOGGER.info("Backpacks advanced crafting upgrade enabled");
		}
		if (storage) {
			StorageCompat.init(modBus);
			LOGGER.info("Storage advanced crafting upgrade enabled");
		}
		if (!backpacks && !storage) {
			LOGGER.warn("Neither Sophisticated Backpacks nor Sophisticated Storage is loaded; no upgrades registered");
		}

		modBus.addListener(ModNetwork::register);
		if (FMLEnvironment.dist == Dist.CLIENT) {
			ClientSetup.init(modBus);
		}
		LOGGER.info("Sophisticated Advanced Crafting loaded");
	}
}
