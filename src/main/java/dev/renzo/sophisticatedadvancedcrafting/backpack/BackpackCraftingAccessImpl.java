package dev.renzo.sophisticatedadvancedcrafting.backpack;

import dev.renzo.sophisticatedadvancedcrafting.upgrade.AdvancedCraftingUpgradeItem;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
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
 * and can supply ingredients into a vanilla crafting table.
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
				return true;
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

	/** Account backpack storage stacks into recipe-book contents. */
	public static void accountBackpackContents(Player player, StackedItemContents contents) {
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
			return false;
		});
	}

	/**
	 * Extract up to {@code amount} items matching {@code item} from backpacks that have our upgrade.
	 * When the grid slot already holds a stack, the extracted item must share its components,
	 * matching {@link Inventory#findSlotMatchingCraftingIngredient}.
	 */
	public static ItemStack extractMatching(Player player, Holder<Item> item, ItemStack existing, int amount) {
		if (amount <= 0 || item == null || !hasAdvancedCraftingUpgrade(player)) {
			return ItemStack.EMPTY;
		}
		int[] rem = {amount};
		ItemStack[] out = {ItemStack.EMPTY};

		PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, handlerName, identifier, slot) -> {
			if (rem[0] <= 0 || !backpackHasAdvancedCrafting(backpack)) {
				return rem[0] <= 0;
			}
			InventoryHandler inv = BackpackWrapper.fromStack(backpack).getInventoryHandler();
			for (int i = 0; i < inv.getSlots() && rem[0] > 0; i++) {
				ItemStack inSlot = inv.getStackInSlot(i);
				if (!matches(inSlot, item, existing)) {
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

	private static boolean matches(ItemStack inSlot, Holder<Item> item, ItemStack existing) {
		if (inSlot.isEmpty() || !inSlot.is(item) || !Inventory.isUsableForCrafting(inSlot)) {
			return false;
		}
		return existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, inSlot);
	}

	/** Try to insert leftovers back into a backpack with our upgrade; returns the remainder. */
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
