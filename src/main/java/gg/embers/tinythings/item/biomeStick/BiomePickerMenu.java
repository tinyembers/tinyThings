package gg.embers.tinythings.item.biomeStick;

import com.sk89q.worldedit.world.biome.BiomeType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Paginated chest GUI listing every biome registered with WorldEdit/FAWE — this includes
 * vanilla Minecraft biomes and any datapack biomes such as Terralith, because FAWE mirrors
 * the server's biome registry. Clicking a biome stores it on the held Biome Stick.
 */
public final class BiomePickerMenu {
    public static final int SIZE = 54;
    public static final int PER_PAGE = 45;
    public static final int SLOT_PREV = 45;
    public static final int SLOT_INFO = 49;
    public static final int SLOT_NEXT = 53;

    private BiomePickerMenu() {
    }

    /** Identifies our GUI and remembers which biome each slot maps to. */
    public static final class Holder implements InventoryHolder {
        private final int page;
        private final Map<Integer, String> slotBiomes = new LinkedHashMap<>();
        private Inventory inventory;

        private Holder(int page) {
            this.page = page;
        }

        public int page() {
            return this.page;
        }

        public String biomeAt(int slot) {
            return this.slotBiomes.get(slot);
        }

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }

    private static List<String> allBiomeIds() {
        ArrayList<String> ids = new ArrayList<>();
        for (BiomeType type : BiomeType.REGISTRY) {
            ids.add(type.id());
        }
        ids.sort(String::compareTo);
        return ids;
    }

    public static void open(Player player, int page, String activeId) {
        List<String> ids = allBiomeIds();
        int pages = Math.max(1, (ids.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.max(0, Math.min(page, pages - 1));

        Holder holder = new Holder(page);
        Inventory inv = Bukkit.createInventory(holder, SIZE, "§1Biome Picker §8(" + (page + 1) + "/" + pages + ")");
        holder.inventory = inv;

        int start = page * PER_PAGE;
        int end = Math.min(start + PER_PAGE, ids.size());
        int slot = 0;
        for (int i = start; i < end; ++i, ++slot) {
            String id = ids.get(i);
            inv.setItem(slot, icon(id, id.equals(activeId)));
            holder.slotBiomes.put(slot, id);
        }

        if (page > 0) {
            inv.setItem(SLOT_PREV, nav(Material.ARROW, "§ePrevious page"));
        }
        inv.setItem(SLOT_INFO, nav(Material.PAPER, "§bPage " + (page + 1) + " §7of §b" + pages,
                "§7" + ids.size() + " biomes available"));
        if (page < pages - 1) {
            inv.setItem(SLOT_NEXT, nav(Material.ARROW, "§eNext page"));
        }

        player.openInventory(inv);
    }

    private static ItemStack nav(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(List.of(lore));
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack icon(String id, boolean active) {
        ItemStack stack = new ItemStack(iconFor(id));
        ItemMeta meta = stack.getItemMeta();
        String namespace = id.contains(":") ? id.substring(0, id.indexOf(58)) : "minecraft";
        String color = namespace.equals("minecraft") ? "§a" : (namespace.equals("terralith") ? "§b" : "§6");
        meta.setDisplayName(color + pretty(id) + (active ? " §8(selected)" : ""));
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§8" + id);
        lore.add("");
        lore.add(active ? "§aCurrently selected" : "§7Click to select");
        meta.setLore(lore);
        if (active) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    /** "minecraft:snowy_taiga" -> "Snowy Taiga". */
    public static String pretty(String id) {
        String path = id.contains(":") ? id.substring(id.indexOf(58) + 1) : id;
        String[] parts = path.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return builder.length() == 0 ? id : builder.toString();
    }

    /** Best-effort representative icon so the menu reads at a glance. Defaults to a grass block. */
    private static Material iconFor(String id) {
        String s = id.toLowerCase(Locale.ROOT);
        if (s.contains("ocean") || s.contains("river")) return Material.WATER_BUCKET;
        if (s.contains("snow") || s.contains("frozen") || s.contains("ice") || s.contains("glacial")) return Material.SNOW_BLOCK;
        if (s.contains("badlands") || s.contains("mesa") || s.contains("canyon")) return Material.TERRACOTTA;
        if (s.contains("desert") || s.contains("dune") || s.contains("sand")) return Material.SAND;
        if (s.contains("nether") || s.contains("crimson") || s.contains("soul") || s.contains("basalt")) return Material.NETHERRACK;
        if (s.contains("end") || s.contains("void")) return Material.END_STONE;
        if (s.contains("mushroom") || s.contains("fungal")) return Material.RED_MUSHROOM_BLOCK;
        if (s.contains("cherry") || s.contains("blossom")) return Material.CHERRY_LEAVES;
        if (s.contains("mangrove")) return Material.MANGROVE_LEAVES;
        if (s.contains("jungle") || s.contains("rainforest") || s.contains("bamboo")) return Material.JUNGLE_LEAVES;
        if (s.contains("savanna") || s.contains("shrubland")) return Material.ACACIA_LEAVES;
        if (s.contains("swamp") || s.contains("marsh") || s.contains("bog") || s.contains("wetland")) return Material.LILY_PAD;
        if (s.contains("taiga") || s.contains("pine") || s.contains("spruce") || s.contains("conifer")) return Material.SPRUCE_SAPLING;
        if (s.contains("birch")) return Material.BIRCH_SAPLING;
        if (s.contains("volcanic") || s.contains("lava") || s.contains("ashen") || s.contains("scorch")) return Material.MAGMA_BLOCK;
        if (s.contains("cave") || s.contains("dripstone") || s.contains("lush") || s.contains("deep")) return Material.MOSS_BLOCK;
        if (s.contains("beach") || s.contains("shore") || s.contains("coast")) return Material.SAND;
        if (s.contains("forest") || s.contains("grove") || s.contains("woodland") || s.contains("thicket")) return Material.OAK_LEAVES;
        if (s.contains("mountain") || s.contains("peak") || s.contains("hill") || s.contains("highland")
                || s.contains("alpine") || s.contains("cliff") || s.contains("crag") || s.contains("slope")) return Material.STONE;
        return Material.GRASS_BLOCK;
    }
}
