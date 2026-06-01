/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.event.Listener
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 */
package gg.embers.tinythings.item.superBonemeal;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.TinyItem;
import gg.embers.tinythings.item.superBonemeal.SuperBonemealListener;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class SuperBonemealItem
implements TinyItem {
    public static final String ID = "super_bonemeal";
    private final TinyThings plugin;

    public SuperBonemealItem(TinyThings tinyThings) {
        this.plugin = tinyThings;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Super Bonemeal";
    }

    @Override
    public ItemStack createItem() {
        ItemStack itemStack = new ItemStack(Material.BONE_MEAL);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("\u00a76Super Bonemeal");
        int n = this.plugin.getConfig().getInt("super-bonemeal.radius", 2);
        int n2 = n * 2 + 1;
        itemMeta.setLore(List.of("\u00a77Grows everything in a \u00a76" + n2 + "\u00d7" + n2 + "\u00a77 area", "\u00a77around the clicked block.", "\u00a77Consumes one per use."));
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES});
        itemMeta.getPersistentDataContainer().set(this.plugin.itemIdKey(), PersistentDataType.STRING, ID);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void register(TinyThings tinyThings) {
        Bukkit.getPluginManager().registerEvents((Listener)new SuperBonemealListener(tinyThings, this), (Plugin)tinyThings);
    }
}

