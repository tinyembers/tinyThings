package gg.embers.tinythings.item.biomeStick;

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

public class BiomeStickItem implements TinyItem {
    public static final String ID = "biome_stick";
    private final TinyThings plugin;
    private final NamespacedKey biomeKey;

    public BiomeStickItem(TinyThings plugin) {
        this.plugin = plugin;
        this.biomeKey = new NamespacedKey(plugin, "biome_stick_biome");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Biome Stick";
    }

    public NamespacedKey biomeKey() {
        return this.biomeKey;
    }

    /** Maximum horizontal footprint (X*Z columns) one paint may cover. 0 or less = unlimited. */
    public int maxArea() {
        return this.plugin.getConfig().getInt("biome-stick.max-area", 10000);
    }

    /** Paint the whole vertical column (world bottom to top) so the surface biome actually changes. */
    public boolean fullHeight() {
        return this.plugin.getConfig().getBoolean("biome-stick.full-height", true);
    }

    /** When full-height is false: blocks above and below the clicked corners to include. */
    public int verticalRadius() {
        return Math.max(0, this.plugin.getConfig().getInt("biome-stick.vertical-radius", 16));
    }

    @Override
    public ItemStack createItem() {
        ItemStack stack = new ItemStack(Material.STICK);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName("§aBiome Stick");
        meta.setLore(this.buildLore(null));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.plugin.itemIdKey(), PersistentDataType.STRING, ID);
        stack.setItemMeta(meta);
        return stack;
    }

    public List<String> buildLore(String biomeId) {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Shift + right-click §8» §7open the biome picker");
        lore.add("§7Right-click two corners §8» §7paint the biome");
        lore.add("§7Left-click a block §8» §7copy its biome");
        lore.add("§7Shift + left-click §8» §7clear selection");
        lore.add("");
        lore.add("§7Active biome: " + (biomeId == null ? "§8none" : "§a" + BiomePickerMenu.pretty(biomeId)));
        return lore;
    }

    public String readBiome(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return null;
        }
        return stack.getItemMeta().getPersistentDataContainer().get(this.biomeKey, PersistentDataType.STRING);
    }

    @Override
    public void register(TinyThings plugin) {
        if (Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") == null
                && Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
            plugin.getLogger().warning("Biome Stick disabled: FastAsyncWorldEdit / WorldEdit not found.");
            return;
        }
        Bukkit.getPluginManager().registerEvents(new BiomeStickListener(plugin, this), plugin);
    }
}
