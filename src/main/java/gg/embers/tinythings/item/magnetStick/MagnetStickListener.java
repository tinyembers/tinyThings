/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Sound
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.util.Vector
 */
package gg.embers.tinythings.item.magnetStick;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.magnetStick.MagnetStickItem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class MagnetStickListener
implements Listener {
    private final TinyThings plugin;
    private final MagnetStickItem item;
    private final Map<UUID, Long> cooldownEndsAt = new HashMap<UUID, Long>();

    public MagnetStickListener(TinyThings tinyThings, MagnetStickItem magnetStickItem) {
        this.plugin = tinyThings;
        this.item = magnetStickItem;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (playerInteractEvent.getAction() != Action.RIGHT_CLICK_AIR && playerInteractEvent.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!this.item.matches(this.plugin, playerInteractEvent.getItem())) {
            return;
        }
        playerInteractEvent.setCancelled(true);
        final Player player = playerInteractEvent.getPlayer();
        long l = System.currentTimeMillis();
        long l2 = this.cooldownEndsAt.getOrDefault(player.getUniqueId(), 0L);
        if (l < l2) {
            long l3 = (l2 - l + 999L) / 1000L;
            player.sendMessage("\u00a77Magnet on cooldown (\u00a7e" + l3 + "s\u00a77)");
            return;
        }
        final int n = this.plugin.getConfig().getInt("magnet-stick.radius", 8);
        final int n2 = this.plugin.getConfig().getInt("magnet-stick.duration-ticks", 100);
        int n3 = this.plugin.getConfig().getInt("magnet-stick.cooldown-ticks", 200);
        this.cooldownEndsAt.put(player.getUniqueId(), l + (long)n3 * 50L);
        player.sendMessage("\u00a7dMagnet activated!");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.6f);
        new BukkitRunnable(){
            int ticks = 0;

            public void run() {
                if (this.ticks >= n2 || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                Vector vector = player.getLocation().add(0.0, 0.5, 0.0).toVector();
                for (Entity entity : player.getNearbyEntities((double)n, (double)n, (double)n)) {
                    UUID uUID;
                    Item item;
                    if (!(entity instanceof Item) || (item = (Item)entity).getPickupDelay() >= Short.MAX_VALUE || (uUID = item.getOwner()) != null && !uUID.equals(player.getUniqueId())) continue;
                    Vector vector2 = vector.clone().subtract(item.getLocation().toVector());
                    double d2 = vector2.lengthSquared();
                    if (d2 < 0.5) continue;
                    double d3 = Math.min(0.8, 0.25 + 0.05 * Math.sqrt(d2));
                    item.setVelocity(vector2.normalize().multiply(d3));
                }
                this.ticks += 2;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 2L);
    }
}

