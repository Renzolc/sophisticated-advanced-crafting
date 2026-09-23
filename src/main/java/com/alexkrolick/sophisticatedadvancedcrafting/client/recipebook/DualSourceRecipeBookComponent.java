package com.alexkrolick.sophisticatedadvancedcrafting.client.recipebook;

import com.alexkrolick.sophisticatedadvancedcrafting.network.PlaceCraftingRecipePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Vanilla green recipe book that:
 * <ul>
 *   <li>does not replace {@code player.containerMenu}</li>
 *   <li>does not mutate vanilla {@link net.minecraft.client.ClientRecipeBook} open state</li>
 *   <li>uses {@link DualSourceRecipeButton} so recipe buttons never cast
 *       {@code BackpackContainer} to {@code RecipeBookMenu}</li>
 *   <li>counts backpack storage + player inventory for craftability</li>
 *   <li>places via {@link PlaceCraftingRecipePayload} (dual-source server transfer)</li>
 * </ul>
 */
public class DualSourceRecipeBookComponent extends RecipeBookComponent {
	private static final Component ALL_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.all");

	private DualSourceRecipeBookMenu bridgeMenu;
	private int bookLeft;
	private int bookTop;
	private int lastStorageHash;

	public DualSourceRecipeBookComponent() {
		// Replace vanilla RecipeButtons that cast player.containerMenu with ones that
		// read our DualSourceRecipeBookMenu (this.menu) instead.
		this.recipeBookPage.buttons.clear();
		for (int i = 0; i < 20; i++) {
			this.recipeBookPage.buttons.add(new DualSourceRecipeButton(() -> this.menu));
		}
	}

	/**
	 * Initialize / re-anchor the book so its panel top-left is at ({@code bookLeft}, {@code bookTop}).
	 * Never calls {@link #init} (which would replace {@code player.containerMenu}).
	 * Vanilla centers using {@code (width - 147) / 2 - xOffset} with {@code xOffset = 86}.
	 */
	public void initAnchored(Minecraft minecraft, DualSourceRecipeBookMenu menu, int bookLeft, int bookTop) {
		this.bridgeMenu = menu;
		this.bookLeft = bookLeft;
		this.bookTop = bookTop;

		int width = 2 * (bookLeft + 86) + 147;
		int height = 2 * bookTop + 166;

		this.minecraft = minecraft;
		this.width = width;
		this.height = height;
		this.menu = menu;
		this.widthTooNarrow = false;
		this.book = minecraft.player.getRecipeBook();
		this.timesInventoryChanged = minecraft.player.getInventory().getTimesChanged();
		this.visible = true;
		this.initVisuals();

		menu.syncSlotPositions();
		refillDualSourceContents();
		this.lastStorageHash = computeStorageHash(menu.getStorageMenu());
	}

	/**
	 * Safety: never install the bridge menu as {@code player.containerMenu}.
	 * Callers must use {@link #initAnchored} instead.
	 */
	@Override
	public void init(int width, int height, Minecraft minecraft, boolean widthTooNarrow, net.minecraft.world.inventory.RecipeBookMenu<?, ?> menu) {
		throw new UnsupportedOperationException("Use initAnchored — vanilla init replaces player.containerMenu");
	}

	public void reanchor(int bookLeft, int bookTop) {
		if (this.minecraft == null || this.bridgeMenu == null) {
			return;
		}
		if (this.bookLeft == bookLeft && this.bookTop == bookTop && isVisible()) {
			this.bridgeMenu.syncSlotPositions();
			return;
		}
		initAnchored(this.minecraft, this.bridgeMenu, bookLeft, bookTop);
	}

	private void refillDualSourceContents() {
		if (this.minecraft == null || this.bridgeMenu == null) {
			return;
		}
		StackedContents contents = this.stackedContents;
		contents.clear();
		this.minecraft.player.getInventory().fillStackedContents(contents);
		accountStorageSlots(this.bridgeMenu.getStorageMenu(), this.bridgeMenu.getCraftingContainer(), contents);
		this.menu.fillCraftSlotsStackedContents(contents);
		this.updateCollections(false);
	}

