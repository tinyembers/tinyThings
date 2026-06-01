package gg.embers.tinythings.item.infiniteBucket;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.TinyItem;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Shared base for the infinite buckets (water, lava, powder snow). Each one is an
 * ordinary filled bucket that never empties when used — see {@link InfiniteBucketListener}.
 */
public abstract class InfiniteBucketItem implements TinyItem {
    protected final TinyThings plugin;
    private final String id;
    private final String displayName;
    private final String coloredName;
    private final Material material;
    private final List<String> lore;

    protected InfiniteBucketItem(TinyThings plugin, String id, String displayName, String coloredName, Material material, List<String> lore) {
        this.plugin = plugin;
        this.id = id;
        this.displayName = displayName;
        this.coloredName = coloredName;
        this.material = material;
        this.lore = lore;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String displayName() {
        return this.displayName;
    }

    public Material bucketMaterial() {
        return this.material;
    }

    @Override
    public ItemStack createItem() {
        ItemStack itemStack = new ItemStack(this.material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(this.coloredName);
        itemMeta.setLore(this.lore);
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(this.plugin.itemIdKey(), PersistentDataType.STRING, this.id);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /** True if the stack is one of this plugin's infinite buckets of this exact material. */
    public boolean matchesBucket(ItemStack itemStack) {
        return this.matches(this.plugin, itemStack) && itemStack.getType() == this.material;
    }

    @Override
    public void register(TinyThings plugin) {
        Bukkit.getPluginManager().registerEvents(new InfiniteBucketListener(this), plugin);
    }
}
