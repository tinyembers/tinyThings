package gg.embers.tinythings.item.veinPickaxe;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.Durability;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class VeinPickaxeListener implements Listener {
    private static final Set<Material> ORES = Set.of(
        Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
        Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
        Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
        Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
        Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
        Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
        Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
        Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
        Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
        Material.ANCIENT_DEBRIS
    );

    private final TinyThings plugin;
    private final VeinPickaxeItem item;

    public VeinPickaxeListener(TinyThings plugin, VeinPickaxeItem item) {
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
        Material type = origin.getType();
        if (!ORES.contains(type)) {
            return;
        }
        List<Block> vein = this.collectVein(origin, type, this.item.maxBlocks());
        if (vein.isEmpty()) {
            return;
        }
        for (Block block : vein) {
            block.breakNaturally(tool);
        }
        origin.getWorld().playSound(origin.getLocation(), Sound.BLOCK_STONE_BREAK, 0.8f, 0.8f);
        this.consumeUse(player, tool);
    }

    private List<Block> collectVein(Block origin, Material type, int max) {
        List<Block> found = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Deque<Block> queue = new ArrayDeque<>();
        visited.add(origin);
        queue.add(origin);
        while (!queue.isEmpty() && found.size() < max) {
            Block current = queue.poll();
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dy = -1; dy <= 1; ++dy) {
                    for (int dz = -1; dz <= 1; ++dz) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }
                        Block neighbour = current.getRelative(dx, dy, dz);
                        if (!visited.add(neighbour)) {
                            continue;
                        }
                        if (neighbour.getType() != type) {
                            continue;
                        }
                        found.add(neighbour);
                        queue.add(neighbour);
                        if (found.size() >= max) {
                            return found;
                        }
                    }
                }
            }
        }
        return found;
    }

    private void consumeUse(Player player, ItemStack tool) {
        int max = this.item.maxUses();
        int remaining = this.item.readUses(tool) - 1;
        if (remaining <= 0) {
            tool.setAmount(0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage("§7Your §bVein Pickaxe§7 shattered.");
            return;
        }
        ItemMeta meta = tool.getItemMeta();
        meta.getPersistentDataContainer().set(this.item.usesKey(), PersistentDataType.INTEGER, remaining);
        meta.setLore(this.item.buildLore(remaining, max));
        Durability.apply(meta, remaining, max);
        tool.setItemMeta(meta);
    }
}
