package com.alexkrolick.sophisticatedadvancedcrafting.upgrade;

import com.alexkrolick.sophisticatedadvancedcrafting.client.recipebook.DualSourceRecipeBookComponent;
import com.alexkrolick.sophisticatedadvancedcrafting.client.recipebook.DualSourceRecipeBookMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeSettingsTab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.ICraftingUIPart;

import static net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.GUI_CONTROLS;

/**
 * Advanced crafting upgrade tab embedding the vanilla green {@link RecipeBookComponent}.
 * Placement and craftability consider backpack storage and player inventory.
 * <p>
 * The craft grid stays in a compact tab (same footprint as stock crafting). The recipe book
 * floats beside the craft section and is re-anchored every tick so it never overlaps the craft
 * grid, result slot, or toggle buttons (category-tab overhang included in the collision box).
 */
public class AdvancedCraftingUpgradeTab extends UpgradeSettingsTab<CraftingUpgradeContainer> {
	private static final int BOOK_PANEL_WIDTH = 147;
	private static final int BOOK_PANEL_HEIGHT = 166;
	/** Category tabs overhang ~30px to the left of the 147px green panel. */
	private static final int BOOK_TAB_OVERHANG = 30;
	private static final int BOOK_SCREEN_MARGIN = 3;
	private static final int BOOK_GAP = 10;
	private static final int BOOK_PREFERRED_Y_OFFSET = 20;
	private static final TextureBlitData ARROW = new TextureBlitData(GUI_CONTROLS, new UV(97, 216), new Dimension(15, 8));

	private final ICraftingUIPart craftingUIAddition;
	private final DualSourceRecipeBookComponent recipeBook = new DualSourceRecipeBookComponent();
	private DualSourceRecipeBookMenu bridgeMenu;
	private ImageButton recipeToggleButton;
	private boolean bookVisible = true;
	private final int craftSectionWidth;
	private int bookLeft;
	private int bookTop;

	public AdvancedCraftingUpgradeTab(CraftingUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen,
			ButtonDefinition.Toggle<Boolean> shiftClickTargetButton, ButtonDefinition.Toggle<Boolean> refillCraftingGridButton,
			Component title, Component tooltip) {
		super(upgradeContainer, position, screen, title, tooltip);

		craftingUIAddition = screen.getCraftingUIAddition();
		craftSectionWidth = 63 + craftingUIAddition.getWidth();
		updateOpenDimensions();

		// Compact craft layout (same as stock CraftingUpgradeTab) — book floats separately.
		addHideableChild(new ToggleButton<>(new Position(x + 3, y + 24), shiftClickTargetButton,
				button -> getContainer().setShiftClickIntoStorage(!getContainer().shouldShiftClickIntoStorage()),
				getContainer()::shouldShiftClickIntoStorage));
		addHideableChild(new ToggleButton<>(new Position(x + 21, y + 24), refillCraftingGridButton,
				button -> getContainer().setRefillCraftingGrid(!getContainer().shouldRefillCraftingGrid()),
				getContainer()::shouldRefillCraftingGrid));
	}

	private void updateOpenDimensions() {
		// Tab stays compact whether or not the floating book is visible.
		openTabDimension = new Dimension(craftSectionWidth, 148);
	}

