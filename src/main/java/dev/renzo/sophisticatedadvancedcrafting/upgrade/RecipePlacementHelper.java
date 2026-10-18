package dev.renzo.sophisticatedadvancedcrafting.upgrade;

import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Expands a crafting recipe into a 9-slot grid template for dual-source transfer.
 * Shaped recipes are centered in the 3×3 the same way vanilla {@link PlaceRecipeHelper} places them.
 * <p>
 * Templates prefer an item the player actually has that matches each {@link Ingredient}
 * (tag ingredients must be a concrete stack: Core's transfer matches with {@code isSameItemSameComponents}).
 */
public final class RecipePlacementHelper {
	private RecipePlacementHelper() {
	}

	public static List<ItemStack> expandCraftingRecipeToGrid(RecipeHolder<?> holder, List<ItemStack> available) {
		if (holder == null || holder.value().getType() != RecipeType.CRAFTING) {
			return List.of();
		}
		PlacementInfo placement = holder.value().placementInfo();
		if (placement.isImpossibleToPlace()) {
			return List.of();
		}
		List<Ingredient> ingredients = placement.ingredients();
		List<ItemStack> grid = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
		PlaceRecipeHelper.placeRecipe(3, 3, holder.value(), placement.slotsToIngredientIndex(), (index, slot, x, y) -> {
			if (index == PlacementInfo.EMPTY_SLOT || slot < 0 || slot >= 9) {
				return;
			}
			if (index >= ingredients.size()) {
				return;
			}
			ItemStack template = matchFromAvailable(ingredients.get(index), available);
			if (!template.isEmpty()) {
				grid.set(slot, template);
			}
		});
		return grid;
	}

	/** Prefer a stack the player/storage actually has that matches the ingredient; else the first catalog match. */
	private static ItemStack matchFromAvailable(Ingredient ingredient, List<ItemStack> available) {
		if (ingredient.isEmpty()) {
			return ItemStack.EMPTY;
		}
		for (ItemStack stack : available) {
			if (!stack.isEmpty() && ingredient.test(stack)) {
				ItemStack copy = stack.copyWithCount(1);
				return copy;
			}
		}
		return firstMatch(ingredient);
	}

	private static ItemStack firstMatch(Ingredient ingredient) {
		return ingredient.items()
				.findFirst()
				.map(holder -> new ItemStack(holder.value()))
				.orElse(ItemStack.EMPTY);
	}
}
