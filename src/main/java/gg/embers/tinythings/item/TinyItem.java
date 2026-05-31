/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 */
package gg.embers.tinythings.item;

import gg.embers.tinythings.TinyThings;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public interface TinyItem {
    public String id();

    public String displayName();

    public ItemStack createItem();

    default public boolean matches(TinyThings tinyThings, ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        String string = (String)itemMeta.getPersistentDataContainer().get(tinyThings.itemIdKey(), PersistentDataType.STRING);
        return this.id().equals(string);
    }

    default public void register(TinyThings tinyThings) {
    }

    default public void unregister(TinyThings tinyThings) {
    }

    default public void reload(TinyThings tinyThings) {
    }
}

