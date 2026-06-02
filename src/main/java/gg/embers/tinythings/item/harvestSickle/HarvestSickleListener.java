package gg.embers.tinythings.item.harvestSickle;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.Durability;
import java.util.Collection;
import java.util.Set;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class HarvestSickleListener implements Listener {
    private static final Set<Material> CROPS = Set.of(
        Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS, Material.NETHER_WART
    );

    private final TinyThings plugin;
    private final HarvestSickleItem item;

    public HarvestSickleListener(TinyThings plugin, HarvestSickleItem item) {
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
        ItemStack tool = event.getItem();
        if (!this.item.matches(this.plugin, tool)) {
            return;
        }
        event.setCancelled(true);
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }
        Player player = event.getPlayer();
        int radius = this.item.radius();
        int harvested = 0;
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dz = -radius; dz <= radius; ++dz) {
                if (this.harvest(player, clicked.getRelative(dx, 0, dz))) {
                    ++harvested;
                }
            }
        }
        if (harvested == 0) {
            player.sendMessage("§7No ripe crops in range.");
            return;
        }
        clicked.getWorld().playSound(clicked.getLocation(), Sound.ITEM_CROP_PLANT, 1.0f, 1.0f);
        clicked.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, clicked.getLocation().add(0.5, 0.5, 0.5), 20, radius + 0.5, 0.3, radius + 0.5, 0.0);
        if (player.getGameMode() != GameMode.CREATIVE) {
            this.consumeUse(player, tool);
        }
    }

    private boolean harvest(Player player, Block block) {
        if (!CROPS.contains(block.getType())) {
            return false;
        }
        if (!(block.getBlockData() instanceof Ageable ageable) || ageable.getAge() < ageable.getMaximumAge()) {
            return false;
        }
        Collection<ItemStack> drops = block.getDrops();
        ageable.setAge(0);
        block.setBlockData(ageable, false);
        for (ItemStack drop : drops) {
            for (ItemStack overflow : player.getInventory().addItem(drop).values()) {
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), overflow);
            }
        }
        return true;
    }

    private void consumeUse(Player player, ItemStack tool) {
        int max = this.item.maxUses();
        int size = this.item.radius() * 2 + 1;
        int remaining = this.item.readUses(tool) - 1;
        if (remaining <= 0) {
            tool.setAmount(0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage("§7Your §aHarvest Sickle§7 snapped.");
            return;
        }
        ItemMeta meta = tool.getItemMeta();
        meta.getPersistentDataContainer().set(this.item.usesKey(), PersistentDataType.INTEGER, remaining);
        meta.setLore(this.item.buildLore(remaining, max, size));
        Durability.apply(meta, remaining, max);
        tool.setItemMeta(meta);
    }
}
