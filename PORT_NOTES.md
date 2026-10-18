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
