package com.alexkrolick.sophisticatedadvancedcrafting.upgrade;

import net.minecraft.core.NonNullList;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Expands a crafting recipe into a 9-slot grid template for dual-source transfer.
 * Shaped recipes are centered in the 3×3 like vanilla {@link PlaceRecipe}.
 */
public final class RecipePlacementHelper {
	private RecipePlacementHelper() {
	}

	public static List<ItemStack> expandCraftingRecipeToGrid(Level level, ResourceLocation recipeId) {
		Optional<RecipeHolder<?>> holderOpt = level.getRecipeManager().byKey(recipeId);
		if (holderOpt.isEmpty() || !(holderOpt.get().value() instanceof CraftingRecipe craftingRecipe)
				|| craftingRecipe.getType() != RecipeType.CRAFTING) {
			return List.of();
		}

		RecipeHolder<?> holder = holderOpt.get();
		List<ItemStack> grid = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
		NonNullList<Ingredient> ingredients = craftingRecipe.getIngredients();

		PlaceRecipe<Ingredient> placer = (ingredient, slot, maxAmount, x, y) -> {
			if (slot >= 0 && slot < 9 && !ingredient.isEmpty()) {
				ItemStack template = firstMatch(ingredient);
				if (!template.isEmpty()) {
					grid.set(slot, template);
				}
			}
		};
		placer.placeRecipe(3, 3, -1, holder, ingredients.iterator(), 1);
		return grid;
	}

	private static ItemStack firstMatch(Ingredient ingredient) {
		if (ingredient.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack[] items = ingredient.getItems();
		if (items.length == 0) {
			return ItemStack.EMPTY;
		}
		ItemStack copy = items[0].copy();
		copy.setCount(1);
		return copy;
	}
}
