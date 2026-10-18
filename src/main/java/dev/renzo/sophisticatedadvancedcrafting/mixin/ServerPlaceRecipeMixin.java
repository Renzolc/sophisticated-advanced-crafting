package dev.renzo.sophisticatedadvancedcrafting.mixin;

import dev.renzo.sophisticatedadvancedcrafting.backpack.BackpackCraftingAccess;
import net.minecraft.core.Holder;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * World crafting table: when the open menu is a vanilla {@link CraftingMenu}, backpacks that have
 * the Advanced Crafting upgrade count toward craftability and can supply ingredients the player
 * inventory does not have. The upgrade tab places through its own payload and never reaches here.
 */
@Mixin(ServerPlaceRecipe.class)
public abstract class ServerPlaceRecipeMixin {
	@Shadow
	private Inventory inventory;

	/** Present only when this mixin applied. The dev-only check looks it up by name. */
	@Unique
	private static boolean sophisticatedAdvancedCrafting$mixinApplied() {
		return true;
	}

	@Inject(method = "tryPlaceRecipe", at = @At("HEAD"))
	private void sophisticatedAdvancedCrafting$accountBackpack(RecipeHolder<?> recipe, StackedItemContents contents, CallbackInfoReturnable<?> cir) {
		Player player = this.inventory.player;
		if (player != null && player.containerMenu instanceof CraftingMenu) {
			BackpackCraftingAccess.accountBackpackContents(player, contents);
		}
	}

	@Inject(method = "moveItemToGrid", at = @At("HEAD"), cancellable = true)
	private void sophisticatedAdvancedCrafting$moveFromBackpack(Slot slot, Holder<Item> item, int maxAmount, CallbackInfoReturnable<Integer> cir) {
		if (maxAmount <= 0 || item == null) {
			return;
		}
		Player player = this.inventory.player;
		if (player == null || !(player.containerMenu instanceof CraftingMenu)) {
			return;
		}
		if (this.inventory.findSlotMatchingCraftingIngredient(item, slot.getItem()) != -1) {
			return;
		}
		if (!BackpackCraftingAccess.hasAdvancedCraftingUpgrade(player)) {
			return;
		}
		ItemStack extracted = BackpackCraftingAccess.extractMatching(player, item, slot.getItem(), maxAmount);
		if (extracted.isEmpty()) {
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
