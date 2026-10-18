# Port notes

## 1.21.4

NeoForge 21.4.158, Java 21, Parchment 1.21.4 / 2025.03.23, ModDevGradle 2.0.147.
Sophisticated Core 1.21.4-1.5.1.2350 (mods.toml version 1.5.1, NeoForge `[21.4.0,)`),
Backpacks 1.21.4-3.27.2.2166 (version 3.27.2), Storage 1.21.4-1.6.0.2138 (version 1.6.0).
No NeoForge bump was required.

- Items register with `DeferredRegister.Items.registerItem`. `CraftingUpgradeItem` now takes `Item.Properties`.
- Item models live in `assets/<modid>/items/*.json` as `minecraft:model`.
- Shaped recipe keys are bare item ids.
- `Slot.x` / `Slot.y` are public final. One access transformer removes final so the upgrade tab can move the craft grid. The 1.21.1 recipe-book access transformers targeted members that are private or gone; those entries were removed.
- The recipe book is server-authoritative. Clicks carry a `RecipeDisplayId` (an int index). The server resolves it with `RecipeManager.getRecipeFromDisplay` and places through Core `setItemsWithStacks(ResourceKey, ...)`.
- `RecipeBookMenu` is no longer generic. `AbstractCraftingMenu` owns the 3×3 grid. `StackedItemContents` replaced `StackedContents` for recipe accounting. `PlaceRecipe` is `PlaceRecipeHelper`. `Ingredient.getItems()` is `items()`.
- `RecipeBookComponent.setVisible` writes the global crafting-book open flag. The upgrade tab overrides it and does not send book settings. Placement is redirected off `GameMode.handlePlaceRecipe`, which always targets `player.containerMenu`.
- The book is positioned by choosing `width` / `height` so vanilla's centered origin lands on the 1.0.6 anchor (category tabs may hang off-screen; least-overlap fallback still opens the book).
- World crafting tables hook `ServerPlaceRecipe.tryPlaceRecipe` (account backpack stacks) and `moveItemToGrid(Slot, Holder<Item>, int)` (extract when `findSlotMatchingCraftingIngredient` misses). Only when the open menu is a `CraftingMenu`.
- Backpack button definitions moved to `BackpackButtonDefinitions`.
- Dev runs load `src/devcheck` (not inside the jar) and require the mixin hooks to exist. Runs also set `mixin.debug.verify` and `mixin.checks.interfaces`.

## Tests

- `gradlew clean build` succeeded. Jar `sophisticated_advanced_crafting-1.0.6-mc1.21.4.jar` (57026 bytes). The devcheck package is not in the jar.
- Dedicated server: `Done (4.175s)! For help, type "help"`. Log line `SAC devcheck: mixins applied (server)`. No mixin, registry, datapack, or recipe errors from this mod. Vanilla command-ambiguity warnings and the offline-mode warning are not from this mod.
- Client: resource reload included `mod/sophisticated_advanced_crafting`, then `Sound engine started`. Log line `SAC devcheck: mixins applied (client)`, which loaded `ServerPlaceRecipe`, `CraftingMenu`, `RecipeBookComponent` (hook `sac$initVisuals`), `CraftingRecipeBookComponent`, and `RecipeBookPage`. Runs also set `mixin.debug.verify` and `mixin.checks.interfaces`. No mixin or mod errors. Client process was stopped after the main menu.

## 1.21.5

NeoForge 21.5.98, Java 21, Parchment 1.21.5 / 2025.06.15, ModDevGradle 2.0.147.
Sophisticated Core 1.21.5-1.5.2.2345 (mods.toml version 1.5.2, NeoForge `[21.5.0,)`),
Backpacks 1.21.5-3.27.3.2164 (version 3.27.3), Storage 1.21.5-1.6.0.2139 (version 1.6.0).
No NeoForge bump was required. Our version ranges `[1.5.1,)`, `[3.24,)`, and `[1.4,)` accept these jars.

No source changes. `CompoundTag` Optional getters and `Item.appendHoverText(TooltipDisplay, Consumer)` are unused here. `ServerPlaceRecipe` stays in `net.minecraft.recipebook` and still returns from `tryPlaceRecipe` / `moveItemToGrid`, so the existing injectors apply. `EventBusSubscriber.bus` is deprecated for removal and still required so the dev check runs on the mod bus.

## Tests

