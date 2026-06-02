package gg.embers.tinythings.item.harvestSickle;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.Durability;
import gg.embers.tinythings.item.TinyItem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class HarvestSickleItem implements TinyItem {
    public static final String ID = "harvest_sickle";
    private final TinyThings plugin;
    private final NamespacedKey usesKey;

    public HarvestSickleItem(TinyThings plugin) {
        this.plugin = plugin;
        this.usesKey = new NamespacedKey(plugin, "harvest_sickle_uses");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Harvest Sickle";
    }

    public NamespacedKey usesKey() {
        return this.usesKey;
    }

    public int maxUses() {
        return Math.max(1, this.plugin.getConfig().getInt("harvest-sickle.max-uses", 256));
    }

    public int radius() {
        return Math.max(1, Math.min(4, this.plugin.getConfig().getInt("harvest-sickle.radius", 1)));
    }

    @Override
    public ItemStack createItem() {
        int uses = this.maxUses();
        int size = this.radius() * 2 + 1;
        ItemStack itemStack = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("§aHarvest Sickle");
        itemMeta.setLore(this.buildLore(uses, uses, size));
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(this.plugin.itemIdKey(), PersistentDataType.STRING, ID);
        container.set(this.usesKey, PersistentDataType.INTEGER, uses);
        Durability.apply(itemMeta, uses, uses);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public List<String> buildLore(int remaining, int max, int size) {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Right-click a ripe crop to");
        lore.add("§7harvest and replant a §a" + size + "×" + size);
        lore.add("§7area in one go.");
        lore.add("");
        lore.add("§7Uses left: §e" + remaining + "§7/§e" + max);
        lore.add("§8Breaks when uses run out.");
        return lore;
    }

    public int readUses(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return 0;
        }
        Integer uses = itemStack.getItemMeta().getPersistentDataContainer().get(this.usesKey, PersistentDataType.INTEGER);
        return uses == null ? 0 : uses;
    }

    @Override
    public void register(TinyThings plugin) {
        Bukkit.getPluginManager().registerEvents(new HarvestSickleListener(plugin, this), plugin);
    }
}
