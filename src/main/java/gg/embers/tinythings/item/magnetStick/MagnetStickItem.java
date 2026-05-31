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
package gg.embers.tinythings.item.magnetStick;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.TinyItem;
import gg.embers.tinythings.item.magnetStick.MagnetStickListener;
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

public class MagnetStickItem
implements TinyItem {
    public static final String ID = "magnet_stick";
    private final TinyThings plugin;

    public MagnetStickItem(TinyThings tinyThings) {
        this.plugin = tinyThings;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Magnet Stick";
    }

    @Override
    public ItemStack createItem() {
        ItemStack itemStack = new ItemStack(Material.STICK);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("\u00a7dMagnet Stick");
        int n = this.plugin.getConfig().getInt("magnet-stick.radius", 8);
        int n2 = this.plugin.getConfig().getInt("magnet-stick.duration-ticks", 100);
        int n3 = this.plugin.getConfig().getInt("magnet-stick.cooldown-ticks", 200);
        itemMeta.setLore(List.of("\u00a77Right-click to pull nearby items", "\u00a77Radius: \u00a7d" + n + "\u00a77 blocks", "\u00a77Duration: \u00a7d" + (double)n2 / 20.0 + "\u00a77s", "\u00a77Cooldown: \u00a7d" + (double)n3 / 20.0 + "\u00a77s"));
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES});
        itemMeta.getPersistentDataContainer().set(this.plugin.itemIdKey(), PersistentDataType.STRING, (Object)ID);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void register(TinyThings tinyThings) {
        Bukkit.getPluginManager().registerEvents((Listener)new MagnetStickListener(tinyThings, this), (Plugin)tinyThings);
    }
}