- `gradlew clean build` succeeded. Jar `sophisticated_advanced_crafting-1.0.6-mc1.21.5.jar` (57024 bytes). The devcheck package is not in the jar.
- Dedicated server: `Done (3.918s)! For help, type "help"`. Log line `SAC devcheck: mixins applied (server)`. No mixin, registry, datapack, or recipe errors from this mod. A Sophisticated Backpacks message `No loader defined for swords` is not from this mod. Offline-mode warning is not from this mod.
- Client: resource reload included `mod/sophisticated_advanced_crafting`, then `Sound engine started`. Log line `SAC devcheck: mixins applied (client)`. No mixin or mod errors. A Realms feature-flag fetch error is not from this mod. Client process was stopped after the main menu.

## 1.21.8

NeoForge 21.8.54, Java 21, Parchment 1.21.8 / 2025.09.14, ModDevGradle 2.0.147.
Sophisticated Core 1.21.8-1.5.1.2344 (mods.toml version 1.5.1, NeoForge `[21.6.0,21.9.0)`),
Backpacks 1.21.8-3.26.3.2163 (version 3.26.3), Storage 1.21.8-1.6.0.2140 (version 1.6.0).
21.8.54 sits inside that range, so no NeoForge bump.

- Client packets use `ClientPacketDistributor.sendToServer`. `PacketDistributor.sendToServer` is gone.
- `ServerPlayer.serverLevel()` is `level()`.
- GUI pose stacks are `Matrix3x2fStack`: `pushMatrix` / `popMatrix`, and `translate(float, float)`.
- `@EventBusSubscriber` no longer has a mod-bus selector. The dev check registers with `IEventBus.addListener` through reflection from the mod constructor, and it is still absent from the jar.
- Mixin targets `ServerPlaceRecipe.tryPlaceRecipe`, `moveItemToGrid`, and `RecipeBookComponent` stacked-contents hooks are unchanged.

## Tests

- `gradlew clean build` succeeded. Jar `sophisticated_advanced_crafting-1.0.6-mc1.21.8.jar` (57504 bytes). The devcheck package is not in the jar.
- Dedicated server: `Done (4.725s)! For help, type "help"`. Log line `SAC devcheck: mixins applied (server)`. No mixin, registry, datapack, or recipe errors from this mod. A Sophisticated Backpacks message `No loader defined for swords` is not from this mod.
- Client: resource reload included `mod/sophisticated_advanced_crafting`, then `Sound engine started`. Log line `SAC devcheck: mixins applied (client)`. No mixin or mod errors. Client process was stopped after the main menu.

## 1.21.10

NeoForge 21.10.64, Java 21, Parchment 1.21.10 / 2025.10.12, ModDevGradle 2.0.147.
Sophisticated Core 1.21.10-1.5.1.2351 (mods.toml version 1.5.1, NeoForge `[21.10.0,21.11.0)`),
Backpacks 1.21.10-3.26.3.2165 (version 3.26.3), Storage 1.21.10-1.5.103.2141 (version 1.5.103).
21.10.64 sits inside that range, so no NeoForge bump. Storage's mods.toml version dropped to 1.5.103; `[1.4,)` still accepts it.

- `FMLEnvironment.dist` is `getDist()`.
- GUI input is `MouseButtonEvent`, `KeyEvent`, and `CharacterEvent`. `mouseClicked` is `(MouseButtonEvent, boolean)`. Shift-click is `hasShiftDown()` on the event, passed into `RecipeBookComponent.tryPlaceRecipe` as a third boolean. The upgrade tab forwards that flag as max-transfer.
- `Screen.hasShiftDown()` is `Minecraft.hasShiftDown()`.
- Backpack inventory is the NeoForge transfer API. `InventoryHandler.size()`, `extract(slot, ItemResource, amount, Transaction)`, and `insert(...)` replace `getSlots` / `extractItem` / `insertItem`. Root transactions are committed only when items actually move.
- Items register with the two-argument `registerItem` (id is applied by the deferred register).

## Tests

- `gradlew clean build` succeeded. Jar `sophisticated_advanced_crafting-1.0.6-mc1.21.10.jar` (58187 bytes). The devcheck package is not in the jar.
- Dedicated server: `Done (1.793s)! For help, type "help"`. Log line `SAC devcheck: mixins applied (server)`. No mixin, registry, datapack, or recipe errors from this mod. A Sophisticated Backpacks message `No loader defined for swords` is not from this mod.
- Client: resource reload included `mod/sophisticated_advanced_crafting`, then `Sound engine started`. Log line `SAC devcheck: mixins applied (client)`. No mixin or mod errors. Client process was stopped after the main menu.

## 1.21.11

