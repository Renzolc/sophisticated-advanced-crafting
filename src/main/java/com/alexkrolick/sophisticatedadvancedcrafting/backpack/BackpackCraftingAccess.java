package com.alexkrolick.sophisticatedadvancedcrafting.backpack;

import com.alexkrolick.sophisticatedadvancedcrafting.SophisticatedAdvancedCraftingMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
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
						"com.alexkrolick.sophisticatedadvancedcrafting.backpack.BackpackCraftingAccessImpl");
				has = impl.getMethod("hasAdvancedCraftingUpgrade", Player.class);
				account = impl.getMethod("accountBackpackContents", Player.class, StackedContents.class);
				extract = impl.getMethod("extractMatching", Player.class, ItemStack.class, int.class);
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

	public static void accountBackpackContents(Player player, StackedContents contents) {
		if (ACCOUNT == null) {
			return;
		}
		try {
			ACCOUNT.invoke(null, player, contents);
		} catch (ReflectiveOperationException ignored) {
		}
	}

	public static ItemStack extractMatching(Player player, ItemStack template, int amount) {
		if (EXTRACT == null) {
			return ItemStack.EMPTY;
		}
		try {
			return (ItemStack) EXTRACT.invoke(null, player, template, amount);
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
