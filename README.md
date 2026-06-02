# tinyThings

Extensible Paper/Spigot plugin for custom command-only items — Super Sponge, Magnet Stick, Surveyor Stick, Biome Stick, Super Bonemeal, Spawner Wrench, Vein Pickaxe, Excavator Shovel, Builder's Wand, Painter Brush, Harvest Sickle, Grappling Hook, infinite Water/Lava/Snow Buckets, and an Infinite Block placement system. Includes a `/size` command for player scaling.

- **API:** Paper 1.21
- **Main class:** `gg.embers.tinythings.TinyThings`
- **Soft deps:** Vault, ShopGUIPlus, RoseStacker, FastAsyncWorldEdit (required for the Biome Stick)

## Biome Stick

Repaints the biome of a region. Shift + right-click opens a paginated picker of every biome registered with WorldEdit/FAWE (vanilla **and** datapack biomes such as Terralith); right-click two corners to paint the selection; left-click a block to copy its biome. The area is capped by `biome-stick.max-area` in `config.yml`, and edits run through FAWE so even large selections stay off the main thread. Gated by the `tinythings.biomestick.use` permission.

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
