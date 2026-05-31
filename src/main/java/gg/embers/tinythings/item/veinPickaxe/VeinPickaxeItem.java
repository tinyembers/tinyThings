package gg.embers.tinythings.item.veinPickaxe;

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

public class VeinPickaxeItem implements TinyItem {
    public static final String ID = "vein_pickaxe";
    private final TinyThings plugin;
    private final NamespacedKey usesKey;

    public VeinPickaxeItem(TinyThings plugin) {
        this.plugin = plugin;
        this.usesKey = new NamespacedKey(plugin, "vein_pickaxe_uses");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Vein Pickaxe";
    }

    public NamespacedKey usesKey() {
        return this.usesKey;
    }

    public int maxUses() {
        return Math.max(1, this.plugin.getConfig().getInt("vein-pickaxe.max-uses", 256));
    }

    public int maxBlocks() {
        return Math.max(1, this.plugin.getConfig().getInt("vein-pickaxe.max-blocks", 64));
    }

    @Override
    public ItemStack createItem() {
        int uses = this.maxUses();
        ItemStack itemStack = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("§bVein Pickaxe");
        itemMeta.setLore(this.buildLore(uses, uses));
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.setUnbreakable(true);
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(this.plugin.itemIdKey(), PersistentDataType.STRING, ID);
        container.set(this.usesKey, PersistentDataType.INTEGER, uses);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public List<String> buildLore(int remaining, int max) {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Mines the entire connected");
        lore.add("§7ore vein in a single break.");
        lore.add("");
        lore.add("§7Sneak to mine one block.");
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
        Bukkit.getPluginManager().registerEvents(new VeinPickaxeListener(plugin, this), plugin);
    }
}