	@Override
	protected void onTabOpen() {
		super.onTabOpen();
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && screen.getMenu() instanceof StorageContainerMenuBase<?> storageMenu) {
			bridgeMenu = new DualSourceRecipeBookMenu(storageMenu, getContainer());
			if (bookVisible) {
				initRecipeBook();
			}
		}
		ensureRecipeToggleButton();
		repositionRecipeToggle();
	}

	@Override
	protected void onTabClose() {
		super.onTabClose();
		craftingUIAddition.onCraftingSlotsHidden();
		recipeBook.setBookVisible(false);
		bridgeMenu = null;
	}

	private void ensureRecipeToggleButton() {
		if (recipeToggleButton == null) {
			recipeToggleButton = new ImageButton(0, 0, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, btn -> toggleBook());
		}
	}

	private void toggleBook() {
		bookVisible = !bookVisible;
		updateOpenDimensions();
		if (isOpen) {
			setWidth(Math.max(openTabDimension.width(), 21));
			setHeight(openTabDimension.height());
		}
		if (bookVisible) {
			initRecipeBook();
		} else {
			recipeBook.setBookVisible(false);
		}
		moveSlotsToTab();
		repositionRecipeToggle();
	}

	/**
	 * Place the green recipe book so it NEVER overlaps the craft section (3×3 grid + result +
	 * toggle buttons). Collision box includes the ~30px category-tab overhang on the left of
	 * the 147px panel. Prefer left, then right, then above, then below; never clamp into the
	 * craft exclusion rect.
	 */
	private void computeBookAnchor() {
		Minecraft mc = Minecraft.getInstance();
		int screenW = mc.getWindow().getGuiScaledWidth();
		int screenH = mc.getWindow().getGuiScaledHeight();

		// Hard exclusion: entire open craft tab (grid, result slot, toggle buttons).
		int excludeLeft = x;
		int excludeTop = y;
		int excludeRight = x + craftSectionWidth;
		int excludeBottom = y + openTabDimension.height();

		int panelW = BOOK_PANEL_WIDTH;
		int panelH = BOOK_PANEL_HEIGHT;

		int minLeft = BOOK_SCREEN_MARGIN + BOOK_TAB_OVERHANG; // keep tabs on-screen too
		int maxLeft = Math.max(minLeft, screenW - panelW - BOOK_SCREEN_MARGIN);
		int minTop = BOOK_SCREEN_MARGIN;
		int maxTop = Math.max(minTop, screenH - panelH - BOOK_SCREEN_MARGIN);

		record Candidate(int left, int top, int priority) {}
		java.util.ArrayList<Candidate> candidates = new java.util.ArrayList<>();

		// Left of craft: panel sits left; tabs overhang further left (away from craft).
		candidates.add(new Candidate(excludeLeft - BOOK_GAP - panelW, excludeTop + BOOK_PREFERRED_Y_OFFSET, 0));
		// Right of craft: tabs overhang LEFT toward craft — push panel further right by overhang.
		candidates.add(new Candidate(excludeRight + BOOK_GAP + BOOK_TAB_OVERHANG, excludeTop + BOOK_PREFERRED_Y_OFFSET, 1));
		// Above / below: center-ish on craft section; tabs hang left of panel.
		int aboveBelowLeft = excludeLeft + BOOK_TAB_OVERHANG;
		candidates.add(new Candidate(aboveBelowLeft, excludeTop - BOOK_GAP - panelH, 2));
		candidates.add(new Candidate(aboveBelowLeft, excludeBottom + BOOK_GAP, 3));

		Candidate best = null;
		for (Candidate c : candidates) {
			int left = Mth.clamp(c.left, minLeft, maxLeft);
			int top = Mth.clamp(c.top, minTop, maxTop);
			// Collision rect includes tab overhang to the left of the panel.
			int colLeft = left - BOOK_TAB_OVERHANG;
			int colRight = left + panelW;
			int colTop = top;
			int colBottom = top + panelH;
			if (rectsOverlap(colLeft, colTop, colRight, colBottom, excludeLeft, excludeTop, excludeRight, excludeBottom)) {
				continue;
			}
			if (best == null || c.priority < best.priority) {
				best = new Candidate(left, top, c.priority);
			}
		}

		if (best != null) {
			bookLeft = best.left;
			bookTop = best.top;
			return;
		}

		// Last resort: no non-overlapping placement fits — leave previous anchor;
		// caller may hide via toggle. Prefer not mutating visibility mid-tick.
		bookLeft = Mth.clamp(bookLeft, minLeft, maxLeft);
		bookTop = Mth.clamp(bookTop, minTop, maxTop);
		int colLeft = bookLeft - BOOK_TAB_OVERHANG;
		if (rectsOverlap(colLeft, bookTop, bookLeft + panelW, bookTop + panelH,
				excludeLeft, excludeTop, excludeRight, excludeBottom)) {
			// Force off-screen parking above viewport so it cannot cover the grid.
			bookTop = -panelH - BOOK_GAP;
		}
	}

	private static boolean rectsOverlap(int aL, int aT, int aR, int aB, int bL, int bT, int bR, int bB) {
		return aL < bR && aR > bL && aT < bB && aB > bT;
	}

	private void initRecipeBook() {
		Minecraft mc = Minecraft.getInstance();
		if (bridgeMenu == null || mc.player == null) {
			return;
		}
		computeBookAnchor();
		recipeBook.initAnchored(mc, bridgeMenu, bookLeft, bookTop);
	}

	private void repositionRecipeToggle() {
		if (recipeToggleButton == null) {
			return;
		}
		// Sit with shift-click / refill toggles above the grid — never over craft slots (y+44).
		recipeToggleButton.setPosition(x + 39, y + 24);
	}

	@Override
	public void tick() {
		if (isOpen && bookVisible && recipeBook.isVisible()) {
			computeBookAnchor();
			recipeBook.reanchor(bookLeft, bookTop);
			recipeBook.tick();
			if (bridgeMenu != null) {
				bridgeMenu.syncSlotPositions();
			}
		}
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, Minecraft minecraft, int mouseX, int mouseY) {
		super.renderBg(guiGraphics, minecraft, mouseX, mouseY);
		if (!getContainer().isOpen()) {
			return;
		}
		int gridLeft = x + craftingUIAddition.getWidth();
		GuiHelper.renderSlotsBackground(guiGraphics, gridLeft + 3, y + 44, 3, 3);
		GuiHelper.blit(guiGraphics, gridLeft + 3 + 19, y + 101, ARROW);
		GuiHelper.blit(guiGraphics, gridLeft + 3 + 14, y + 111, GuiHelper.CRAFTING_RESULT_SLOT);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		if (!getContainer().isOpen()) {
			return;
		}
		repositionRecipeToggle();
		if (recipeToggleButton != null) {
			recipeToggleButton.render(guiGraphics, mouseX, mouseY, partialTicks);
		}
		if (bookVisible && recipeBook.isVisible()) {
			recipeBook.render(guiGraphics, mouseX, mouseY, partialTicks);
			recipeBook.renderGhostRecipe(guiGraphics, screen.getGuiLeft(), screen.getGuiTop(), true, partialTicks);
		}
	}

	@Override
	public void renderTooltip(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		super.renderTooltip(screen, guiGraphics, mouseX, mouseY);
		if (isOpen && bookVisible && recipeBook.isVisible()) {
			recipeBook.renderTooltip(guiGraphics, this.screen.getGuiLeft(), this.screen.getGuiTop(), mouseX, mouseY);
		}
	}

	/**
	 * Screen-level mouse forwarding for the floating book (may sit outside this tab's widget bounds).
	 * Does not route through {@link #mouseClicked} so craft-grid slot clicks are not stolen.
	 */
	public boolean handleRecipeBookMouseClicked(double mouseX, double mouseY, int button) {
		return isOpen && bookVisible && recipeBook.isVisible() && recipeBook.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isOpen && recipeToggleButton != null && recipeToggleButton.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		if (handleRecipeBookMouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	public boolean handleKeyPressed(int keyCode, int scanCode, int modifiers) {
		return isOpen && bookVisible && recipeBook.isVisible() && recipeBook.keyPressed(keyCode, scanCode, modifiers);
	}

	public boolean handleCharTyped(char codePoint, int modifiers) {
		return isOpen && bookVisible && recipeBook.isVisible() && recipeBook.charTyped(codePoint, modifiers);
	}

	@Override
	protected void moveSlotsToTab() {
		int gridLeftOffset = craftingUIAddition.getWidth();
		int slotNumber = 0;
		for (Slot slot : getContainer().getSlots()) {
			slot.x = x + 3 + gridLeftOffset - screen.getGuiLeft() + 1 + (slotNumber % 3) * 18;
			slot.y = y + 44 - screen.getGuiTop() + 1 + (slotNumber / 3) * 18;
			slotNumber++;
			if (slotNumber >= 9) {
				break;
			}
		}

		Slot craftingResult = getContainer().getSlots().get(9);
		craftingResult.x = x + 3 + gridLeftOffset - screen.getGuiLeft() + 19;
		craftingResult.y = y + 44 - screen.getGuiTop() + 72;

		craftingUIAddition.onCraftingSlotsDisplayed(getContainer().getSlots());
		if (bridgeMenu != null) {
			bridgeMenu.syncSlotPositions();
		}
	}
}