NeoForge 21.11.45, Java 21, Parchment 1.21.11 / 2025.12.20, ModDevGradle 2.0.147.
Sophisticated Core 1.21.11-1.5.2.2348 (mods.toml version 1.5.2, NeoForge `[21.11.0,21.12.0)`),
Backpacks 1.21.11-3.26.3.2168 (version 3.26.3), Storage 1.21.11-1.5.102.2142 (version 1.5.102).
21.11.45 sits inside that range, so no NeoForge bump.

- `ResourceLocation` is `Identifier`. The place-recipe payload type uses `Identifier.fromNamespaceAndPath`.
- Recipe-book mixin targets are unchanged from 1.21.10 (`tryPlaceRecipe` still takes the shift boolean).

## Tests

- `gradlew clean build` succeeded. Jar `sophisticated_advanced_crafting-1.0.6-mc1.21.11.jar` (58194 bytes). The devcheck package is not in the jar.
- Dedicated server: `Done (1.980s)! For help, type "help"`. Log line `SAC devcheck: mixins applied (server)`. No mixin, registry, datapack, or recipe errors from this mod.
- Client: resource reload included `mod/sophisticated_advanced_crafting`, then `Sound engine started`. Log line `SAC devcheck: mixins applied (client)`. No mixin or mod errors. Client process was stopped after the main menu.

## 26.1.2

NeoForge 26.1.2.114, Java 25, ModDevGradle 2.0.148, no Parchment (unobfuscated).
Sophisticated Core 26.1.2-1.5.14.2409 (mods.toml version 1.5.14, NeoForge `[26.1.2.44-beta,26.2.0)`),
Backpacks 26.1.2-3.26.13.2207 (version 3.26.13), Storage 26.1.2-1.5.122.2185 (version 1.5.122).
26.1.2.114 sits inside that range, so no NeoForge bump.

- GUI drawing uses `GuiGraphicsExtractor`. Tab `render` / `renderBg` are `extractRenderState` / `extractBg`. The recipe book uses `extractRenderState`, `extractGhostRecipe`, and `extractTooltip`. Buttons use `extractRenderState`.
- Screen origin is `getLeftPos` / `getTopPos`; size is `getImageWidth` / `getImageHeight`.
- `ScreenEvent.Render` still exposes `getGuiGraphics()`, now a `GuiGraphicsExtractor`.
- The dev check loads mixin targets without running static initializers. Recipe-book classes touch item components in `<clinit>`, and common setup is before components are bound (`Components not bound yet`).
- Mixin targets for placement are unchanged.

## Tests

- `gradlew clean build` succeeded. Jar `sophisticated_advanced_crafting-1.0.6-mc26.1.2.jar` (58226 bytes). The devcheck package is not in the jar.
- Dedicated server: `Done (1.834s)! For help, type "help"`. Log line `SAC devcheck: mixins applied (server)`. No mixin, registry, datapack, or recipe errors from this mod.
- Client: resource reload included `mod/sophisticated_advanced_crafting`, then `Sound engine started`. Log line `SAC devcheck: mixins applied (client)`. No mixin or mod errors. Client process was stopped after the main menu. The first client launch crashed in the dev check because it initialized recipe-book classes during common setup; loading those classes without initialization fixed it.

## 26.2

NeoForge 26.2.0.88, Java 25, ModDevGradle 2.0.148, no Parchment (unobfuscated).
Sophisticated Core 26.2-1.5.12.2410 (mods.toml version 1.5.12, NeoForge `[26.2.0.53-beta,26.3.0)`),
Backpacks 26.2-3.26.12.2210 (version 3.26.12), Storage 26.2-1.5.122.2186 (version 1.5.122).
26.2.0.88 sits inside that range, so no NeoForge bump.

No source changes from 26.1.2. `ServerPlaceRecipe.tryPlaceRecipe` and `moveItemToGrid`, and `RecipeBookComponent.tryPlaceRecipe` plus `extractRenderState` / `extractGhostRecipe` / `extractTooltip`, keep the 26.1 signatures. Entity type constants and `Minecraft#gui.setScreen` do not appear in this mod. Mixin `compatibilityLevel` stays `JAVA_21`. `RecipePlacementHelper` still compiles with a deprecation note.

## Tests

- `gradlew clean build` succeeded. Jar `sophisticated_advanced_crafting-1.0.6-mc26.2.jar` (58225 bytes). The devcheck package is not in the jar.
- Dedicated server: `Done (1.861s)! For help, type "help"`. Log line `SAC devcheck: mixins applied (server)`. No mixin, registry, datapack, or recipe errors from this mod.
- Client: resource reload included `mod/sophisticated_advanced_crafting`, then `Sound engine started`. Log line `SAC devcheck: mixins applied (client)`. No mixin or mod errors. Client process was stopped after the main menu.
