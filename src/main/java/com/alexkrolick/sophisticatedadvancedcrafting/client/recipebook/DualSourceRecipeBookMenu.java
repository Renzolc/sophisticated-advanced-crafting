package com.alexkrolick.sophisticatedadvancedcrafting.client.recipebook;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;

import java.util.List;

/**
 * Client-only {@link RecipeBookMenu} adapter so vanilla {@link net.minecraft.client.gui.screens.recipebook.RecipeBookComponent}
 * can drive craftability / ghost highlights for the backpack crafting upgrade.
 * Slot layout matches {@link net.minecraft.world.inventory.CraftingMenu}: result at 0, grid at 1–9.
 */
public class DualSourceRecipeBookMenu extends RecipeBookMenu<CraftingInput, CraftingRecipe> {
	private final StorageContainerMenuBase<?> storageMenu;
	private final CraftingUpgradeContainer crafting;
	private final CraftingContainer mirrorCraftSlots;
	private final ResultContainer mirrorResult = new ResultContainer();

	public DualSourceRecipeBookMenu(StorageContainerMenuBase<?> storageMenu, CraftingUpgradeContainer crafting) {
		super(MenuType.CRAFTING, storageMenu.containerId);
		this.storageMenu = storageMenu;
		this.crafting = crafting;
		this.mirrorCraftSlots = new TransientCraftingContainer(this, 3, 3);

		List<Slot> upgradeSlots = crafting.getSlots();
		// Index 0 = result (upgrade slot 9)
		addSlot(new MirrorSlot(mirrorResult, 0, upgradeSlots.get(9)));
		// Indexes 1–9 = craft grid (upgrade slots 0–8)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				int gridIndex = col + row * 3;
				addSlot(new MirrorSlot(mirrorCraftSlots, gridIndex, upgradeSlots.get(gridIndex)));
			}
		}
	}

	public StorageContainerMenuBase<?> getStorageMenu() {
		return storageMenu;
	}

	public CraftingUpgradeContainer getCraftingContainer() {
		return crafting;
	}

	/** Keep ghost-recipe coordinates aligned with the upgrade tab's moved slots.
	 * Writes only mirror slot x/y — never mutates real storage / player inventory / hotbar slots. */
	public void syncSlotPositions() {
		List<Slot> upgradeSlots = crafting.getSlots();
		slots.get(0).x = upgradeSlots.get(9).x;
		slots.get(0).y = upgradeSlots.get(9).y;
		for (int i = 0; i < 9; i++) {
			slots.get(i + 1).x = upgradeSlots.get(i).x;
			slots.get(i + 1).y = upgradeSlots.get(i).y;
		}
	}

	@Override
	public void fillCraftSlotsStackedContents(StackedContents itemHelper) {
		for (int i = 0; i < 9; i++) {
			itemHelper.accountSimpleStack(crafting.getCraftMatrix().getItem(i));
		}
	}

	@Override
	public void clearCraftingContent() {
		// Dual-source transfer clears/fills the real upgrade grid; no-op here.
	}

	@Override
	public boolean recipeMatches(RecipeHolder<CraftingRecipe> recipe) {
		CraftingInput input = buildInputFromUpgrade();
		Player player = net.minecraft.client.Minecraft.getInstance().player;
		return recipe.value().matches(input, player.level());
	}

	private CraftingInput buildInputFromUpgrade() {
		return CraftingInput.of(3, 3,
				java.util.stream.IntStream.range(0, 9).mapToObj(i -> crafting.getCraftMatrix().getItem(i)).toList());
	}

	@Override
	public int getResultSlotIndex() {
		return 0;
	}

	@Override
	public int getGridWidth() {
		return 3;
	}

	@Override
	public int getGridHeight() {
		return 3;
	}

	@Override
	public int getSize() {
		return 10;
	}

	@Override
	public RecipeBookType getRecipeBookType() {
		return RecipeBookType.CRAFTING;
	}

	@Override
	public boolean shouldMoveToInventory(int slotIndex) {
		return slotIndex != getResultSlotIndex();
	}

	@Override
	public boolean stillValid(Player player) {
		return storageMenu.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	/** Slot whose only job is to expose x/y for ghost rendering. */
	private static final class MirrorSlot extends Slot {
		private final Slot source;

		MirrorSlot(net.minecraft.world.Container container, int slot, Slot source) {
			super(container, slot, source.x, source.y);
			this.source = source;
		}

		void syncFromSource() {
			this.x = source.x;
			this.y = source.y;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}
	}
}
