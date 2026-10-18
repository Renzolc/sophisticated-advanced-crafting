package dev.renzo.sophisticatedadvancedcrafting.backpack;

import dev.renzo.sophisticatedadvancedcrafting.SophisticatedAdvancedCraftingMod;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

/**
 * Reflection facade so mixins can call backpack dual-source helpers without a hard runtime
 * dependency when Sophisticated Backpacks is not installed.
 */
public final class BackpackCraftingAccess {
	private static final boolean BACKPACKS = ModList.get().isLoaded(SophisticatedAdvancedCraftingMod.BACKPACKS_MOD_ID);
	private static final Method HAS_UPGRADE;
	private static final Method ACCOUNT;
	private static final Method EXTRACT;
	private static final Method INSERT;

	static {
		Method has = null, account = null, extract = null, insert = null;
		if (BACKPACKS) {
			try {
				Class<?> impl = Class.forName(
						"dev.renzo.sophisticatedadvancedcrafting.backpack.BackpackCraftingAccessImpl");
				has = impl.getMethod("hasAdvancedCraftingUpgrade", Player.class);
				account = impl.getMethod("accountBackpackContents", Player.class, StackedItemContents.class);
				extract = impl.getMethod("extractMatching", Player.class, Holder.class, ItemStack.class, int.class);
				insert = impl.getMethod("insertRemaining", Player.class, ItemStack.class);
			} catch (ReflectiveOperationException e) {
				SophisticatedAdvancedCraftingMod.LOGGER.warn("Backpack crafting access unavailable", e);
			}
		}
		HAS_UPGRADE = has;
		ACCOUNT = account;
		EXTRACT = extract;
		INSERT = insert;
	}

	private BackpackCraftingAccess() {
	}

	public static boolean hasAdvancedCraftingUpgrade(Player player) {
		if (HAS_UPGRADE == null) {
			return false;
		}
		try {
			return (boolean) HAS_UPGRADE.invoke(null, player);
		} catch (ReflectiveOperationException e) {
			return false;
		}
	}

	public static void accountBackpackContents(Player player, StackedItemContents contents) {
		if (ACCOUNT == null) {
			return;
		}
		try {
			ACCOUNT.invoke(null, player, contents);
		} catch (ReflectiveOperationException ignored) {
		}
	}

	public static ItemStack extractMatching(Player player, Holder<Item> item, ItemStack existing, int amount) {
		if (EXTRACT == null) {
			return ItemStack.EMPTY;
		}
		try {
			return (ItemStack) EXTRACT.invoke(null, player, item, existing, amount);
		} catch (ReflectiveOperationException e) {
			return ItemStack.EMPTY;
		}
	}

	public static ItemStack insertRemaining(Player player, ItemStack stack) {
		if (INSERT == null) {
			return stack;
		}
		try {
			return (ItemStack) INSERT.invoke(null, player, stack);
		} catch (ReflectiveOperationException e) {
			return stack;
		}
	}
}
