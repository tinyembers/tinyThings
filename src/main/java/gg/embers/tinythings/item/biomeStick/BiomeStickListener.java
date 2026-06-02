package gg.embers.tinythings.item.biomeStick;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import gg.embers.tinythings.TinyThings;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class BiomeStickListener implements Listener {
    private static final String PERMISSION = "tinythings.biomestick.use";

    private final TinyThings plugin;
    private final BiomeStickItem item;
    private final Map<UUID, Location> corners = new HashMap<>();
    private final boolean async;

    public BiomeStickListener(TinyThings plugin, BiomeStickItem item) {
        this.plugin = plugin;
        this.item = item;
        // FAWE makes off-thread world edits safe; plain WorldEdit does not, so fall back to sync.
        this.async = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        ItemStack held = event.getItem();
        if (!this.item.matches(this.plugin, held)) {
            return;
        }
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage("§cYou don't have permission to use the Biome Stick.");
            return;
        }

        Action action = event.getAction();
        boolean sneaking = player.isSneaking();

        if (sneaking && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            BiomePickerMenu.open(player, 0, this.item.readBiome(held));
            return;
        }
        if (sneaking && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
            if (this.corners.remove(player.getUniqueId()) != null) {
                player.sendMessage("§eBiome Stick §8» §7selection cleared.");
            } else {
                player.sendMessage("§eBiome Stick §8» §7no selection to clear.");
            }
            return;
        }
        if (!sneaking && action == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null) {
                copyBiome(player, held, block);
            }
            return;
        }
        if (!sneaking && action == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null) {
                handleCorner(player, held, block);
            }
        }
    }

    private void copyBiome(Player player, ItemStack held, Block block) {
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(block.getWorld());
        BiomeType biome = weWorld.getBiome(BlockVector3.at(block.getX(), block.getY(), block.getZ()));
        if (biome == null) {
            player.sendMessage("§cCouldn't read the biome at that block.");
            return;
        }
        setActiveBiome(player, held, biome.id());
        player.sendMessage("§aBiome Stick §8» §7copied §f" + BiomePickerMenu.pretty(biome.id()) + "§7.");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.6f);
    }

    private void handleCorner(Player player, ItemStack held, Block block) {
        String biomeId = this.item.readBiome(held);
        if (biomeId == null) {
            player.sendMessage("§cPick a biome first §7(shift + right-click to open the picker).");
            return;
        }
        UUID uuid = player.getUniqueId();
        Location clicked = block.getLocation();
        Location first = this.corners.get(uuid);
        if (first == null || !clicked.getWorld().equals(first.getWorld())) {
            this.corners.put(uuid, clicked);
            player.sendMessage("§aBiome Stick §8» §7corner §aA§7 set at §f" + clicked.getBlockX() + ", "
                    + clicked.getBlockZ() + "§7. Right-click the opposite corner.");
            player.playSound(clicked, Sound.UI_BUTTON_CLICK, 0.5f, 1.4f);
            return;
        }
        this.corners.remove(uuid);
        applyBiome(player, first, clicked, biomeId);
    }

    private void applyBiome(Player player, Location a, Location b, String biomeId) {
        BiomeType biome = BiomeTypes.get(biomeId);
        if (biome == null) {
            player.sendMessage("§cUnknown biome: §f" + biomeId);
            return;
        }
        org.bukkit.World bukkitWorld = a.getWorld();
        int minX = Math.min(a.getBlockX(), b.getBlockX());
        int maxX = Math.max(a.getBlockX(), b.getBlockX());
        int minZ = Math.min(a.getBlockZ(), b.getBlockZ());
        int maxZ = Math.max(a.getBlockZ(), b.getBlockZ());

        long footprint = (long) (maxX - minX + 1) * (long) (maxZ - minZ + 1);
        int maxArea = this.item.maxArea();
        if (maxArea > 0 && footprint > maxArea) {
            player.sendMessage("§cThat region is too big: §f" + footprint + "§c columns (limit §f" + maxArea
                    + "§c). Raise §ebiome-stick.max-area§c in the config to allow larger areas.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
            return;
        }

        int minY;
        int maxY;
        if (this.item.fullHeight()) {
            minY = bukkitWorld.getMinHeight();
            maxY = bukkitWorld.getMaxHeight() - 1;
        } else {
            int radius = this.item.verticalRadius();
            minY = Math.max(bukkitWorld.getMinHeight(), Math.min(a.getBlockY(), b.getBlockY()) - radius);
            maxY = Math.min(bukkitWorld.getMaxHeight() - 1, Math.max(a.getBlockY(), b.getBlockY()) + radius);
        }

        final com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);
        final BlockVector3 min = BlockVector3.at(minX, minY, minZ);
        final BlockVector3 max = BlockVector3.at(maxX, maxY, maxZ);
        final String pretty = BiomePickerMenu.pretty(biomeId);
        final long columns = footprint;

        player.sendMessage("§aBiome Stick §8» §7painting §f" + pretty + "§7 over §f" + columns + "§7 columns…");

        Runnable task = () -> {
            try (EditSession edit = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(weWorld).maxBlocks(-1).build()) {
                Region region = new CuboidRegion(weWorld, min, max);
                RegionFunction replace = new BiomeReplace(edit, biome);
                RegionVisitor visitor = new RegionVisitor(region, replace);
                Operations.completeBlindly(visitor);
            } catch (Throwable t) {
                this.plugin.getLogger().warning("Biome Stick edit failed: " + t.getMessage());
                Bukkit.getScheduler().runTask(this.plugin,
                        () -> player.sendMessage("§cBiome paint failed: §f" + t.getMessage()));
                return;
            }
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                player.sendMessage("§aBiome Stick §8» §7done! Set §f" + pretty + "§7 across §f" + columns + "§7 columns.");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            });
        };

        if (this.async) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task);
        } else {
            task.run();
        }
    }

    private void setActiveBiome(Player player, ItemStack held, String biomeId) {
        ItemMeta meta = held.getItemMeta();
        meta.getPersistentDataContainer().set(this.item.biomeKey(), PersistentDataType.STRING, biomeId);
        meta.setLore(this.item.buildLore(biomeId));
        held.setItemMeta(meta);
        // held is always the main-hand stick (interact path uses event.getItem(), menu path
        // re-reads the main hand); write it back so the change persists even if the API
        // handed us a copy rather than the live stack.
        player.getInventory().setItemInMainHand(held);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BiomePickerMenu.Holder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        // Only react to clicks inside the menu itself, not the player's own inventory.
        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }
        int slot = event.getSlot();
        if (slot == BiomePickerMenu.SLOT_PREV) {
            BiomePickerMenu.open(player, holder.page() - 1, currentActive(player));
            return;
        }
        if (slot == BiomePickerMenu.SLOT_NEXT) {
            BiomePickerMenu.open(player, holder.page() + 1, currentActive(player));
            return;
        }
        String biomeId = holder.biomeAt(slot);
        if (biomeId == null) {
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!this.item.matches(this.plugin, held)) {
            player.sendMessage("§cHold your Biome Stick to pick a biome.");
            player.closeInventory();
            return;
        }
        setActiveBiome(player, held, biomeId);
        player.closeInventory();
        player.sendMessage("§aBiome Stick §8» §7active biome set to §f" + BiomePickerMenu.pretty(biomeId)
                + "§7. Right-click two corners to paint.");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.8f);
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof BiomePickerMenu.Holder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.corners.remove(event.getPlayer().getUniqueId());
    }

    private String currentActive(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        return this.item.matches(this.plugin, held) ? this.item.readBiome(held) : null;
    }
}
