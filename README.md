# Sophisticated Advanced Crafting

NeoForge 1.21.1 mod that adds **Advanced Crafting Upgrades** for Sophisticated Backpacks and Sophisticated Storage.

One jar replaces the separate `sb-advanced-crafting` and `ss-advanced-crafting` mods.

## Features

- Floating clamped green recipe book (flip to freer side of the screen)
- Dual-source recipe placement (storage/backpack inventory + player inventory)
- Gold-outline icons matching other Sophisticated advanced upgrades
- Cheaper upgrade recipe: **1 diamond + 1 crafting upgrade + 2 gold ingots + 3 redstone**

## Items

| Item ID | Requires |
|---------|----------|
| `sophisticated_advanced_crafting:backpack_advanced_crafting_upgrade` | Sophisticated Backpacks |
| `sophisticated_advanced_crafting:storage_advanced_crafting_upgrade` | Sophisticated Storage |

Conflicts with the stock `CraftingUpgradeItem` in the same backpack/storage.

## Dependencies

- **Required:** Minecraft 1.21.1, NeoForge 21.1+, Sophisticated Core
- **Optional:** Sophisticated Backpacks, Sophisticated Storage

Backpack features register only when Backpacks is loaded; storage features only when Storage is loaded.

## Build

```bash
./gradlew build
```

Jar: `build/libs/sophisticated_advanced_crafting-1.0.0.jar`
