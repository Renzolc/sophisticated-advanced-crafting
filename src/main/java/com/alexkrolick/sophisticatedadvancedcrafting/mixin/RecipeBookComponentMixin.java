package com.alexkrolick.sophisticatedadvancedcrafting.mixin;

import com.alexkrolick.sophisticatedadvancedcrafting.backpack.BackpackCraftingAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * World crafting table: count backpack contents (when Advanced Crafting upgrade is installed)
 * for recipe-book craftability highlighting.
 */
@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {
	@Shadow
	protected Minecraft minecraft;

	@Shadow
	protected RecipeBookMenu<?, ?> menu;

	@Shadow
	public StackedContents stackedContents;

	@Inject(method = "updateStackedContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;updateCollections(Z)V"))
	private void sophisticatedAdvancedCrafting$accountBackpackOnUpdate(CallbackInfo ci) {
		accountIfWorldCraftingTable();
	}

	@Inject(method = "initVisuals", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;updateCollections(Z)V"))
	private void sophisticatedAdvancedCrafting$accountBackpackOnInit(CallbackInfo ci) {
		accountIfWorldCraftingTable();
	}

	private void accountIfWorldCraftingTable() {
		if (this.minecraft == null || this.minecraft.player == null) {
			return;
		}
		// Only vanilla/world crafting tables — upgrade-tab book uses DualSourceRecipeBookMenu, not CraftingMenu.
		if (this.menu instanceof CraftingMenu) {
			BackpackCraftingAccess.accountBackpackContents(this.minecraft.player, this.stackedContents);
		}
	}
}
