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
package gg.embers.tinythings.item.surveyorStick;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.TinyItem;
import gg.embers.tinythings.item.surveyorStick.SurveyorStickListener;
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

public class SurveyorStickItem
implements TinyItem {
    public static final String ID = "surveyor_stick";
    private final TinyThings plugin;

    public SurveyorStickItem(TinyThings tinyThings) {
        this.plugin = tinyThings;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Surveyor's Stick";
    }

    @Override
    public ItemStack createItem() {
        ItemStack itemStack = new ItemStack(Material.STICK);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("\u00a7eSurveyor's Stick");
        itemMeta.setLore(List.of("\u00a77Right-click two blocks to measure", "\u00a77distance, area and volume.", "\u00a77Shift + right-click to reset."));
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES});
        itemMeta.getPersistentDataContainer().set(this.plugin.itemIdKey(), PersistentDataType.STRING, (Object)ID);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void register(TinyThings tinyThings) {
        Bukkit.getPluginManager().registerEvents((Listener)new SurveyorStickListener(tinyThings, this), (Plugin)tinyThings);
    }
}

