package com.alexkrolick.sophisticatedadvancedcrafting.mixin;

import com.alexkrolick.sophisticatedadvancedcrafting.backpack.BackpackCraftingAccess;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * World crafting table: pull ingredients from backpacks that have our Advanced Crafting upgrade
 * when the player inventory alone cannot supply them.
 */
@Mixin(ServerPlaceRecipe.class)
public abstract class ServerPlaceRecipeMixin {
	@Shadow
	protected Inventory inventory;

	@Shadow
	protected RecipeBookMenu<?, ?> menu;

	@Shadow
	protected StackedContents stackedContents;

	@Inject(method = "recipeClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/RecipeBookMenu;fillCraftSlotsStackedContents(Lnet/minecraft/world/entity/player/StackedContents;)V", shift = At.Shift.AFTER))
	private void sophisticatedAdvancedCrafting$accountBackpack(CallbackInfo ci) {
		if (!(this.menu instanceof CraftingMenu)) {
			return;
		}
		Player player = this.inventory.player;
		BackpackCraftingAccess.accountBackpackContents(player, this.stackedContents);
	}

	@Inject(method = "moveItemToGrid", at = @At("HEAD"), cancellable = true)
	private void sophisticatedAdvancedCrafting$moveFromBackpack(Slot slot, ItemStack stack, int maxAmount, CallbackInfoReturnable<Integer> cir) {
		if (!(this.menu instanceof CraftingMenu) || stack.isEmpty() || maxAmount <= 0) {
			return;
		}
		Player player = this.inventory.player;
		if (!BackpackCraftingAccess.hasAdvancedCraftingUpgrade(player)) {
			return;
		}
		// Let vanilla handle player-inventory matches; only intervene when inv has none.
		if (this.inventory.findSlotMatchingUnusedItem(stack) != -1) {
			return;
		}
		ItemStack extracted = BackpackCraftingAccess.extractMatching(player, stack, maxAmount);
		if (extracted.isEmpty()) {
			cir.setReturnValue(-1);
			return;
		}
		int moved = extracted.getCount();
		if (slot.getItem().isEmpty()) {
			slot.set(extracted);
		} else {
			slot.getItem().grow(moved);
		}
		cir.setReturnValue(maxAmount - moved);
	}
}
