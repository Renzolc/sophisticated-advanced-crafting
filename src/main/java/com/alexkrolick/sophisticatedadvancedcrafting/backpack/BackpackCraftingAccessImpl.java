package com.alexkrolick.sophisticatedadvancedcrafting.backpack;

import com.alexkrolick.sophisticatedadvancedcrafting.upgrade.AdvancedCraftingUpgradeItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;

/**
 * Dual-source helpers for world crafting tables: when the player carries a Sophisticated
 * Backpack with our Advanced Crafting upgrade, backpack inventory counts for craftability
 * and can supply ingredients into a vanilla {@code CraftingMenu}.
 */
public final class BackpackCraftingAccessImpl {
	private BackpackCraftingAccessImpl() {
	}

	/** True if any worn/carried backpack has our Advanced Crafting upgrade installed. */
	public static boolean hasAdvancedCraftingUpgrade(Player player) {
		if (player == null) {
			return false;
		}
		boolean[] found = {false};
		PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, handlerName, identifier, slot) -> {
			if (backpackHasAdvancedCrafting(backpack)) {
				found[0] = true;
				return true; // stop
			}
			return false;
		});
		return found[0];
	}

	public static boolean backpackHasAdvancedCrafting(ItemStack backpackStack) {
		IBackpackWrapper wrapper = BackpackWrapper.fromStack(backpackStack);
		UpgradeHandler upgrades = wrapper.getUpgradeHandler();
		for (IUpgradeWrapper upgrade : upgrades.getSlotWrappers().values()) {
			if (upgrade.getUpgradeStack().getItem() instanceof AdvancedCraftingUpgradeItem) {
				return true;
			}
		}
		return false;
	}

	/** Account backpack storage stacks into recipe-book {@link StackedContents} (no double-count of empty). */
	public static void accountBackpackContents(Player player, StackedContents contents) {
		if (!hasAdvancedCraftingUpgrade(player)) {
			return;
		}
		PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, handlerName, identifier, slot) -> {
			if (!backpackHasAdvancedCrafting(backpack)) {
				return false;
			}
			InventoryHandler inv = BackpackWrapper.fromStack(backpack).getInventoryHandler();
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (!stack.isEmpty()) {
					contents.accountSimpleStack(stack);
				}
			}
			return false; // continue other backpacks
		});
	}

	/**
	 * Extract up to {@code amount} items matching {@code template} (same item+components) from
	 * backpacks that have our Advanced Crafting upgrade. Returns the extracted stack (may be partial).
	 */
	public static ItemStack extractMatching(Player player, ItemStack template, int amount) {
		if (amount <= 0 || template.isEmpty() || !hasAdvancedCraftingUpgrade(player)) {
			return ItemStack.EMPTY;
		}
		ItemStack collected = ItemStack.EMPTY;
		int remaining = amount;
		int[] rem = {remaining};
		ItemStack[] out = {collected};

		PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, handlerName, identifier, slot) -> {
			if (rem[0] <= 0 || !backpackHasAdvancedCrafting(backpack)) {
				return rem[0] <= 0;
			}
			InventoryHandler inv = BackpackWrapper.fromStack(backpack).getInventoryHandler();
			for (int i = 0; i < inv.getSlots() && rem[0] > 0; i++) {
				ItemStack inSlot = inv.getStackInSlot(i);
				if (inSlot.isEmpty() || !ItemStack.isSameItemSameComponents(inSlot, template)) {
					continue;
				}
				ItemStack extracted = inv.extractItem(i, rem[0], false);
				if (extracted.isEmpty()) {
					continue;
				}
				if (out[0].isEmpty()) {
					out[0] = extracted;
				} else {
					out[0].grow(extracted.getCount());
				}
				rem[0] -= extracted.getCount();
			}
			return rem[0] <= 0;
		});
		return out[0];
	}

	/** Try to insert leftovers back into a backpack with our upgrade; returns remainder. */
	public static ItemStack insertRemaining(Player player, ItemStack stack) {
		if (stack.isEmpty() || !hasAdvancedCraftingUpgrade(player)) {
			return stack;
		}
		ItemStack[] remaining = {stack.copy()};
		PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, handlerName, identifier, slot) -> {
			if (remaining[0].isEmpty() || !backpackHasAdvancedCrafting(backpack)) {
				return remaining[0].isEmpty();
			}
			InventoryHandler inv = BackpackWrapper.fromStack(backpack).getInventoryHandler();
			for (int i = 0; i < inv.getSlots() && !remaining[0].isEmpty(); i++) {
				remaining[0] = inv.insertItem(i, remaining[0], false);
			}
			return remaining[0].isEmpty();
		});
		return remaining[0];
	}
}
