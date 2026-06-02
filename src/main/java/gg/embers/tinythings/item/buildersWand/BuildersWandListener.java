package gg.embers.tinythings.item.buildersWand;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.Durability;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class BuildersWandListener implements Listener {
    private final TinyThings plugin;
    private final BuildersWandItem item;

    public BuildersWandListener(TinyThings plugin, BuildersWandItem item) {
        this.plugin = plugin;
        this.item = item;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack wand = event.getItem();
        if (!this.item.matches(this.plugin, wand)) {
            return;
        }
        event.setCancelled(true);
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }
        Material material = clicked.getType();
        Player player = event.getPlayer();
        if (!material.isBlock() || !material.isSolid()) {
            player.sendMessage("§7That block can't be extended.");
            return;
        }
        BlockData template = clicked.getBlockData();
        List<Block> targets = new ArrayList<>();
        for (int[] offset : this.planeOffsets(event.getBlockFace(), this.item.radius())) {
            Block target = clicked.getRelative(offset[0], offset[1], offset[2]);
            if (target.getType().isAir() || target.isReplaceable()) {
                targets.add(target);
            }
        }
        if (targets.isEmpty()) {
            player.sendMessage("§7Nothing to extend here.");
            return;
        }
        boolean creative = player.getGameMode() == GameMode.CREATIVE;
        int placeable = creative ? targets.size() : Math.min(targets.size(), this.countMaterial(player, material));
        if (placeable <= 0) {
            player.sendMessage("§7You have no §e" + this.prettyName(material) + "§7 to place.");
            return;
        }
        for (int i = 0; i < placeable; ++i) {
            targets.get(i).setBlockData(template, false);
        }
        if (!creative) {
            player.getInventory().removeItem(new ItemStack(material, placeable));
        }
        clicked.getWorld().playSound(clicked.getLocation(), Sound.BLOCK_STONE_PLACE, 0.8f, 1.2f);
        if (!creative) {
            this.consumeUse(player, wand);
        }
    }

    private int countMaterial(Player player, Material material) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
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

    private String prettyName(Material material) {
        String[] parts = material.getKey().getKey().split("_");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; ++i) {
            if (parts[i].isEmpty()) {
                continue;
            }
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) {
                builder.append(parts[i].substring(1).toLowerCase());
            }
        }
        return builder.toString();
    }

    private void consumeUse(Player player, ItemStack wand) {
        int max = this.item.maxUses();
        int size = this.item.radius() * 2 + 1;
        int remaining = this.item.readUses(wand) - 1;
        if (remaining <= 0) {
            wand.setAmount(0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage("§7Your §6Builder's Wand§7 shattered.");
            return;
        }
        ItemMeta meta = wand.getItemMeta();
        meta.getPersistentDataContainer().set(this.item.usesKey(), PersistentDataType.INTEGER, remaining);
        meta.setLore(this.item.buildLore(remaining, max, size));
        Durability.apply(meta, remaining, max);
        wand.setItemMeta(meta);
    }
}
