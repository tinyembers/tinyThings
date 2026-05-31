package gg.embers.tinythings.item.excavatorShovel;

import gg.embers.tinythings.TinyThings;
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

public class ExcavatorShovelItem implements TinyItem {
    public static final String ID = "excavator_shovel";
    private final TinyThings plugin;
    private final NamespacedKey usesKey;

    public ExcavatorShovelItem(TinyThings plugin) {
        this.plugin = plugin;
        this.usesKey = new NamespacedKey(plugin, "excavator_shovel_uses");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Excavator Shovel";
    }

    public NamespacedKey usesKey() {
        return this.usesKey;
    }

    public int maxUses() {
        return Math.max(1, this.plugin.getConfig().getInt("excavator-shovel.max-uses", 256));
    }

    public int radius() {
        return Math.max(1, Math.min(4, this.plugin.getConfig().getInt("excavator-shovel.radius", 1)));
    }

    @Override
    public ItemStack createItem() {
        int uses = this.maxUses();
        int size = this.radius() * 2 + 1;
        ItemStack itemStack = new ItemStack(Material.DIAMOND_SHOVEL);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("§3Excavator Shovel");
        itemMeta.setLore(this.buildLore(uses, uses, size));
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.setUnbreakable(true);
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(this.plugin.itemIdKey(), PersistentDataType.STRING, ID);
        container.set(this.usesKey, PersistentDataType.INTEGER, uses);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public List<String> buildLore(int remaining, int max, int size) {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Digs a §3" + size + "×" + size + "§7 area of soft");
        lore.add("§7terrain in one swing.");
        lore.add("");
        lore.add("§7Sneak to dig one block.");
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
        Bukkit.getPluginManager().registerEvents(new ExcavatorShovelListener(plugin, this), plugin);
    }
}
