package gg.embers.tinythings.item.excavatorShovel;

import gg.embers.tinythings.TinyThings;
import java.util.Set;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;

public class ExcavatorShovelListener implements Listener {
    private static final Set<Material> DIGGABLE = Set.of(
        Material.DIRT, Material.GRASS_BLOCK, Material.PODZOL, Material.COARSE_DIRT,
        Material.ROOTED_DIRT, Material.MYCELIUM, Material.DIRT_PATH, Material.FARMLAND,
        Material.SAND, Material.RED_SAND, Material.GRAVEL, Material.CLAY,
        Material.SOUL_SAND, Material.SOUL_SOIL, Material.MUD, Material.MUDDY_MANGROVE_ROOTS,
        Material.SNOW, Material.SNOW_BLOCK, Material.POWDER_SNOW
    );

    private final TinyThings plugin;
    private final ExcavatorShovelItem item;

    public ExcavatorShovelListener(TinyThings plugin, ExcavatorShovelItem item) {
        this.plugin = plugin;
        this.item = item;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!this.item.matches(this.plugin, tool)) {
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE || player.isSneaking()) {
            return;
        }
        Block origin = event.getBlock();
        if (!DIGGABLE.contains(origin.getType())) {
            return;
        }
        BlockFace face = this.resolveFace(player);
        int radius = this.item.radius();
        int dug = 0;
        for (int[] offset : this.planeOffsets(face, radius)) {
            Block block = origin.getRelative(offset[0], offset[1], offset[2]);
            if (!DIGGABLE.contains(block.getType())) {
                continue;
            }
            block.breakNaturally(tool);
            ++dug;
        }
        if (dug == 0) {
            return;
        }
        origin.getWorld().playSound(origin.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 0.8f, 0.8f);
        this.consumeUse(player, tool);
    }

    private BlockFace resolveFace(Player player) {
        RayTraceResult result = player.rayTraceBlocks(6.0, FluidCollisionMode.NEVER);
        if (result != null && result.getHitBlockFace() != null) {
            return result.getHitBlockFace();
        }
        float pitch = player.getLocation().getPitch();
        if (pitch > 45.0f) {
            return BlockFace.UP;
        }
        if (pitch < -45.0f) {
            return BlockFace.DOWN;
        }
        return player.getFacing();
    }

    private int[][] planeOffsets(BlockFace face, int radius) {
        boolean verticalAxis = face == BlockFace.UP || face == BlockFace.DOWN;
        boolean zAxis = face == BlockFace.NORTH || face == BlockFace.SOUTH;
        int span = radius * 2 + 1;
        int[][] offsets = new int[span * span - 1][3];
        int index = 0;
        for (int a = -radius; a <= radius; ++a) {
            for (int b = -radius; b <= radius; ++b) {
                if (a == 0 && b == 0) {
                    continue;
                }
                if (verticalAxis) {
                    offsets[index++] = new int[]{a, 0, b};
                } else if (zAxis) {
                    offsets[index++] = new int[]{a, b, 0};
                } else {
                    offsets[index++] = new int[]{0, b, a};
                }
            }
        }
        return offsets;
    }

    private void consumeUse(Player player, ItemStack tool) {
        int max = this.item.maxUses();
        int size = this.item.radius() * 2 + 1;
        int remaining = this.item.readUses(tool) - 1;
        if (remaining <= 0) {
            tool.setAmount(0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage("§7Your §3Excavator Shovel§7 shattered.");
            return;
        }
        ItemMeta meta = tool.getItemMeta();
        meta.getPersistentDataContainer().set(this.item.usesKey(), PersistentDataType.INTEGER, remaining);
        meta.setLore(this.item.buildLore(remaining, max, size));
        tool.setItemMeta(meta);
    }
}
