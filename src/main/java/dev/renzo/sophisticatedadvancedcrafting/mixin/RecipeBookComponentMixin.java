package dev.renzo.sophisticatedadvancedcrafting.mixin;

import dev.renzo.sophisticatedadvancedcrafting.backpack.BackpackCraftingAccess;
import dev.renzo.sophisticatedadvancedcrafting.client.recipebook.DualSourceRecipeBookComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * World crafting table: count backpack contents when the Advanced Crafting upgrade is installed.
 * Upgrade-tab books are {@link DualSourceRecipeBookComponent} and place through our own payload.
 */
@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {
	@Shadow
	protected Minecraft minecraft;

	@Shadow
	protected RecipeBookMenu menu;

	@Shadow
	public StackedItemContents stackedContents;

	@Inject(method = {"updateStackedContents", "initVisuals"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;selectMatchingRecipes()V"))
	private void sophisticatedAdvancedCrafting$accountBackpack(CallbackInfo ci) {
		if (this.minecraft == null || this.minecraft.player == null) {
			return;
		}
		if (this.menu instanceof CraftingMenu) {
			BackpackCraftingAccess.accountBackpackContents(this.minecraft.player, this.stackedContents);
		}
	}

	@Inject(method = "tryPlaceRecipe", at = @At("HEAD"), cancellable = true)
	private void sophisticatedAdvancedCrafting$placeDualSource(RecipeCollection collection, RecipeDisplayId id, boolean maxTransfer, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof DualSourceRecipeBookComponent dual) {
			cir.setReturnValue(dual.placeFromBook(collection, id, maxTransfer));
		}
	}
}
