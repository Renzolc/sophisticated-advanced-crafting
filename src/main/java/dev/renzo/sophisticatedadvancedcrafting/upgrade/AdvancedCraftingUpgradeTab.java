package dev.renzo.sophisticatedadvancedcrafting.upgrade;

import dev.renzo.sophisticatedadvancedcrafting.client.recipebook.DualSourceRecipeBookComponent;
import dev.renzo.sophisticatedadvancedcrafting.client.recipebook.DualSourceRecipeBookMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.ImageButton;
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
 * floats in free space outside the entire storage screen content area (backpack slots + player
 * inventory + hotbar + craft tab) and is re-anchored every tick. Category tabs may hang off-screen.
 * If no clear spot exists, the panel is clamped to the least-overlap position so the book still opens.
 */
public class AdvancedCraftingUpgradeTab extends UpgradeSettingsTab<CraftingUpgradeContainer> {
	private static final int BOOK_PANEL_WIDTH = 147;
	private static final int BOOK_PANEL_HEIGHT = 166;
	/** Category tabs overhang ~30px to the left of the 147px green panel. */
	private static final int BOOK_TAB_OVERHANG = 30;
	private static final int BOOK_SCREEN_MARGIN = 3;
	/** Gap between book collision box and the full storage GUI exclusion rect. */
	private static final int BOOK_GAP = 16;
	private static final int BOOK_PREFERRED_Y_OFFSET = 20;
	private static final TextureBlitData ARROW = new TextureBlitData(GUI_CONTROLS, new UV(97, 216), new Dimension(15, 8));

