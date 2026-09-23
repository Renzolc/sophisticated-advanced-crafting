# Sophisticated Advanced Crafting

NeoForge 1.21.1 mod that adds **Advanced Crafting Upgrades** for Sophisticated Backpacks and Sophisticated Storage.

One jar replaces the separate `sb-advanced-crafting` and `ss-advanced-crafting` mods.

## Features

- Floating green recipe book that never overlaps the storage GUI / player inventory / hotbar (right → left → above → below outside the full screen content; closed by default; category-tab overhang included)
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

- **Required:** Minecraft 1.21.1, NeoForge 21.1+, Sophisticated Core
- **Optional:** Sophisticated Backpacks, Sophisticated Storage

## Build

```bash
./gradlew build
```

Jar: `build/libs/sophisticated_advanced_crafting-1.0.3.jar`

## License

MIT — Alex Krolick
