package gg.embers.tinythings.item.painterBrush;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.Durability;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PainterBrushListener implements Listener {
    private final TinyThings plugin;
    private final PainterBrushItem item;

    public PainterBrushListener(TinyThings plugin, PainterBrushItem item) {
        this.plugin = plugin;
        this.item = item;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        ItemStack brush = event.getItem();
        if (!this.item.matches(this.plugin, brush)) {
            return;
        }
        Block clicked = event.getClickedBlock();
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && clicked != null) {
            event.setCancelled(true);
            this.pick(player, brush, clicked.getType());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clicked != null) {
            event.setCancelled(true);
            this.paint(player, brush, clicked);
        }
    }

    private void pick(Player player, ItemStack brush, Material material) {
        if (!material.isBlock() || !material.isItem()) {
            player.sendMessage("§7You can't paint with §e" + this.prettyName(material) + "§7.");
            return;
        }
        ItemMeta meta = brush.getItemMeta();
        meta.getPersistentDataContainer().set(this.item.paintKey(), PersistentDataType.STRING, material.name());
        meta.setLore(this.item.buildLore(this.item.readUses(brush), this.item.maxUses(), material));
        brush.setItemMeta(meta);
        player.sendMessage("§dPainter Brush§7 will now paint §d" + this.prettyName(material) + "§7.");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.6f);
    }

    private void paint(Player player, ItemStack brush, Block clicked) {
        Material picked = this.item.readPaint(brush);
        if (picked == null) {
            player.sendMessage("§7Left-click a block first to pick a material.");
            return;
        }
        if (clicked.isEmpty() || clicked.isLiquid()) {
            return;
        }
        if (clicked.getState() instanceof InventoryHolder) {
            player.sendMessage("§7That block can't be painted.");
            return;
        }
        if (clicked.getType() == picked) {
            return;
        }
        boolean creative = player.getGameMode() == GameMode.CREATIVE;
        if (creative) {
            clicked.setType(picked, false);
            this.effects(clicked);
            return;
        }
        if (this.countMaterial(player, picked) <= 0) {
            player.sendMessage("§7You have no §e" + this.prettyName(picked) + "§7 to paint with.");
            return;
        }
        Collection<ItemStack> drops = clicked.getDrops();
        player.getInventory().removeItem(new ItemStack(picked, 1));
        clicked.setType(picked, false);
        for (ItemStack drop : drops) {
            for (ItemStack overflow : player.getInventory().addItem(drop).values()) {
                clicked.getWorld().dropItemNaturally(clicked.getLocation().add(0.5, 0.5, 0.5), overflow);
            }
        }
        this.effects(clicked);
        this.consumeUse(player, brush);
    }

    private void effects(Block block) {
        block.getWorld().spawnParticle(Particle.WAX_ON, block.getLocation().add(0.5, 0.5, 0.5), 12, 0.3, 0.3, 0.3, 0.0);
        block.getWorld().playSound(block.getLocation(), Sound.ITEM_BRUSH_BRUSHING_GENERIC, 0.8f, 1.4f);
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
                builder.append(parts[i].substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return builder.toString();
    }

    private void consumeUse(Player player, ItemStack brush) {
        int max = this.item.maxUses();
        Material picked = this.item.readPaint(brush);
        int remaining = this.item.readUses(brush) - 1;
        if (remaining <= 0) {
            brush.setAmount(0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage("§7Your §dPainter Brush§7 wore out.");
            return;
        }
        ItemMeta meta = brush.getItemMeta();
        meta.getPersistentDataContainer().set(this.item.usesKey(), PersistentDataType.INTEGER, remaining);
        meta.setLore(this.item.buildLore(remaining, max, picked));
        Durability.apply(meta, remaining, max);
        brush.setItemMeta(meta);
    }
}
