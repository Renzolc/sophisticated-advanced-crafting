package com.alexkrolick.sophisticatedadvancedcrafting.client.recipebook;

import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.function.Supplier;

/**
 * Vanilla {@link RecipeButton} reads {@code player.containerMenu} and casts it to
 * {@link RecipeBookMenu}. On the backpack screen that menu is {@code BackpackContainer},
 * which crashes. This button instead uses the dual-source bridge menu we own.
 */
public class DualSourceRecipeButton extends RecipeButton {
	private final Supplier<RecipeBookMenu<?, ?>> menuSupplier;

	public DualSourceRecipeButton(Supplier<RecipeBookMenu<?, ?>> menuSupplier) {
		this.menuSupplier = menuSupplier;
	}

	@Override
	public void init(RecipeCollection collection, RecipeBookPage recipeBookPage) {
		this.collection = collection;
		RecipeBookMenu<?, ?> bridge = menuSupplier.get();
		if (bridge == null) {
			throw new IllegalStateException("DualSourceRecipeBookMenu is not ready for recipe button init");
		}
		this.menu = bridge;
		this.book = recipeBookPage.getRecipeBook();
		List<RecipeHolder<?>> recipes = collection.getRecipes(this.book.isFiltering(this.menu));
		for (RecipeHolder<?> recipe : recipes) {
			if (this.book.willHighlight(recipe)) {
				recipeBookPage.recipesShown(recipes);
				this.animationTime = 15.0F;
				break;
			}
		}
	}
}
