# Sophisticated Advanced Crafting

NeoForge mod for Minecraft 1.21.8 that adds **Advanced Crafting Upgrades** for Sophisticated Backpacks and Sophisticated Storage.

One jar replaces the separate `sb-advanced-crafting` and `ss-advanced-crafting` mods.

## Features

- Floating green recipe book beside the storage GUI (left preferred, away from JEI). Category tabs may hang off-screen. On a short GUI the book still opens at the least-overlap spot. Closed by default.
- Dual-source recipe placement in the upgrade tab (storage/backpack inventory + player inventory)
- **World crafting tables** also pull from backpack inventory when the backpack has this Advanced Crafting upgrade
- Distinct icons: backpack copper+gold, storage wood+gold
- Cheaper upgrade recipe: **1 diamond + 1 crafting upgrade + 2 gold ingots + 3 redstone**

## Items

| Item ID | Requires |
|---------|----------|
| `sophisticated_advanced_crafting:backpack_advanced_crafting_upgrade` | Sophisticated Backpacks |
| `sophisticated_advanced_crafting:storage_advanced_crafting_upgrade` | Sophisticated Storage |

Conflicts with the stock `CraftingUpgradeItem` in the same backpack/storage.

## Dependencies

- **Required:** Minecraft 1.21.8, NeoForge 21.8.54+, Sophisticated Core 1.21.8-1.5.1.2344
- **Optional:** Sophisticated Backpacks 1.21.8-3.26.3.2163, Sophisticated Storage 1.21.8-1.6.0.2140

## Build

```bash
./gradlew build
```

Jar: `build/libs/sophisticated_advanced_crafting-1.0.6-mc1.21.8.jar`

## License

MIT — Renzo
