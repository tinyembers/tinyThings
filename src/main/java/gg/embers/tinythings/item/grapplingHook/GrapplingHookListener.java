package gg.embers.tinythings.item.grapplingHook;

import gg.embers.tinythings.TinyThings;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class GrapplingHookListener implements Listener {
    private static final long FALL_IMMUNITY_MILLIS = 8000L;

    private final TinyThings plugin;
    private final GrapplingHookItem item;
    private final Map<UUID, Long> cooldownEndsAt = new HashMap<>();
    private final Map<UUID, Long> fallImmunityUntil = new HashMap<>();

    public GrapplingHookListener(TinyThings plugin, GrapplingHookItem item) {
        this.plugin = plugin;
        this.item = item;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack hook = event.getItem();
        if (!this.item.matches(this.plugin, hook)) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        long now = System.currentTimeMillis();
        long cooldownEnd = this.cooldownEndsAt.getOrDefault(player.getUniqueId(), 0L);
        if (now < cooldownEnd) {
            return;
        }
        RayTraceResult result = player.rayTraceBlocks(this.item.maxDistance(), FluidCollisionMode.NEVER);
        if (result == null || result.getHitBlock() == null) {
            player.sendMessage("§7No anchor point in range.");
            return;
        }
        Vector eye = player.getEyeLocation().toVector();
        Vector direction = result.getHitPosition().clone().subtract(eye);
        double distance = direction.length();
        if (distance < 1.0) {
            return;
        }
        Vector velocity = direction.normalize().multiply(this.item.power());
        velocity.setY(velocity.getY() + Math.min(0.6, distance * 0.05));
        player.setVelocity(velocity);
        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 1.0f, 1.2f);
        this.cooldownEndsAt.put(player.getUniqueId(), now + (long) this.item.cooldownTicks() * 50L);
        this.fallImmunityUntil.put(player.getUniqueId(), now + FALL_IMMUNITY_MILLIS);
        this.consumeUse(player, hook);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Long until = this.fallImmunityUntil.get(player.getUniqueId());
        if (until != null && System.currentTimeMillis() < until) {
            event.setCancelled(true);
            this.fallImmunityUntil.remove(player.getUniqueId());
        }
    }

    private void consumeUse(Player player, ItemStack hook) {
        int max = this.item.maxUses();
        int remaining = this.item.readUses(hook) - 1;
        if (remaining <= 0) {
            hook.setAmount(0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage("§7Your §eGrappling Hook§7 snapped.");
            return;
        }
        ItemMeta meta = hook.getItemMeta();
        meta.getPersistentDataContainer().set(this.item.usesKey(), PersistentDataType.INTEGER, remaining);
        meta.setLore(this.item.buildLore(remaining, max));
        hook.setItemMeta(meta);
    }
}
