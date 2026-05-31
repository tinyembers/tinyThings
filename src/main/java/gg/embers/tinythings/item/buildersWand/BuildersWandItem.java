package gg.embers.tinythings.item.buildersWand;

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

public class BuildersWandItem implements TinyItem {
    public static final String ID = "builders_wand";
    private final TinyThings plugin;
    private final NamespacedKey usesKey;

    public BuildersWandItem(TinyThings plugin) {
        this.plugin = plugin;
        this.usesKey = new NamespacedKey(plugin, "builders_wand_uses");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Builder's Wand";
    }

    public NamespacedKey usesKey() {
        return this.usesKey;
    }

    public int maxUses() {
        return Math.max(1, this.plugin.getConfig().getInt("builders-wand.max-uses", 256));
    }

    public int radius() {
        return Math.max(1, Math.min(3, this.plugin.getConfig().getInt("builders-wand.radius", 1)));
    }

    @Override
    public ItemStack createItem() {
        int uses = this.maxUses();
        int size = this.radius() * 2 + 1;
        ItemStack itemStack = new ItemStack(Material.BLAZE_ROD);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("§6Builder's Wand");
        itemMeta.setLore(this.buildLore(uses, uses, size));
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(this.plugin.itemIdKey(), PersistentDataType.STRING, ID);
        container.set(this.usesKey, PersistentDataType.INTEGER, uses);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public List<String> buildLore(int remaining, int max, int size) {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Right-click a block face to");
        lore.add("§7extend that surface up to a");
        lore.add("§7" + size + "×" + size + " §7patch, using matching");
        lore.add("§7blocks from your inventory.");
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
        Bukkit.getPluginManager().registerEvents(new BuildersWandListener(plugin, this), plugin);
    }
}
