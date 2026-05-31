/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.event.Listener
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataContainer
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 */
package gg.embers.tinythings.item.spawnerWrench;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.TinyItem;
import gg.embers.tinythings.item.spawnerWrench.SpawnerWrenchListener;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class SpawnerWrenchItem
implements TinyItem {
    public static final String ID = "spawner_wrench";
    private final TinyThings plugin;
    private final NamespacedKey usesKey;

    public SpawnerWrenchItem(TinyThings tinyThings) {
        this.plugin = tinyThings;
        this.usesKey = new NamespacedKey((Plugin)tinyThings, "spawner_wrench_uses");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Spawner Wrench";
    }

    public NamespacedKey usesKey() {
        return this.usesKey;
    }

    public int maxUses() {
        return Math.max(1, this.plugin.getConfig().getInt("spawner-wrench.max-uses", 5));
    }

    @Override
    public ItemStack createItem() {
        int n = this.maxUses();
        ItemStack itemStack = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("\u00a76Spawner Wrench");
        itemMeta.setLore(this.buildLore(n, n));
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
        itemMeta.setUnbreakable(true);
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        persistentDataContainer.set(this.plugin.itemIdKey(), PersistentDataType.STRING, (Object)ID);
        persistentDataContainer.set(this.usesKey, PersistentDataType.INTEGER, (Object)n);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public List<String> buildLore(int n, int n2) {
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("\u00a77Right-click a spawner to");
        arrayList.add("\u00a77mine it (keeps its mob type).");
        arrayList.add("");
        arrayList.add("\u00a77Uses left: \u00a7e" + n + "\u00a77/\u00a7e" + n2);
        arrayList.add("\u00a78Breaks when uses run out.");
        return arrayList;
    }

    public int readUses(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return 0;
        }
        Integer n = (Integer)itemStack.getItemMeta().getPersistentDataContainer().get(this.usesKey, PersistentDataType.INTEGER);
        return n == null ? 0 : n;
    }

    @Override
    public void register(TinyThings tinyThings) {
        Bukkit.getPluginManager().registerEvents((Listener)new SpawnerWrenchListener(tinyThings, this), (Plugin)tinyThings);
    }
}

