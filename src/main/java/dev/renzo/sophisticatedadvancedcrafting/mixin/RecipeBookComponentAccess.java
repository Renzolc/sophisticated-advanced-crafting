package dev.renzo.sophisticatedadvancedcrafting.mixin;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.world.entity.player.StackedItemContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Private recipe-book members the upgrade-tab book has to drive without touching vanilla open state.
 */
@Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentAccess {
	@Invoker("initVisuals")
	void sac$initVisuals();

	@Accessor("ghostSlots")
	GhostSlots sac$getGhostSlots();

	@Accessor("stackedContents")
	StackedItemContents sac$getStackedContents();

	@Accessor("recipeBookPage")
	RecipeBookPage sac$getRecipeBookPage();

	@Accessor("width")
	void sac$setWidth(int width);

	@Accessor("height")
	void sac$setHeight(int height);

	@Accessor("widthTooNarrow")
	void sac$setWidthTooNarrow(boolean widthTooNarrow);

	@Accessor("book")
	void sac$setBook(ClientRecipeBook book);

	@Accessor("timesInventoryChanged")
	int sac$getTimesInventoryChanged();

	@Accessor("timesInventoryChanged")
	void sac$setTimesInventoryChanged(int times);

	@Accessor("visible")
	void sac$setVisible(boolean visible);
}