	static void accountStorageSlots(StorageContainerMenuBase<?> storageMenu, CraftingUpgradeContainer crafting, StackedContents contents) {
		Set<Slot> excluded = new HashSet<>(crafting.getRecipeSlots());
		List<Slot> upgradeSlots = crafting.getSlots();
		if (upgradeSlots.size() > 9) {
			excluded.add(upgradeSlots.get(9));
		}
		for (Slot slot : storageMenu.slots) {
			if (!slot.isActive() || excluded.contains(slot)) {
				continue;
			}
			ItemStack stack = slot.getItem();
			if (!stack.isEmpty()) {
				contents.accountSimpleStack(stack);
			}
		}
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

	@Override
	public void tick() {
		if (!isVisible() || this.minecraft == null || this.bridgeMenu == null) {
			return;
		}
		// Own the visibility toggle — do not call super.tick() (syncs from book.isOpen).
		if (this.timesInventoryChanged != this.minecraft.player.getInventory().getTimesChanged()) {
			this.timesInventoryChanged = this.minecraft.player.getInventory().getTimesChanged();
			refillDualSourceContents();
			this.lastStorageHash = computeStorageHash(this.bridgeMenu.getStorageMenu());
			return;
		}
		int storageHash = computeStorageHash(this.bridgeMenu.getStorageMenu());
		if (storageHash != this.lastStorageHash) {
			this.lastStorageHash = storageHash;
			refillDualSourceContents();
		}
	}

	/**
	 * Local visibility only — never touches {@code ClientRecipeBook.setOpen} or
	 * {@link #sendUpdateSettings}, which would mark CRAFTING as GUI-open globally.
	 */
	@Override
	protected void setVisible(boolean visible) {
		if (visible) {
			this.initVisuals();
		}
		this.visible = visible;
		if (!visible) {
			this.recipeBookPage.setInvisible();
		}
	}

	/**
	 * Never call {@code super.mouseClicked}: vanilla places via
	 * {@code gameMode.handlePlaceRecipe(player.containerMenu)} and can toggle visibility /
	 * book settings. Keep search / tab / filter handling without those side effects.
	 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!isVisible() || this.minecraft == null || this.minecraft.player.isSpectator()) {
			return false;
		}

		int panelX = (this.width - 147) / 2 - this.xOffset;
		int panelY = (this.height - 166) / 2;

		if (this.recipeBookPage.mouseClicked(mouseX, mouseY, button, panelX, panelY, 147, 166)) {
			RecipeHolder<?> recipe = this.recipeBookPage.getLastClickedRecipe();
			RecipeCollection collection = this.recipeBookPage.getLastClickedRecipeCollection();
			if (recipe != null && collection != null) {
				if (!collection.isCraftable(recipe) && this.ghostRecipe.getRecipe() == recipe) {
					return false;
				}
				this.ghostRecipe.clear();
				boolean maxTransfer = Screen.hasShiftDown();
				if (!collection.isCraftable(recipe)) {
					this.setupGhostRecipe(recipe, this.menu.slots);
				}
				PacketDistributor.sendToServer(new PlaceCraftingRecipePayload(recipe.id(), maxTransfer));
			}
			return true;
		}

		if (this.searchBox != null && this.searchBox.mouseClicked(mouseX, mouseY, button)) {
			this.searchBox.setFocused(true);
			return true;
		}
		if (this.searchBox != null) {
			this.searchBox.setFocused(false);
		}

		if (this.filterButton != null && this.filterButton.mouseClicked(mouseX, mouseY, button)) {
			boolean filtering = toggleFilteringLocal();
			this.filterButton.setStateTriggered(filtering);
			this.filterButton.setTooltip(filtering
					? Tooltip.create(this.getRecipeFilterName())
					: Tooltip.create(ALL_RECIPES_TOOLTIP));
			this.updateCollections(false);
			return true;
		}

		for (RecipeBookTabButton tab : this.tabButtons) {
			if (tab.mouseClicked(mouseX, mouseY, button)) {
				if (this.selectedTab != tab) {
					if (this.selectedTab != null) {
						this.selectedTab.setStateTriggered(false);
					}
					this.selectedTab = tab;
					this.selectedTab.setStateTriggered(true);
					this.updateCollections(true);
				}
				return true;
			}
		}

		return false;
	}

	private boolean toggleFilteringLocal() {
		RecipeBookType type = this.menu.getRecipeBookType();
		boolean filtering = !this.book.isFiltering(type);
		this.book.setFiltering(type, filtering);
		return filtering;
	}

	public void setBookVisible(boolean visible) {
		setVisible(visible);
	}

	public DualSourceRecipeBookMenu getBridgeMenu() {
		return bridgeMenu;
	}
}
