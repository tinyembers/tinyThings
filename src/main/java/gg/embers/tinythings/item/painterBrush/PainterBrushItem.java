package gg.embers.tinythings.item.painterBrush;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.TinyItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PainterBrushItem implements TinyItem {
    public static final String ID = "painter_brush";
    private final TinyThings plugin;
    private final NamespacedKey usesKey;
    private final NamespacedKey paintKey;

    public PainterBrushItem(TinyThings plugin) {
        this.plugin = plugin;
        this.usesKey = new NamespacedKey(plugin, "painter_brush_uses");
        this.paintKey = new NamespacedKey(plugin, "painter_brush_paint");
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Painter Brush";
    }

    public NamespacedKey usesKey() {
        return this.usesKey;
    }

    public NamespacedKey paintKey() {
        return this.paintKey;
    }

    public int maxUses() {
        return Math.max(1, this.plugin.getConfig().getInt("painter-brush.max-uses", 256));
    }

    @Override
    public ItemStack createItem() {
        int uses = this.maxUses();
        ItemStack itemStack = new ItemStack(Material.BRUSH);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("§dPainter Brush");
        itemMeta.setLore(this.buildLore(uses, uses, null));
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.setUnbreakable(true);
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(this.plugin.itemIdKey(), PersistentDataType.STRING, ID);
        container.set(this.usesKey, PersistentDataType.INTEGER, uses);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public List<String> buildLore(int remaining, int max, Material picked) {
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Left-click a block to pick it,");
        lore.add("§7right-click to paint a block");
        lore.add("§7with the picked type.");
        lore.add("");
        lore.add("§7Painting with: §d" + (picked == null ? "nothing" : this.prettyName(picked)));
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

    public Material readPaint(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }
        String name = itemStack.getItemMeta().getPersistentDataContainer().get(this.paintKey, PersistentDataType.STRING);
        return name == null ? null : Material.getMaterial(name);
    }

    private String prettyName(Material material) {
        String[] parts = material.getKey().getKey().split("_");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; ++i) {
            if (parts[i].isEmpty()) {
                continue;
            }
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) {
                builder.append(parts[i].substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return builder.toString();
    }

    @Override
    public void register(TinyThings plugin) {
        Bukkit.getPluginManager().registerEvents(new PainterBrushListener(plugin, this), plugin);
    }
}
