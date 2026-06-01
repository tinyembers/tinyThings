package gg.embers.tinythings.item.infiniteBucket;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Keeps an infinite bucket filled after it is emptied. The liquid/snow is still placed
 * (the event is not cancelled); we only override the resulting hand item so the bucket
 * never turns into an empty one.
 */
public class InfiniteBucketListener implements Listener {
    private final InfiniteBucketItem item;

    public InfiniteBucketListener(InfiniteBucketItem item) {
        this.item = item;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        EquipmentSlot hand = event.getHand();
        ItemStack used = hand == null
                ? event.getPlayer().getInventory().getItemInMainHand()
                : event.getPlayer().getInventory().getItem(hand);
        if (!this.item.matchesBucket(used)) {
            return;
        }
        event.setItemStack(used.clone());
    }
}
