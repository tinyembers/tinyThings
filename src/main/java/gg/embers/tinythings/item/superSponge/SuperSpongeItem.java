/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Listener
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 */
package gg.embers.tinythings.item.superSponge;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.TinyItem;
import gg.embers.tinythings.item.superSponge.SuperSpongeListener;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class SuperSpongeItem
implements TinyItem {
    public static final String ID = "super_sponge";
    private final TinyThings plugin;
    private SuperSpongeListener listener;

    public SuperSpongeItem(TinyThings tinyThings) {
        this.plugin = tinyThings;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Super Sponge";
    }

    @Override
    public ItemStack createItem() {
        return this.build(Material.SPONGE, "\u00a7bSuper Sponge");
    }

    public ItemStack createWetItem() {
        return this.build(Material.WET_SPONGE, "\u00a7bSuper Sponge (Wet)");
    }

    public boolean isDry(ItemStack itemStack) {
        return this.matches(this.plugin, itemStack) && itemStack.getType() == Material.SPONGE;
    }

    public boolean isWet(ItemStack itemStack) {
        return this.matches(this.plugin, itemStack) && itemStack.getType() == Material.WET_SPONGE;
    }

    private ItemStack build(Material material, String string) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(string);
        int n = this.plugin.getConfig().getInt("super-sponge.max-blocks", 500);
        itemMeta.setLore(List.of("\u00a77Absorbs up to \u00a7b" + n + "\u00a77 water blocks"));
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
        itemMeta.getPersistentDataContainer().set(this.plugin.itemIdKey(), PersistentDataType.STRING, ID);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public void updateLoreOfOnlinePlayers() {
        int n = this.plugin.getConfig().getInt("super-sponge.max-blocks", 500);
        String string = "\u00a77Absorbs up to \u00a7b" + n + "\u00a77 water blocks";
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack == null || !this.matches(this.plugin, itemStack)) continue;
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setLore(List.of(string));
                itemStack.setItemMeta(itemMeta);
            }
        }
    }

    @Override
    public void register(TinyThings tinyThings) {
        this.listener = new SuperSpongeListener(tinyThings, this);
        Bukkit.getPluginManager().registerEvents((Listener)this.listener, (Plugin)tinyThings);
    }

    @Override
    public void unregister(TinyThings tinyThings) {
        if (this.listener != null) {
            this.listener.persist();
        }
    }

    @Override
    public void reload(TinyThings tinyThings) {
        this.updateLoreOfOnlinePlayers();
    }
}

