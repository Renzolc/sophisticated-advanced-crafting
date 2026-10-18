package dev.renzo.sophisticatedadvancedcrafting.client.recipebook;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Client-only crafting menu so the vanilla recipe book can highlight recipes for the upgrade tab.
 * It is never installed as {@code player.containerMenu}. Ghost-recipe slots mirror the upgrade grid.
 */
public class DualSourceRecipeBookMenu extends AbstractCraftingMenu {
	private final StorageContainerMenuBase<?> storageMenu;
	private final CraftingUpgradeContainer crafting;

	public DualSourceRecipeBookMenu(StorageContainerMenuBase<?> storageMenu, CraftingUpgradeContainer crafting) {
		super(MenuType.CRAFTING, -1, 3, 3);
		this.storageMenu = storageMenu;
		this.crafting = crafting;

		List<Slot> upgradeSlots = crafting.getSlots();
		addSlot(new Slot(this.resultSlots, 0, upgradeSlots.get(9).x, upgradeSlots.get(9).y));
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				int gridIndex = col + row * 3;
				Slot source = upgradeSlots.get(gridIndex);
				addSlot(new Slot(this.craftSlots, gridIndex, source.x, source.y));
			}
		}
	}

	public StorageContainerMenuBase<?> getStorageMenu() {
		return storageMenu;
	}

	public CraftingUpgradeContainer getCraftingContainer() {
		return crafting;
	}

	/** Keep ghost-recipe coordinates aligned with the upgrade tab's moved slots. */
	public void syncSlotPositions() {
		List<Slot> upgradeSlots = crafting.getSlots();
		if (upgradeSlots.size() <= 9 || slots.size() <= 9) {
			return;
		}
		slots.get(0).x = upgradeSlots.get(9).x;
		slots.get(0).y = upgradeSlots.get(9).y;
		for (int i = 0; i < 9; i++) {
			slots.get(i + 1).x = upgradeSlots.get(i).x;
			slots.get(i + 1).y = upgradeSlots.get(i).y;
		}
	}

	/**
	 * Count storage slots and the craft grid once. Player inventory slots that live on the storage
	 * menu are included here; the recipe book component must not also call
	 * {@code Inventory.fillStackedContents} or those stacks would be counted twice.
	 */
	@Override
	public void fillCraftSlotsStackedContents(StackedItemContents contents) {
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
		for (int i = 0; i < 9; i++) {
			ItemStack stack = crafting.getCraftMatrix().getItem(i);
			if (!stack.isEmpty()) {
				contents.accountSimpleStack(stack);
			}
		}
	}

	@Override
	public Slot getResultSlot() {
		return slots.get(0);
	}

	@Override
	public List<Slot> getInputGridSlots() {
		return slots.subList(1, Math.min(10, slots.size()));
	}

	@Override
	protected Player owner() {
		return Minecraft.getInstance().player;
	}

	@Override
	public RecipeBookType getRecipeBookType() {
		return RecipeBookType.CRAFTING;
	}

	@Override
	public boolean stillValid(Player player) {
		return storageMenu.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}
}
