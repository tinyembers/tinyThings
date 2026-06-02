package gg.embers.tinythings.item;

import gg.embers.tinythings.TinyThings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Shows a custom "uses left" count on an item's real durability bar, so players
 * get the usual green-to-red wear indicator and can tell when a tool is about to
 * break. The use count stays the single source of truth: vanilla durability loss
 * on our items is suppressed so it never desyncs from the bar.
 */
public final class Durability implements Listener {
    private final TinyThings plugin;

    public Durability(TinyThings plugin) {
        this.plugin = plugin;
    }

    /**
     * Renders {@code remaining}/{@code max} uses as a durability bar on the given meta.
     * Any item can carry a durability bar once it is unstackable, so this works for the
     * non-tool items (blaze rod, tripwire hook) just as well as the diamond tools.
     */
    public static void apply(ItemMeta meta, int remaining, int max) {
        if (!(meta instanceof Damageable damageable)) {
            return;
        }
        int safeMax = Math.max(1, max);
        int safeRemaining = Math.max(0, Math.min(safeMax, remaining));
        // Unbreakable items never render a bar, so make sure ours can — vanilla wear is
        // blocked by the handler below instead.
        meta.setUnbreakable(false);
        meta.setMaxStackSize(1);
        damageable.setMaxDamage(safeMax);
        damageable.setDamage(safeMax - safeRemaining);
    }

    /**
     * Our items manage their own wear through the use count, so vanilla durability damage
     * is cancelled to keep the bar in step with "uses left".
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (!item.hasItemMeta()) {
            return;
        }
        if (item.getItemMeta().getPersistentDataContainer().has(this.plugin.itemIdKey(), PersistentDataType.STRING)) {
            event.setCancelled(true);
        }
    }
}
