package com.alexkrolick.sophisticatedadvancedcrafting.network;

import com.alexkrolick.sophisticatedadvancedcrafting.SophisticatedAdvancedCraftingMod;
import com.alexkrolick.sophisticatedadvancedcrafting.upgrade.AdvancedCraftingUpgradeItem;
import com.alexkrolick.sophisticatedadvancedcrafting.upgrade.RecipePlacementHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ICraftingContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.CraftingContainerRecipeTransferHandlerServer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Places a crafting recipe into the open advanced crafting upgrade grid, pulling ingredients
 * from backpack/storage inventory and player inventory via Sophisticated Core's dual-source transfer.
 */
public record PlaceCraftingRecipePayload(ResourceLocation recipeId, boolean maxTransfer) implements CustomPacketPayload {
	public static final Type<PlaceCraftingRecipePayload> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(SophisticatedAdvancedCraftingMod.MOD_ID, "place_crafting_recipe"));

	public static final StreamCodec<RegistryFriendlyByteBuf, PlaceCraftingRecipePayload> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, PlaceCraftingRecipePayload::recipeId, ByteBufCodecs.BOOL, PlaceCraftingRecipePayload::maxTransfer,
			PlaceCraftingRecipePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(PlaceCraftingRecipePayload payload, IPayloadContext context) {
		context.enqueueWork(() -> {
			Player player = context.player();
			if (!(player.containerMenu instanceof StorageContainerMenuBase<?> menu)) {
				return;
			}

			Optional<? extends UpgradeContainerBase<?, ?>> craftingOpt = findAdvancedCraftingContainer(menu);
			if (craftingOpt.isEmpty() || !(craftingOpt.get() instanceof ICraftingContainer craftingContainer)) {
				return;
			}

			UpgradeContainerBase<?, ?> upgradeContainer = craftingOpt.get();
			if (!upgradeContainer.isOpen()) {
				menu.getOpenContainer().ifPresent(c -> {
					c.setIsOpen(false);
					menu.setOpenTabId(-1);
				});
				upgradeContainer.setIsOpen(true);
				menu.setOpenTabId(upgradeContainer.getUpgradeContainerId());
			}

			List<Slot> recipeSlots = craftingContainer.getRecipeSlots();
			Set<Slot> excluded = new HashSet<>(recipeSlots);
			List<Slot> upgradeSlots = upgradeContainer.getSlots();
			if (upgradeSlots.size() > 9) {
				excluded.add(upgradeSlots.get(9));
			}

			List<Integer> craftingSlotIndexes = recipeSlots.stream().map(s -> s.index).toList();
			List<Integer> inventorySlotIndexes = menu.slots.stream()
					.filter(s -> s.isActive() && s.mayPickup(player) && !excluded.contains(s))
					.map(s -> s.index)
					.toList();

			// Prefer templates that match items actually present (tag-safe for Core's exact-item transfer).
			List<ItemStack> available = new ArrayList<>();
			for (int index : inventorySlotIndexes) {
				if (index >= 0 && index < menu.slots.size()) {
					ItemStack stack = menu.getSlot(index).getItem();
					if (!stack.isEmpty()) {
						available.add(stack);
					}
				}
			}
			// Also allow matching against items already sitting in the craft grid.
			for (Slot recipeSlot : recipeSlots) {
				ItemStack stack = recipeSlot.getItem();
				if (!stack.isEmpty()) {
					available.add(stack);
				}
			}

			List<ItemStack> stacks = RecipePlacementHelper.expandCraftingRecipeToGrid(player.level(), payload.recipeId(), available);
			if (stacks.isEmpty() || stacks.stream().allMatch(ItemStack::isEmpty)) {
				return;
			}

			CraftingContainerRecipeTransferHandlerServer.setItemsWithStacks(player, payload.recipeId(), RecipeType.CRAFTING, stacks, craftingSlotIndexes,
					inventorySlotIndexes, payload.maxTransfer());
		});
	}

	/**
	 * Prefer an open crafting container whose upgrade item is ours; else any open/first crafting container.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static Optional<? extends UpgradeContainerBase<?, ?>> findAdvancedCraftingContainer(StorageContainerMenuBase<?> menu) {
		Optional openOrFirst = menu.getOpenOrFirstCraftingContainer(RecipeType.CRAFTING);
		if (openOrFirst.isPresent() && isAdvancedCraftingUpgrade((UpgradeContainerBase<?, ?>) openOrFirst.get())) {
			return openOrFirst;
		}
		Optional open = menu.getOpenContainer();
		if (open.isPresent() && isAdvancedCraftingUpgrade((UpgradeContainerBase<?, ?>) open.get())) {
			return open;
		}
		return openOrFirst;
	}

	private static boolean isAdvancedCraftingUpgrade(UpgradeContainerBase<?, ?> container) {
		return container.getUpgradeStack().getItem() instanceof AdvancedCraftingUpgradeItem;
	}
}
