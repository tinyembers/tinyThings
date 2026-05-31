/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.GameMode
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockState
 *  org.bukkit.block.CreatureSpawner
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.BlockStateMeta
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 */
package gg.embers.tinythings.item.spawnerWrench;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.spawnerWrench.SpawnerWrenchItem;
import java.util.HashMap;
import java.util.Locale;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerWrenchListener
implements Listener {
    private final TinyThings plugin;
    private final SpawnerWrenchItem item;

    public SpawnerWrenchListener(TinyThings tinyThings, SpawnerWrenchItem spawnerWrenchItem) {
        this.plugin = tinyThings;
        this.item = spawnerWrenchItem;
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
        if (block == null || block.getType() != Material.SPAWNER) {
            return;
        }
        playerInteractEvent.setCancelled(true);
        Player player = playerInteractEvent.getPlayer();
        BlockState blockState = block.getState();
        if (!(blockState instanceof CreatureSpawner)) {
            player.sendMessage("\u00a7cThat spawner can't be picked up.");
            return;
        }
        CreatureSpawner creatureSpawner = (CreatureSpawner)blockState;
        blockState = creatureSpawner.getSpawnedType();
        ItemStack itemStack2 = this.buildSpawnerItem(creatureSpawner, (EntityType)blockState);
        block.setType(Material.AIR, false);
        block.getWorld().spawnParticle(Particle.SMOKE, block.getLocation().add(0.5, 0.5, 0.5), 20, 0.3, 0.3, 0.3, 0.02);
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_NETHERITE_BLOCK_BREAK, 1.0f, 1.2f);
        HashMap hashMap = player.getInventory().addItem(new ItemStack[]{itemStack2});
        if (!hashMap.isEmpty()) {
            for (ItemStack itemStack3 : hashMap.values()) {
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), itemStack3);
            }
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        this.consumeUse(player, itemStack);
    }

    private ItemStack buildSpawnerItem(CreatureSpawner creatureSpawner, EntityType entityType) {
        ItemStack itemStack = new ItemStack(Material.SPAWNER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta)itemMeta;
            CreatureSpawner creatureSpawner2 = (CreatureSpawner)blockStateMeta.getBlockState();
            if (entityType != null) {
                creatureSpawner2.setSpawnedType(entityType);
            }
            blockStateMeta.setBlockState((BlockState)creatureSpawner2);
            itemMeta = blockStateMeta;
        }
        if (entityType != null) {
            itemMeta.setDisplayName("\u00a7e" + this.prettyName(entityType) + " Spawner");
        } else {
            itemMeta.setDisplayName("\u00a7eMonster Spawner");
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private String prettyName(EntityType entityType) {
        String string = entityType.getKey().getKey();
        String[] stringArray = string.split("_");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stringArray.length; ++i) {
            if (stringArray[i].isEmpty()) continue;
            if (i > 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(Character.toUpperCase(stringArray[i].charAt(0)));
            if (stringArray[i].length() <= 1) continue;
            stringBuilder.append(stringArray[i].substring(1).toLowerCase(Locale.ROOT));
        }
        return stringBuilder.toString();
    }

    private void consumeUse(Player player, ItemStack itemStack) {
        int n = this.item.maxUses();
        int n2 = this.item.readUses(itemStack) - 1;
        if (n2 <= 0) {
            itemStack.setAmount(0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            player.sendMessage("\u00a77Your \u00a76Spawner Wrench\u00a77 shattered.");
            return;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(this.item.usesKey(), PersistentDataType.INTEGER, (Object)n2);
        itemMeta.setLore(this.item.buildLore(n2, n));
        itemStack.setItemMeta(itemMeta);
    }
}

