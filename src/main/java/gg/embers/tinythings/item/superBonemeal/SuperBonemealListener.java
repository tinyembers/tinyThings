/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.GameMode
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockFace
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.ItemStack
 */
package gg.embers.tinythings.item.superBonemeal;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.superBonemeal.SuperBonemealItem;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SuperBonemealListener
implements Listener {
    private final TinyThings plugin;
    private final SuperBonemealItem item;

    public SuperBonemealListener(TinyThings tinyThings, SuperBonemealItem superBonemealItem) {
        this.plugin = tinyThings;
        this.item = superBonemealItem;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (playerInteractEvent.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack itemStack = playerInteractEvent.getItem();
        if (!this.item.matches(this.plugin, itemStack)) {
            return;
        }
        Block block = playerInteractEvent.getClickedBlock();
        if (block == null) {
            return;
        }
        playerInteractEvent.setCancelled(true);
        int n = this.plugin.getConfig().getInt("super-bonemeal.radius", 2);
        BlockFace blockFace = playerInteractEvent.getBlockFace();
        int n2 = 0;
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                Block block2 = block.getRelative(i, 0, j);
                if (!block2.applyBoneMeal(blockFace)) continue;
                ++n2;
            }
        }
        Player player = playerInteractEvent.getPlayer();
        if (n2 == 0) {
            player.sendMessage("\u00a77Super Bonemeal had no effect here.");
            return;
        }
        if (player.getGameMode() != GameMode.CREATIVE) {
            itemStack.setAmount(itemStack.getAmount() - 1);
        }
        block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 1.0, 0.5), 30, (double)n + 0.5, 0.5, (double)n + 0.5, 0.0);
        block.getWorld().playSound(block.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 1.2f);
    }
}

