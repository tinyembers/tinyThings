/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockFace
 *  org.bukkit.block.data.BlockData
 *  org.bukkit.block.data.Waterlogged
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.block.SpongeAbsorbEvent
 *  org.bukkit.event.inventory.FurnaceSmeltEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package gg.embers.tinythings.item.superSponge;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.superSponge.SuperSpongeItem;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class SuperSpongeListener
implements Listener {
    private static final BlockFace[] FACES = new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    private static final Set<Material> ABSORBABLE_PLANTS = Set.of(Material.SEAGRASS, Material.TALL_SEAGRASS, Material.KELP, Material.KELP_PLANT, Material.BUBBLE_COLUMN, Material.TUBE_CORAL, Material.BRAIN_CORAL, Material.BUBBLE_CORAL, Material.FIRE_CORAL, Material.HORN_CORAL, Material.TUBE_CORAL_FAN, Material.BRAIN_CORAL_FAN, Material.BUBBLE_CORAL_FAN, Material.FIRE_CORAL_FAN, Material.HORN_CORAL_FAN);
    private static final String DATA_KEY = "super-sponge.locations";
    private final TinyThings plugin;
    private final SuperSpongeItem item;
    private final Map<Location, Player> pendingAbsorb = new HashMap<Location, Player>();
    private final Set<Location> tracked;

    public SuperSpongeListener(TinyThings tinyThings, SuperSpongeItem superSpongeItem) {
        this.plugin = tinyThings;
        this.item = superSpongeItem;
        this.tracked = this.loadLocations();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        Block block = blockPlaceEvent.getBlockPlaced();
        ItemStack itemStack = blockPlaceEvent.getPlayer().getInventory().getItemInMainHand();
        if (block.getType() == Material.SPONGE && this.item.isDry(itemStack)) {
            this.pendingAbsorb.put(block.getLocation(), blockPlaceEvent.getPlayer());
            this.tracked.add(block.getLocation());
        } else if (block.getType() == Material.WET_SPONGE && this.item.isWet(itemStack)) {
            this.tracked.add(block.getLocation());
        }
    }

    @EventHandler
    public void onSpongeAbsorb(SpongeAbsorbEvent spongeAbsorbEvent) {
        Location location = spongeAbsorbEvent.getBlock().getLocation();
        if (!this.pendingAbsorb.containsKey(location)) {
            return;
        }
        spongeAbsorbEvent.getBlocks().clear();
        Player player = this.pendingAbsorb.remove(location);
        int n = this.plugin.getConfig().getInt("super-sponge.max-blocks", 500);
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            int n2 = this.absorbWater(spongeAbsorbEvent.getBlock(), n);
            if (n2 > 0) {
                spongeAbsorbEvent.getBlock().setType(Material.WET_SPONGE);
                if (player != null && player.isOnline()) {
                    player.sendMessage("\u00a7bSuper Sponge\u00a77 absorbed \u00a7b" + n2 + "\u00a77 blocks of water.");
                }
            }
        });
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        Location location = blockBreakEvent.getBlock().getLocation();
        if (!this.tracked.contains(location)) {
            return;
        }
        blockBreakEvent.setDropItems(false);
        this.tracked.remove(location);
        ItemStack itemStack = blockBreakEvent.getBlock().getType() == Material.WET_SPONGE ? this.item.createWetItem() : this.item.createItem();
        blockBreakEvent.getBlock().getWorld().dropItemNaturally(location, itemStack);
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent furnaceSmeltEvent) {
        if (!this.item.isWet(furnaceSmeltEvent.getSource())) {
            return;
        }
        furnaceSmeltEvent.setResult(this.item.createItem());
    }

    public void persist() {
        ArrayList<CallSite> arrayList = new ArrayList<CallSite>();
        for (Location location : this.tracked) {
            if (location.getWorld() == null) continue;
            arrayList.add((CallSite)((Object)(location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ())));
        }
        this.plugin.data().set(DATA_KEY, arrayList);
        this.plugin.saveData();
    }

    private Set<Location> loadLocations() {
        HashSet<Location> hashSet = new HashSet<Location>();
        if (!this.plugin.data().isList(DATA_KEY)) {
            return hashSet;
        }
        for (String string : this.plugin.data().getStringList(DATA_KEY)) {
            World world;
            String[] stringArray = string.split(",");
            if (stringArray.length != 4 || (world = Bukkit.getWorld((String)stringArray[0])) == null) continue;
            try {
                hashSet.add(new Location(world, Double.parseDouble(stringArray[1]), Double.parseDouble(stringArray[2]), Double.parseDouble(stringArray[3])));
            }
            catch (NumberFormatException numberFormatException) {}
        }
        return hashSet;
    }

    private int absorbWater(Block block, int n) {
        LinkedList<Block> linkedList = new LinkedList<Block>();
        HashSet<Block> hashSet = new HashSet<Block>();
        linkedList.add(block);
        hashSet.add(block);
        int n2 = 0;
        while (!linkedList.isEmpty() && n2 < n) {
            Block block2 = (Block)linkedList.poll();
            for (BlockFace blockFace : FACES) {
                Waterlogged waterlogged;
                Block block3 = block2.getRelative(blockFace);
                if (!hashSet.add(block3)) continue;
                if (block3.getType() == Material.WATER) {
                    block3.setType(Material.AIR);
                    ++n2;
                    linkedList.add(block3);
                    continue;
                }
                BlockData blockData = block3.getBlockData();
                if (blockData instanceof Waterlogged && (waterlogged = (Waterlogged)blockData).isWaterlogged()) {
                    waterlogged.setWaterlogged(false);
                    block3.setBlockData((BlockData)waterlogged);
                    ++n2;
                    linkedList.add(block3);
                    continue;
                }
                if (!ABSORBABLE_PLANTS.contains(block3.getType())) continue;
                block3.setType(Material.AIR);
                ++n2;
                linkedList.add(block3);
            }
        }
        return n2;
    }
}

