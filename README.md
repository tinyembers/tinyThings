# tinyThings

Extensible Paper/Spigot plugin for custom command-only items — Super Sponge, Magnet Stick, Surveyor Stick, Super Bonemeal, Spawner Wrench, Vein Pickaxe, Excavator Shovel, Builder's Wand, Painter Brush, Harvest Sickle, Grappling Hook, infinite Water/Lava/Snow Buckets, and an Infinite Block placement system. Includes a `/size` command for player scaling.

- **API:** Paper 1.21
- **Main class:** `gg.embers.tinythings.TinyThings`
- **Soft deps:** Vault, ShopGUIPlus, RoseStacker

## Build

```sh
mvn package
```

Output: `target/tinyThings.jar`.

## Commands

- `/tinythings <give|list|make-infinite|reload|help>` (aliases: `/tt`, `/tiny`)
- `/size [<number>] [<player>]`

## Source provenance

Initial source was decompiled from the shipped `tinyThings.jar` (v1.0) with CFR 0.152 and committed as a recovery snapshot. Subsequent changes are authored directly.
