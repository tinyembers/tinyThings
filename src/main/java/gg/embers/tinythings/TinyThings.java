/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.milkbowl.vault.economy.Economy
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.NamespacedKey
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.RegisteredServiceProvider
 *  org.bukkit.plugin.java.JavaPlugin
 */
package gg.embers.tinythings;

import gg.embers.tinythings.command.SizeCommand;
import gg.embers.tinythings.command.TinyThingsCommand;
import gg.embers.tinythings.item.ItemRegistry;
import gg.embers.tinythings.item.buildersWand.BuildersWandItem;
import gg.embers.tinythings.item.excavatorShovel.ExcavatorShovelItem;
import gg.embers.tinythings.item.grapplingHook.GrapplingHookItem;
import gg.embers.tinythings.item.harvestSickle.HarvestSickleItem;
import gg.embers.tinythings.item.infiniteBlock.InfiniteBlockManager;
import gg.embers.tinythings.item.infiniteBucket.InfiniteLavaBucketItem;
import gg.embers.tinythings.item.infiniteBucket.InfiniteSnowBucketItem;
import gg.embers.tinythings.item.infiniteBucket.InfiniteWaterBucketItem;
import gg.embers.tinythings.item.magnetStick.MagnetStickItem;
import gg.embers.tinythings.item.painterBrush.PainterBrushItem;
import gg.embers.tinythings.item.spawnerWrench.SpawnerWrenchItem;
import gg.embers.tinythings.item.superBonemeal.SuperBonemealItem;
import gg.embers.tinythings.item.superSponge.SuperSpongeItem;
import gg.embers.tinythings.item.surveyorStick.SurveyorStickItem;
import gg.embers.tinythings.item.veinPickaxe.VeinPickaxeItem;
import java.io.File;
import java.io.IOException;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class TinyThings
extends JavaPlugin {
    public static final String ITEM_ID_KEY = "tinything_id";
    private NamespacedKey itemIdKey;
    private ItemRegistry registry;
    private File dataFile;
    private FileConfiguration data;
    private Economy economy;
    private InfiniteBlockManager infiniteBlockManager;

    public void onEnable() {
        this.saveDefaultConfig();
        this.itemIdKey = new NamespacedKey((Plugin)this, ITEM_ID_KEY);
        this.dataFile = new File(this.getDataFolder(), "data.yml");
        if (!this.dataFile.exists()) {
            try {
                this.dataFile.getParentFile().mkdirs();
                this.dataFile.createNewFile();
            }
            catch (IOException iOException) {
                this.getLogger().warning("Could not create data.yml: " + iOException.getMessage());
            }
        }
        this.data = YamlConfiguration.loadConfiguration((File)this.dataFile);
        this.registry = new ItemRegistry();
        this.registry.register(new SuperSpongeItem(this));
        this.registry.register(new MagnetStickItem(this));
        this.registry.register(new SurveyorStickItem(this));
        this.registry.register(new SuperBonemealItem(this));
        this.registry.register(new SpawnerWrenchItem(this));
        this.registry.register(new VeinPickaxeItem(this));
        this.registry.register(new ExcavatorShovelItem(this));
        this.registry.register(new BuildersWandItem(this));
        this.registry.register(new PainterBrushItem(this));
        this.registry.register(new HarvestSickleItem(this));
        this.registry.register(new GrapplingHookItem(this));
        this.registry.register(new InfiniteWaterBucketItem(this));
        this.registry.register(new InfiniteLavaBucketItem(this));
        this.registry.register(new InfiniteSnowBucketItem(this));
        this.registry.all().forEach(tinyItem -> tinyItem.register(this));
        this.setupEconomy();
        this.infiniteBlockManager = new InfiniteBlockManager(this);
        Bukkit.getPluginManager().registerEvents((Listener)this.infiniteBlockManager, (Plugin)this);
        TinyThingsCommand tinyThingsCommand = new TinyThingsCommand(this);
        this.getCommand("tinythings").setExecutor((CommandExecutor)tinyThingsCommand);
        this.getCommand("tinythings").setTabCompleter((TabCompleter)tinyThingsCommand);
        SizeCommand sizeCommand = new SizeCommand();
        this.getCommand("size").setExecutor((CommandExecutor)sizeCommand);
        this.getCommand("size").setTabCompleter((TabCompleter)sizeCommand);
        Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.AQUA) + "[tinyThings] " + String.valueOf(ChatColor.GREEN) + "Enabled with " + this.registry.size() + " item(s).");
    }

    public void onDisable() {
        if (this.registry != null) {
            this.registry.all().forEach(tinyItem -> tinyItem.unregister(this));
        }
        this.saveData();
        Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.AQUA) + "[tinyThings] " + String.valueOf(ChatColor.RED) + "Disabled.");
    }

    public NamespacedKey itemIdKey() {
        return this.itemIdKey;
    }

    public ItemRegistry registry() {
        return this.registry;
    }

    public FileConfiguration data() {
        return this.data;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public InfiniteBlockManager infiniteBlockManager() {
        return this.infiniteBlockManager;
    }

    public void saveData() {
        if (this.data == null || this.dataFile == null) {
            return;
        }
        try {
            this.data.save(this.dataFile);
        }
        catch (IOException iOException) {
            this.getLogger().warning("Could not save data.yml: " + iOException.getMessage());
        }
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            this.getLogger().warning("Vault not found \u2014 infinite block placement costs will be disabled.");
            return;
        }
        RegisteredServiceProvider registeredServiceProvider = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (registeredServiceProvider == null) {
            this.getLogger().warning("No Vault economy provider registered \u2014 infinite block placement costs will be disabled.");
            return;
        }
        this.economy = (Economy)registeredServiceProvider.getProvider();
    }
}

