package gg.embers.tinythings.item.grapplingHook;

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

public class GrapplingHookItem implements TinyItem {
    public static final String ID = "grappling_hook";
    private final TinyThings plugin;
    private final NamespacedKey usesKey;

    public GrapplingHookItem(TinyThings plugin) {
        this.plugin = plugin;
        this.usesKey = new NamespacedKey(plugin, "grappling_hook_uses");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Grappling Hook";
    }

    public NamespacedKey usesKey() {
        return this.usesKey;
    }

    public int maxUses() {
        return Math.max(1, this.plugin.getConfig().getInt("grappling-hook.max-uses", 128));
    }

    public double power() {
        return Math.max(0.1, this.plugin.getConfig().getDouble("grappling-hook.power", 1.5));
    }

    public double maxDistance() {
        return Math.max(1.0, this.plugin.getConfig().getDouble("grappling-hook.max-distance", 30.0));
    }

    public int cooldownTicks() {
        return Math.max(0, this.plugin.getConfig().getInt("grappling-hook.cooldown-ticks", 20));
    }

    @Override
    public ItemStack createItem() {
        int uses = this.maxUses();
        ItemStack itemStack = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("§eGrappling Hook");
        itemMeta.setLore(this.buildLore(uses, uses));
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(this.plugin.itemIdKey(), PersistentDataType.STRING, ID);
        container.set(this.usesKey, PersistentDataType.INTEGER, uses);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public List<String> buildLore(int remaining, int max) {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Right-click to fling yourself");
        lore.add("§7toward the block you're aiming at.");
        lore.add("§7Range: §e" + (int) this.maxDistance() + "§7 blocks");
        lore.add("§7Cooldown: §e" + (double) this.cooldownTicks() / 20.0 + "§7s");
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
        Bukkit.getPluginManager().registerEvents(new GrapplingHookListener(plugin, this), plugin);
    }
}
