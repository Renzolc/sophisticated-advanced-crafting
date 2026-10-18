package dev.renzo.sophisticatedadvancedcrafting.client.recipebook;

import dev.renzo.sophisticatedadvancedcrafting.mixin.RecipeBookComponentAccess;
import dev.renzo.sophisticatedadvancedcrafting.network.PlaceCraftingRecipePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;

/**
 * Vanilla green recipe book that:
 * <ul>
 *   <li>does not replace {@code player.containerMenu}</li>
 *   <li>does not mutate vanilla recipe-book open state or send book settings</li>
 *   <li>counts storage slots (including the player inventory on that screen) for craftability</li>
 *   <li>places via {@link PlaceCraftingRecipePayload} instead of the vanilla place-recipe packet</li>
 * </ul>
 * The menu is fixed at construction. Call {@link #initAnchored} rather than {@link #init}.
 */
public class DualSourceRecipeBookComponent extends CraftingRecipeBookComponent {
	private int bookLeft;
	private int bookTop;
	private int lastStorageHash;
	private RecipeDisplayId lastPlaced;

	public DualSourceRecipeBookComponent(DualSourceRecipeBookMenu menu) {
		super(menu);
	}

	public DualSourceRecipeBookMenu bridge() {
		return (DualSourceRecipeBookMenu) this.menu;
	}

	/**
	 * Place the panel's top-left at ({@code bookLeft}, {@code bookTop}).
	 * Vanilla centers with {@code (width - 147) / 2 - xOffset} and {@code xOffset = 86}, and
	 * vertically with {@code (height - 166) / 2}. Passing those dimensions makes both origins
	 * land on the anchor. {@code xOffset} stays 86 so a recipe click does not auto-close the book.
	 */
	public void initAnchored(Minecraft minecraft, int bookLeft, int bookTop) {
		this.bookLeft = bookLeft;
		this.bookTop = bookTop;
		this.minecraft = minecraft;
		RecipeBookComponentAccess access = (RecipeBookComponentAccess) (Object) this;
		access.sac$setWidth(2 * (bookLeft + 86) + 147);
		access.sac$setHeight(2 * bookTop + 166);
		access.sac$setWidthTooNarrow(false);
		access.sac$setBook(minecraft.player.getRecipeBook());
		access.sac$setTimesInventoryChanged(minecraft.player.getInventory().getTimesChanged());
		access.sac$setVisible(true);
		access.sac$initVisuals();
		bridge().syncSlotPositions();
		refreshContents();
		this.lastStorageHash = computeStorageHash(bridge().getStorageMenu());
	}

	public void reanchor(int bookLeft, int bookTop) {
		if (this.minecraft == null) {
			return;
		}
		if (this.bookLeft == bookLeft && this.bookTop == bookTop && isVisible()) {
			bridge().syncSlotPositions();
			return;
		}
		initAnchored(this.minecraft, bookLeft, bookTop);
	}

	@Override
	public void tick() {
		if (!isVisible() || this.minecraft == null || this.minecraft.player == null) {
			return;
		}
		int changed = this.minecraft.player.getInventory().getTimesChanged();
		int storageHash = computeStorageHash(bridge().getStorageMenu());
		RecipeBookComponentAccess access = (RecipeBookComponentAccess) (Object) this;
		if (changed != access.sac$getTimesInventoryChanged() || storageHash != this.lastStorageHash) {
			access.sac$setTimesInventoryChanged(changed);
			this.lastStorageHash = storageHash;
			refreshContents();
		}
	}

	/**
	 * Storage-menu slots already include the player inventory, so skip the vanilla player-inventory fill.
	 * {@code updateStackedContents} is private and would count that inventory twice.
	 */
	private void refreshContents() {
		StackedItemContents contents = ((RecipeBookComponentAccess) (Object) this).sac$getStackedContents();
		contents.clear();
		this.menu.fillCraftSlotsStackedContents(contents);
		this.recipesUpdated();
	}

	/**
	 * Local visibility only. Vanilla {@code setVisible} writes {@code ClientRecipeBook.setOpen}
	 * and sends a settings packet, which would leave the crafting-table book stuck open.
	 */
	@Override
	protected void setVisible(boolean visible) {
		RecipeBookComponentAccess access = (RecipeBookComponentAccess) (Object) this;
		if (visible) {
			access.sac$initVisuals();
		}
		access.sac$setVisible(visible);
		if (!visible) {
			access.sac$getRecipeBookPage().setInvisible();
		}
	}

	@Override
	protected void sendUpdateSettings() {
		// Filter clicks still update the local book flag via vanilla toggleFiltering.
		// Do not tell the server, or the world crafting table's book settings change.
	}

	public void setBookVisible(boolean visible) {
		setVisible(visible);
	}

	/**
	 * Called from the recipe-book mixin in place of {@code GameMode.handlePlaceRecipe},
	 * which always targets {@code player.containerMenu}.
	 */
	public boolean placeFromBook(RecipeCollection collection, RecipeDisplayId id) {
		if (!collection.isCraftable(id) && id.equals(this.lastPlaced)) {
			return false;
		}
		this.lastPlaced = id;
		((RecipeBookComponentAccess) (Object) this).sac$getGhostSlots().clear();
		if (!collection.isCraftable(id)) {
			for (RecipeDisplayEntry entry : collection.getRecipes()) {
				if (entry.id().equals(id)) {
					bridge().syncSlotPositions();
					fillGhostRecipe(entry.display());
					break;
				}
			}
		}
		ClientPacketDistributor.sendToServer(new PlaceCraftingRecipePayload(id.index(), Screen.hasShiftDown()));
		return true;
	}

	private static int computeStorageHash(StorageContainerMenuBase<?> menu) {
		int hash = 1;
		for (Slot slot : menu.slots) {
			ItemStack stack = slot.getItem();
			hash = 31 * hash + net.minecraft.core.registries.BuiltInRegistries.ITEM.getId(stack.getItem());
			hash = 31 * hash + stack.getCount();
			hash = 31 * hash + stack.getComponentsPatch().hashCode();
		}
		return hash;
	}
}
