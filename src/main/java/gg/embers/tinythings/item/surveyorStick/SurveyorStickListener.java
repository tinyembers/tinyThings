/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.EquipmentSlot
 */
package gg.embers.tinythings.item.surveyorStick;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.surveyorStick.SurveyorStickItem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SurveyorStickListener
implements Listener {
    private final TinyThings plugin;
    private final SurveyorStickItem item;
    private final Map<UUID, Location> firstPoints = new HashMap<UUID, Location>();

    public SurveyorStickListener(TinyThings tinyThings, SurveyorStickItem surveyorStickItem) {
        this.plugin = tinyThings;
        this.item = surveyorStickItem;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (!this.item.matches(this.plugin, playerInteractEvent.getItem())) {
            return;
        }
        Player player = playerInteractEvent.getPlayer();
        UUID uUID = player.getUniqueId();
        if (player.isSneaking() && (playerInteractEvent.getAction() == Action.RIGHT_CLICK_AIR || playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            playerInteractEvent.setCancelled(true);
            if (this.firstPoints.remove(uUID) != null) {
                player.sendMessage("\u00a7eSurveyor: \u00a77selection cleared.");
            } else {
                player.sendMessage("\u00a7eSurveyor: \u00a77no selection to clear.");
            }
            return;
        }
        if (playerInteractEvent.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = playerInteractEvent.getClickedBlock();
        if (block == null) {
            return;
        }
        playerInteractEvent.setCancelled(true);
        Location location = block.getLocation();
        Location location2 = this.firstPoints.get(uUID);
        if (location2 == null || !location.getWorld().equals((Object)location2.getWorld())) {
            this.firstPoints.put(uUID, location);
            player.sendMessage("\u00a7eSurveyor \u00a78\u00bb \u00a77Point \u00a7aA\u00a77 set at \u00a7f" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
            player.playSound(location, Sound.UI_BUTTON_CLICK, 0.5f, 1.4f);
            return;
        }
        this.firstPoints.remove(uUID);
        this.sendMeasurement(player, location2, location);
        player.playSound(location, Sound.UI_BUTTON_CLICK, 0.5f, 1.8f);
    }

    private void sendMeasurement(Player player, Location location, Location location2) {
        int n = Math.abs(location2.getBlockX() - location.getBlockX());
        int n2 = Math.abs(location2.getBlockY() - location.getBlockY());
        int n3 = Math.abs(location2.getBlockZ() - location.getBlockZ());
        double d = Math.sqrt(n * n + n2 * n2 + n3 * n3);
        double d2 = Math.sqrt(n * n + n3 * n3);
        long l = (long)(n + 1) * (long)(n3 + 1);
        long l2 = l * (long)(n2 + 1);
        player.sendMessage("\u00a7eSurveyor \u00a78\u00bb \u00a77measurement:");
        player.sendMessage("  \u00a77A: \u00a7f" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "  \u00a78| \u00a77B: \u00a7f" + location2.getBlockX() + ", " + location2.getBlockY() + ", " + location2.getBlockZ());
        player.sendMessage("  \u00a77\u0394: \u00a7fx=" + n + "  \u00a7fy=" + n2 + "  \u00a7fz=" + n3);
        player.sendMessage(String.format("  \u00a77Distance: \u00a7f%.2f\u00a77  Horizontal: \u00a7f%.2f", d, d2));
        player.sendMessage("  \u00a77Footprint: \u00a7f" + (n + 1) + "\u00d7" + (n3 + 1) + " \u00a78(\u00a7f" + l + " blocks\u00a78)");
        player.sendMessage("  \u00a77Volume: \u00a7f" + (n + 1) + "\u00d7" + (n2 + 1) + "\u00d7" + (n3 + 1) + " \u00a78(\u00a7f" + l2 + " blocks\u00a78)");
    }
}

