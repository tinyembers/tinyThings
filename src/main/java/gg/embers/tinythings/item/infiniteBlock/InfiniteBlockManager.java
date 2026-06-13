/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.brcdev.shopgui.ShopGuiPlusApi
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.format.TextDecoration
 *  net.milkbowl.vault.economy.Economy
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataContainer
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 */
package gg.embers.tinythings.item.infiniteBlock;

import gg.embers.tinythings.TinyThings;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.StonecutterInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class InfiniteBlockManager
implements Listener {
    private static final int CURRENT_VERSION = 0;
    private final NamespacedKey versionKey = new NamespacedKey("tinyembers", "infinite_block_ver");
    private final NamespacedKey infBlockKey = new NamespacedKey("tinyembers", "infinite_block");
    private final TinyThings plugin;
    private final DecimalFormat df = new DecimalFormat("#,###.##");
    private HashSet<Material> blacklist = new HashSet();
    private CostMode costMode = CostMode.PERCENTAGE;
    private double costPercentage = 0.1;
    private double costFlat = 10.0;
    private double costFallback = 10.0;

    public InfiniteBlockManager(TinyThings tinyThings) {
        this.plugin = tinyThings;
        this.reload();
    }

    public void reload() {
        this.blacklist = this.readBlacklist();
        String string = this.plugin.getConfig().getString("infinite-block.cost.mode", "percentage");
        this.costMode = "flat".equalsIgnoreCase(string) ? CostMode.FLAT : CostMode.PERCENTAGE;
        this.costPercentage = this.plugin.getConfig().getDouble("infinite-block.cost.percentage", 0.1);
        this.costFlat = this.plugin.getConfig().getDouble("infinite-block.cost.flat", 10.0);
        this.costFallback = this.plugin.getConfig().getDouble("infinite-block.cost.fallback", 10.0);
    }

    private HashSet<Material> readBlacklist() {
        HashSet<Material> hashSet = new HashSet<Material>();
        for (String string : this.plugin.getConfig().getStringList("infinite-block.blacklist")) {
            Material material = Material.matchMaterial((String)string);
            if (material == null) continue;
            hashSet.add(material);
        }
        return hashSet;
    }

    public boolean isItemInfinite(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }
        PersistentDataContainer persistentDataContainer = itemStack.getItemMeta().getPersistentDataContainer();
        if (!persistentDataContainer.has(this.infBlockKey)) {
            return false;
        }
        if (persistentDataContainer.has(this.versionKey)) {
            Integer n = (Integer)persistentDataContainer.get(this.versionKey, PersistentDataType.INTEGER);
            if (n != null) {
                this.updateItem(itemStack, n);
            }
        } else {
            this.plugin.getLogger().warning("Infinite item without version key.");
        }
        return true;
    }

    private boolean hasInfiniteTag(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }
        return itemStack.getItemMeta().getPersistentDataContainer().has(this.infBlockKey);
    }

    private boolean matrixContainsInfinite(ItemStack[] matrix) {
        if (matrix == null) {
            return false;
        }
        for (ItemStack itemStack : matrix) {
            if (this.hasInfiniteTag(itemStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean stationInputsInfinite(Inventory inventory) {
        if (inventory instanceof StonecutterInventory stonecutterInventory) {
            return this.hasInfiniteTag(stonecutterInventory.getInputItem());
        }
        if (inventory instanceof GrindstoneInventory grindstoneInventory) {
            return this.hasInfiniteTag(grindstoneInventory.getUpperItem()) || this.hasInfiniteTag(grindstoneInventory.getLowerItem());
        }
        if (inventory instanceof SmithingInventory smithingInventory) {
            return this.hasInfiniteTag(smithingInventory.getInputTemplate()) || this.hasInfiniteTag(smithingInventory.getInputEquipment()) || this.hasInfiniteTag(smithingInventory.getInputMineral());
        }
        return false;
    }

    private int stationResultSlot(Inventory inventory) {
        if (inventory instanceof StonecutterInventory) {
            return 1;
        }
        if (inventory instanceof GrindstoneInventory) {
            return 2;
        }
        if (inventory instanceof SmithingInventory) {
            return 3;
        }
        return -1;
    }

    private void notifyBlocked(HumanEntity humanEntity) {
        if (humanEntity instanceof Player player) {
            player.sendActionBar(Component.text((String)"Infinite blocks can't be used in crafting!").color((TextColor)NamedTextColor.RED));
        }
    }

    private boolean updateItem(ItemStack itemStack, int n) {
        boolean bl = false;
        while (n < 0) {
            bl = true;
            ++n;
        }
        if (bl) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.getPersistentDataContainer().set(this.versionKey, PersistentDataType.INTEGER, n);
            itemStack.setItemMeta(itemMeta);
        }
        return bl;
    }

    public boolean isItemBlacklisted(ItemStack itemStack) {
        return this.blacklist.contains(itemStack.getType());
    }

    public boolean createInfiniteItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        persistentDataContainer.set(this.infBlockKey, PersistentDataType.BOOLEAN, true);
        persistentDataContainer.set(this.versionKey, PersistentDataType.INTEGER, 0);
        String string = InfiniteBlockManager.capitalizeFully(itemStack.getType().toString().replace("_", " "));
        itemMeta.itemName(Component.text((String)("Infinite " + string)).color((TextColor)NamedTextColor.GREEN));
        itemMeta.lore(List.of((TextComponent)((TextComponent)((TextComponent)((TextComponent)Component.text((String)"An infinite ").append(Component.text((String)string).decorate(TextDecoration.BOLD))).append((Component)Component.text((String)(". " + this.costDescription())))).decoration(TextDecoration.ITALIC, false)).color((TextColor)NamedTextColor.GRAY)));
        itemStack.setItemMeta(itemMeta);
        return true;
    }

    private String costDescription() {
        if (this.costMode == CostMode.FLAT) {
            return "Costs $" + this.df.format(this.costFlat) + " to place.";
        }
        return "Costs " + this.df.format(this.costPercentage * 100.0) + "% of the shop price to place.";
    }

    private double calculateCost(ItemStack itemStack) {
        if (this.costMode == CostMode.FLAT) {
            return Math.max(0.0, this.costFlat);
        }
        double d = this.lookupShopPrice(itemStack);
        if (d <= 0.0) {
            return Math.max(0.0, this.costFallback);
        }
        return Math.max(0.0, d * this.costPercentage);
    }

    private double lookupShopPrice(ItemStack itemStack) {
        if (Bukkit.getPluginManager().getPlugin("ShopGUIPlus") == null) {
            return 0.0;
        }
        try {
            if (!ShopGuiPlusApi.getPlugin().getShopManager().areShopsLoaded()) {
                return 0.0;
            }
            return ShopGuiPlusApi.getItemStackPriceBuy((ItemStack)ItemStack.of((Material)itemStack.getType(), (int)1));
        }
        catch (Throwable throwable) {
            return 0.0;
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        if (blockPlaceEvent.isCancelled()) {
            return;
        }
        ItemStack itemStack = blockPlaceEvent.getItemInHand().clone();
        itemStack.setAmount(1);
        if (!this.isItemInfinite(itemStack)) {
            return;
        }
        Player player = blockPlaceEvent.getPlayer();
        if (blockPlaceEvent.getHand() == EquipmentSlot.OFF_HAND) {
            blockPlaceEvent.setCancelled(true);
            player.sendActionBar(Component.text((String)"Infinite blocks can only be placed from your main hand!").color((TextColor)NamedTextColor.RED));
            return;
        }
        if (this.isItemBlacklisted(itemStack)) {
            blockPlaceEvent.setCancelled(true);
            this.plugin.getLogger().warning(player.getName() + " tried placing a blacklisted infinite of " + String.valueOf(itemStack.getType()) + " at " + String.valueOf(blockPlaceEvent.getBlock().getLocation()));
            return;
        }
        double d = this.calculateCost(itemStack);
        Economy economy = this.plugin.getEconomy();
        if (economy == null) {
            blockPlaceEvent.setCancelled(true);
            this.plugin.getLogger().warning("Vault economy unavailable; cancelled infinite placement.");
            return;
        }
        EquipmentSlot equipmentSlot = blockPlaceEvent.getHand();
        if (economy.getBalance((OfflinePlayer)player) >= d) {
            if (d > 0.0) {
                economy.withdrawPlayer((OfflinePlayer)player, d);
            }
            player.sendActionBar((Component)Component.text((String)String.format("Placed infinite block for $%s!", this.df.format(d))));
            ItemStack itemStack2 = player.getInventory().getItem(equipmentSlot);
            ItemStack itemStack3 = itemStack2 == null ? null : itemStack2.clone();
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> player.getInventory().setItem(equipmentSlot, itemStack3));
        } else {
            player.sendMessage("\u00a7cYou do not have enough money to place this!");
            blockPlaceEvent.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPrepareCraft(PrepareItemCraftEvent prepareItemCraftEvent) {
        CraftingInventory craftingInventory = prepareItemCraftEvent.getInventory();
        if (craftingInventory.getResult() == null) {
            return;
        }
        if (this.matrixContainsInfinite(craftingInventory.getMatrix())) {
            craftingInventory.setResult(null);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onCraftItem(CraftItemEvent craftItemEvent) {
        if (!this.matrixContainsInfinite(craftItemEvent.getInventory().getMatrix())) {
            return;
        }
        craftItemEvent.setCancelled(true);
        this.notifyBlocked(craftItemEvent.getWhoClicked());
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockCook(BlockCookEvent blockCookEvent) {
        if (this.hasInfiniteTag(blockCookEvent.getSource())) {
            blockCookEvent.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPrepareSmithing(PrepareSmithingEvent prepareSmithingEvent) {
        if (prepareSmithingEvent.getResult() == null) {
            return;
        }
        if (this.stationInputsInfinite(prepareSmithingEvent.getInventory())) {
            prepareSmithingEvent.setResult(null);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPrepareGrindstone(PrepareGrindstoneEvent prepareGrindstoneEvent) {
        if (prepareGrindstoneEvent.getResult() == null) {
            return;
        }
        if (this.stationInputsInfinite(prepareGrindstoneEvent.getInventory())) {
            prepareGrindstoneEvent.setResult(null);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onResultStationClick(InventoryClickEvent inventoryClickEvent) {
        int resultSlot = this.stationResultSlot(inventoryClickEvent.getInventory());
        if (resultSlot < 0 || inventoryClickEvent.getRawSlot() != resultSlot) {
            return;
        }
        if (!this.stationInputsInfinite(inventoryClickEvent.getInventory())) {
            return;
        }
        inventoryClickEvent.setCancelled(true);
        this.notifyBlocked(inventoryClickEvent.getWhoClicked());
    }

    private static String capitalizeFully(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        String[] stringArray = string.toLowerCase(Locale.ROOT).split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        for (String string2 : stringArray) {
            if (string2.isEmpty()) continue;
            stringBuilder.append(Character.toUpperCase(string2.charAt(0))).append(string2.substring(1)).append(' ');
        }
        return stringBuilder.toString().trim();
    }

    public List<String> blacklistNames() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (Material material : this.blacklist) {
            arrayList.add(material.name());
        }
        return arrayList;
    }

    private static enum CostMode {
        PERCENTAGE,
        FLAT;

    }
}

