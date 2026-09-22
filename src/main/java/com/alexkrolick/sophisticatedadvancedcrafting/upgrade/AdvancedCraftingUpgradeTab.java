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
 * floats beside the craft section and is clamped on-screen, flipping to the side with more
 * free horizontal space when needed.
 */
public class AdvancedCraftingUpgradeTab extends UpgradeSettingsTab<CraftingUpgradeContainer> {
	private static final int BOOK_PANEL_WIDTH = 147;
	private static final int BOOK_PANEL_HEIGHT = 166;
	private static final int BOOK_SCREEN_MARGIN = 3;
	private static final int BOOK_GAP = 4;
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
	 * Prefer the side of the craft section with more free horizontal space; clamp so the
	 * 147×166 panel stays fully on-screen with {@link #BOOK_SCREEN_MARGIN} px margin.
	 */
	private void computeBookAnchor() {
		Minecraft mc = Minecraft.getInstance();
		int screenW = mc.getWindow().getGuiScaledWidth();
		int screenH = mc.getWindow().getGuiScaledHeight();

		int craftLeft = x;
		int craftRight = x + craftSectionWidth;

		int freeLeft = craftLeft - BOOK_SCREEN_MARGIN;
		int freeRight = screenW - craftRight - BOOK_SCREEN_MARGIN;

		boolean placeLeft;
		if (freeLeft >= BOOK_PANEL_WIDTH && freeLeft >= freeRight) {
			placeLeft = true;
		} else if (freeRight >= BOOK_PANEL_WIDTH) {
			placeLeft = false;
		} else {
			placeLeft = freeLeft >= freeRight;
		}

		int desiredLeft = placeLeft
				? craftLeft - BOOK_GAP - BOOK_PANEL_WIDTH
				: craftRight + BOOK_GAP;
		bookLeft = Mth.clamp(desiredLeft, BOOK_SCREEN_MARGIN, Math.max(BOOK_SCREEN_MARGIN, screenW - BOOK_PANEL_WIDTH - BOOK_SCREEN_MARGIN));

		int desiredTop = y + BOOK_PREFERRED_Y_OFFSET;
		bookTop = Mth.clamp(desiredTop, BOOK_SCREEN_MARGIN, Math.max(BOOK_SCREEN_MARGIN, screenH - BOOK_PANEL_HEIGHT - BOOK_SCREEN_MARGIN));
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
		int gridLeft = x + craftingUIAddition.getWidth();
		recipeToggleButton.setPosition(gridLeft + 3, y + 42);
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

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isOpen && recipeToggleButton != null && recipeToggleButton.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		if (isOpen && bookVisible && recipeBook.isVisible() && recipeBook.mouseClicked(mouseX, mouseY, button)) {
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