	private final ICraftingUIPart craftingUIAddition;
	private DualSourceRecipeBookComponent recipeBook;
	private DualSourceRecipeBookMenu bridgeMenu;
	private ImageButton recipeToggleButton;
	/** Closed by default so opening the upgrade tab does not slam the book onto the GUI. */
	private boolean bookVisible = false;
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
			recipeBook = new DualSourceRecipeBookComponent(bridgeMenu);
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
		if (recipeBook != null) {
			recipeBook.setBookVisible(false);
		}
		recipeBook = null;
		bridgeMenu = null;
	}

	private void ensureRecipeToggleButton() {
		if (recipeToggleButton == null) {
			recipeToggleButton = new ImageButton(0, 0, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, btn -> toggleBook());
		}
	}

	private void toggleBook() {
		if (recipeBook == null) {
			return;
		}
		if (!bookVisible) {
			if (!computeBookAnchor()) {
				bookVisible = false;
				recipeBook.setBookVisible(false);
				return;
			}
			bookVisible = true;
			updateOpenDimensions();
			if (isOpen) {
				setWidth(Math.max(openTabDimension.width(), 21));
				setHeight(openTabDimension.height());
			}
			initRecipeBook();
		} else {
			bookVisible = false;
			updateOpenDimensions();
			if (isOpen) {
				setWidth(Math.max(openTabDimension.width(), 21));
				setHeight(openTabDimension.height());
			}
			recipeBook.setBookVisible(false);
		}
		moveSlotsToTab();
		repositionRecipeToggle();
	}

	/**
	 * Place the green recipe book outside the storage screen (backpack slots, player inventory,
	 * hotbar, and the open craft tab). Left is preferred so the book is not under JEI.
	 * <p>
	 * Category tabs may hang off the screen edge. On a short GUI (auto scale on an ultrawide)
	 * that is the only way to sit the 147×166 panel beside a full-height backpack without covering
	 * slots. If even that cannot fit, the panel is clamped on screen at the spot that covers the
	 * least GUI, so the button still opens a usable book.
	 */
	private boolean computeBookAnchor() {
		Minecraft mc = Minecraft.getInstance();
		int screenW = mc.getWindow().getGuiScaledWidth();
		int screenH = mc.getWindow().getGuiScaledHeight();

		int excludeLeft = screen.getLeftPos();
		int excludeTop = screen.getTopPos();
		int excludeRight = Math.max(screen.getLeftPos() + screen.getImageWidth(), x + Math.max(getWidth(), craftSectionWidth));
		int excludeBottom = Math.max(screen.getTopPos() + screen.getImageHeight(), y + openTabDimension.height());
		excludeTop = Math.min(excludeTop, y);

		int panelW = BOOK_PANEL_WIDTH;
		int panelH = BOOK_PANEL_HEIGHT;
		int minLeft = BOOK_SCREEN_MARGIN;
		int maxLeft = screenW - panelW - BOOK_SCREEN_MARGIN;
		if (maxLeft < minLeft) {
			maxLeft = minLeft;
		}
		int minTop = BOOK_SCREEN_MARGIN;
		int maxTop = screenH - panelH - BOOK_SCREEN_MARGIN;
		if (maxTop < minTop) {
			maxTop = minTop;
		}

		record Candidate(int left, int top, int priority) {}
		java.util.ArrayList<Candidate> candidates = new java.util.ArrayList<>();
		int alignedTop = excludeTop + BOOK_PREFERRED_Y_OFFSET;
		// Left of the whole GUI. Panel sits in the gap; category tabs hang further left.
		candidates.add(new Candidate(excludeLeft - BOOK_GAP - panelW, alignedTop, 0));
		// Right is the JEI side — only used when the left cannot fit.
		candidates.add(new Candidate(excludeRight + BOOK_GAP + BOOK_TAB_OVERHANG, alignedTop, 1));
		int aboveBelowLeft = excludeLeft + Math.max(0, (excludeRight - excludeLeft - panelW) / 2);
		candidates.add(new Candidate(aboveBelowLeft, excludeTop - BOOK_GAP - panelH, 2));
		candidates.add(new Candidate(aboveBelowLeft, excludeBottom + BOOK_GAP, 3));

		Candidate bestClear = null;
		Candidate bestAny = null;
		int bestOverlap = Integer.MAX_VALUE;
		for (Candidate c : candidates) {
			int left = Mth.clamp(c.left, minLeft, maxLeft);
			int top = Mth.clamp(c.top, minTop, maxTop);
			int overlap = bookOverlapArea(left, top, panelW, panelH, excludeLeft, excludeTop, excludeRight, excludeBottom);
			if (overlap == 0 && (bestClear == null || c.priority < bestClear.priority)) {
				bestClear = new Candidate(left, top, c.priority);
			}
			if (bestAny == null || overlap < bestOverlap || (overlap == bestOverlap && c.priority < bestAny.priority)) {
				bestOverlap = overlap;
				bestAny = new Candidate(left, top, c.priority);
			}
		}

		Candidate chosen = bestClear != null ? bestClear : bestAny;
		if (chosen == null) {
			return false;
		}
		bookLeft = chosen.left;
		bookTop = chosen.top;
		return true;
	}

	/** Overlap of the green panel (not the off-screen category tabs) with the storage GUI. */
	private static int bookOverlapArea(int left, int top, int panelW, int panelH, int exL, int exT, int exR, int exB) {
		int overlapLeft = Math.max(left, exL);
		int overlapTop = Math.max(top, exT);
		int overlapRight = Math.min(left + panelW, exR);
		int overlapBottom = Math.min(top + panelH, exB);
		if (overlapLeft >= overlapRight || overlapTop >= overlapBottom) {
			return 0;
		}
		return (overlapRight - overlapLeft) * (overlapBottom - overlapTop);
	}

	private void initRecipeBook() {
		Minecraft mc = Minecraft.getInstance();
		if (bridgeMenu == null || recipeBook == null || mc.player == null) {
			return;
		}
		if (!computeBookAnchor()) {
			bookVisible = false;
			recipeBook.setBookVisible(false);
			return;
		}
		recipeBook.initAnchored(mc, bookLeft, bookTop);
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
		if (!isOpen || !bookVisible || recipeBook == null) {
			return;
		}
		if (!computeBookAnchor()) {
			// Lost a valid placement (resize / JEI) — close rather than overlap inventory.
			bookVisible = false;
			recipeBook.setBookVisible(false);
			return;
		}
		if (recipeBook.isVisible()) {
			recipeBook.reanchor(bookLeft, bookTop);
			recipeBook.tick();
			if (bridgeMenu != null) {
				bridgeMenu.syncSlotPositions();
			}
		}
	}

	@Override
	protected void extractBg(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, int mouseX, int mouseY) {
		super.extractBg(guiGraphics, minecraft, mouseX, mouseY);
		if (!getContainer().isOpen()) {
			return;
		}
		int gridLeft = x + craftingUIAddition.getWidth();
		GuiHelper.renderSlotsBackground(guiGraphics, gridLeft + 3, y + 44, 3, 3);
		GuiHelper.blit(guiGraphics, gridLeft + 3 + 19, y + 101, ARROW);
		GuiHelper.blit(guiGraphics, gridLeft + 3 + 14, y + 111, GuiHelper.CRAFTING_RESULT_SLOT);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		if (!getContainer().isOpen()) {
			return;
		}
		repositionRecipeToggle();
		if (recipeToggleButton != null) {
			recipeToggleButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		}
	}

	/**
	 * Drawn from {@code ScreenEvent.Render.Post}. The upgrade tab itself is rendered before
	 * backpack slots, so drawing the book there puts slots and JEI-adjacent UI on top of it.
	 */
	public void renderRecipeBook(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (recipeBook == null || !isOpen || !getContainer().isOpen() || !bookVisible || !recipeBook.isVisible()) {
			return;
		}
		recipeBook.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		// Ghost slots use upgrade-grid coordinates, which are relative to the storage screen origin.
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(screen.getLeftPos(), screen.getTopPos());
		recipeBook.extractGhostRecipe(guiGraphics, true);
		guiGraphics.pose().popMatrix();
		recipeBook.extractTooltip(guiGraphics, mouseX, mouseY, null);
	}

	/**
	 * Screen-level mouse forwarding for the floating book (may sit outside this tab's widget bounds).
	 * Does not route through {@link #mouseClicked} so craft-grid slot clicks are not stolen.
	 */
	public boolean handleRecipeBookMouseClicked(MouseButtonEvent event, boolean doubleClick) {
		return recipeBook != null && isOpen && bookVisible && recipeBook.isVisible() && recipeBook.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (isOpen && recipeToggleButton != null && recipeToggleButton.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (handleRecipeBookMouseClicked(event, doubleClick)) {
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	public boolean handleKeyPressed(KeyEvent event) {
		return recipeBook != null && isOpen && bookVisible && recipeBook.isVisible() && recipeBook.keyPressed(event);
	}

	public boolean handleCharTyped(CharacterEvent event) {
		return recipeBook != null && isOpen && bookVisible && recipeBook.isVisible() && recipeBook.charTyped(event);
	}

	@Override
	protected void moveSlotsToTab() {
		// Only craft-grid / result slots move — never player inventory, hotbar, or storage slots.
		int gridLeftOffset = craftingUIAddition.getWidth();
		int slotNumber = 0;
		for (Slot slot : getContainer().getSlots()) {
			slot.x = x + 3 + gridLeftOffset - screen.getLeftPos() + 1 + (slotNumber % 3) * 18;
			slot.y = y + 44 - screen.getTopPos() + 1 + (slotNumber / 3) * 18;
			slotNumber++;
			if (slotNumber >= 9) {
				break;
			}
		}

		Slot craftingResult = getContainer().getSlots().get(9);
		craftingResult.x = x + 3 + gridLeftOffset - screen.getLeftPos() + 19;
		craftingResult.y = y + 44 - screen.getTopPos() + 72;

		craftingUIAddition.onCraftingSlotsDisplayed(getContainer().getSlots());
		if (bridgeMenu != null) {
			bridgeMenu.syncSlotPositions();
		}
	}
}
