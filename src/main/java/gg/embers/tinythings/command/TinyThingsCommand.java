/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package gg.embers.tinythings.command;

import gg.embers.tinythings.TinyThings;
import gg.embers.tinythings.item.TinyItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TinyThingsCommand
implements CommandExecutor,
TabCompleter {
    private final TinyThings plugin;

    public TinyThingsCommand(TinyThings tinyThings) {
        this.plugin = tinyThings;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] stringArray) {
        String string2;
        if (stringArray.length == 0) {
            this.sendHelp(commandSender, string);
            return true;
        }
        switch (string2 = stringArray[0].toLowerCase()) {
            case "give": {
                this.handleGive(commandSender, string, stringArray);
                break;
            }
            case "list": {
                this.handleList(commandSender);
                break;
            }
            case "reload": {
                this.handleReload(commandSender);
                break;
            }
            case "make-infinite": 
            case "makeinfinite": 
            case "infinite": {
                this.handleMakeInfinite(commandSender);
                break;
            }
            case "help": {
                this.sendHelp(commandSender, string);
                break;
            }
            default: {
                this.sendHelp(commandSender, string);
            }
        }
        return true;
    }

    private void handleMakeInfinite(CommandSender commandSender) {
        if (!commandSender.hasPermission("tinythings.make-infinite")) {
            commandSender.sendMessage("\u00a7cYou don't have permission to create infinite blocks.");
            return;
        }
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("\u00a7cOnly players can use this command.");
            return;
        }
        Player player = (Player)commandSender;
        ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
        if (itemStack.getType() == Material.AIR) {
            player.sendMessage("\u00a7cHold the item you want to make infinite.");
            return;
        }
        if (this.plugin.infiniteBlockManager().isItemBlacklisted(itemStack)) {
            player.sendMessage("\u00a7c" + String.valueOf(itemStack.getType()) + " is blacklisted and cannot be made infinite.");
            return;
        }
        if (!this.plugin.infiniteBlockManager().createInfiniteItem(itemStack)) {
            player.sendMessage("\u00a7cCould not create an infinite block from that item.");
            return;
        }
        player.getInventory().addItem(new ItemStack[]{itemStack});
        player.sendMessage("\u00a7aCreated infinite block.");
    }

    private void handleGive(CommandSender commandSender, String string, String[] stringArray) {
        Player player;
        if (!commandSender.hasPermission("tinythings.give")) {
            commandSender.sendMessage("\u00a7cYou don't have permission to give items.");
            return;
        }
        if (stringArray.length < 2) {
            commandSender.sendMessage("\u00a7cUsage: /" + string + " give <item> [player]");
            return;
        }
        TinyItem tinyItem = this.plugin.registry().get(stringArray[1]);
        if (tinyItem == null) {
            commandSender.sendMessage("\u00a7cUnknown item: \u00a7e" + stringArray[1] + "\u00a7c. Try \u00a7e/" + string + " list\u00a7c.");
            return;
        }
        if (stringArray.length >= 3) {
            player = Bukkit.getPlayerExact((String)stringArray[2]);
            if (player == null) {
                commandSender.sendMessage("\u00a7cPlayer \u00a7e" + stringArray[2] + "\u00a7c is not online.");
                return;
            }
        } else if (commandSender instanceof Player) {
            Player player2;
            player = player2 = (Player)commandSender;
        } else {
            commandSender.sendMessage("\u00a7cConsole must specify a player: /" + string + " give <item> <player>");
            return;
        }
        player.getInventory().addItem(new ItemStack[]{tinyItem.createItem()});
        player.sendMessage("\u00a7aYou received \u00a7b" + tinyItem.displayName() + "\u00a7a.");
        if (commandSender != player) {
            commandSender.sendMessage("\u00a7aGave \u00a7b" + tinyItem.displayName() + "\u00a7a to \u00a7e" + player.getName() + "\u00a7a.");
        }
    }

    private void handleList(CommandSender commandSender) {
        if (!commandSender.hasPermission("tinythings.list")) {
            commandSender.sendMessage("\u00a7cYou don't have permission to list items.");
            return;
        }
        if (this.plugin.registry().size() == 0) {
            commandSender.sendMessage("\u00a77No items registered.");
            return;
        }
        commandSender.sendMessage("\u00a7aRegistered items (\u00a7b" + this.plugin.registry().size() + "\u00a7a):");
        for (TinyItem tinyItem : this.plugin.registry().all()) {
            commandSender.sendMessage("  \u00a78\u2022 \u00a7e" + tinyItem.id() + " \u00a77\u2014 \u00a7f" + tinyItem.displayName());
        }
    }

    private void handleReload(CommandSender commandSender) {
        if (!commandSender.hasPermission("tinythings.reload")) {
            commandSender.sendMessage("\u00a7cYou don't have permission to reload.");
            return;
        }
        this.plugin.reloadConfig();
        this.plugin.registry().all().forEach(tinyItem -> tinyItem.reload(this.plugin));
        this.plugin.infiniteBlockManager().reload();
        commandSender.sendMessage("\u00a7atinyThings reloaded. \u00a77(" + this.plugin.registry().size() + " items)");
    }

    private void sendHelp(CommandSender commandSender, String string) {
        commandSender.sendMessage("\u00a7b\u00a7ltinyThings \u00a77\u2014 commands:");
        commandSender.sendMessage("  \u00a7e/" + string + " give <item> [player]\u00a77 \u2014 give an item");
        commandSender.sendMessage("  \u00a7e/" + string + " list\u00a77 \u2014 list registered items");
        commandSender.sendMessage("  \u00a7e/" + string + " make-infinite\u00a77 \u2014 turn the held item into an infinite block");
        commandSender.sendMessage("  \u00a7e/" + string + " reload\u00a77 \u2014 reload config");
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String string, String[] stringArray) {
        if (stringArray.length == 1) {
            return this.filter(List.of("give", "list", "make-infinite", "reload", "help"), stringArray[0]);
        }
        if (stringArray.length == 2 && stringArray[0].equalsIgnoreCase("give")) {
            ArrayList<String> arrayList = new ArrayList<String>();
            this.plugin.registry().all().forEach(tinyItem -> arrayList.add(tinyItem.id()));
            return this.filter(arrayList, stringArray[1]);
        }
        if (stringArray.length == 3 && stringArray[0].equalsIgnoreCase("give")) {
            ArrayList<String> arrayList = new ArrayList<String>();
            Bukkit.getOnlinePlayers().forEach(player -> arrayList.add(player.getName()));
            return this.filter(arrayList, stringArray[2]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String string) {
        String string2 = string.toLowerCase();
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string3 : list) {
            if (!string3.toLowerCase().startsWith(string2)) continue;
            arrayList.add(string3);
        }
        return arrayList;
    }
}

