package com.alexkrolick.sophisticatedadvancedcrafting.network;

import com.alexkrolick.sophisticatedadvancedcrafting.SophisticatedAdvancedCraftingMod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
	private ModNetwork() {
	}

	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(SophisticatedAdvancedCraftingMod.MOD_ID).versioned("1");
		registrar.playToServer(PlaceCraftingRecipePayload.TYPE, PlaceCraftingRecipePayload.STREAM_CODEC, PlaceCraftingRecipePayload::handle);
	}
}
