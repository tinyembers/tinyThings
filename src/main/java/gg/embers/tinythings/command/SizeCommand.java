/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.attribute.AttributeInstance
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 */
package gg.embers.tinythings.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class SizeCommand
implements CommandExecutor,
TabCompleter {
    private static final String PERM_USE = "embers.size";
    private static final String PERM_ADMIN = "embers.admin";
    private static final double DEFAULT_SIZE = 1.0;
    private static final double USER_MIN = 0.6;
    private static final double USER_MAX = 3.0;
    private static final double ADMIN_MIN = 0.0;
    private static final double ADMIN_MAX = 16.0;

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] stringArray) {
        Player player;
        double d;
        if (!commandSender.hasPermission(PERM_USE)) {
            commandSender.sendMessage("\u00a7cYou don't have permission to use this command.");
            return true;
        }
        if (stringArray.length == 0) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("\u00a7cConsole must specify a size and player: /" + string + " <size> <player>");
                return true;
            }
            Player player2 = (Player)commandSender;
            this.setScale(player2, 1.0);
            player2.sendMessage("\u00a7aYour size has been reset.");
            return true;
        }
        try {
            d = Double.parseDouble(stringArray[0]);
        }
        catch (NumberFormatException numberFormatException) {
            commandSender.sendMessage("\u00a7c'" + stringArray[0] + "' is not a valid number.");
            return true;
        }
        boolean bl = commandSender.hasPermission(PERM_ADMIN);
        double d2 = bl ? 0.0 : 0.6;
        double d3 = bl ? 16.0 : 3.0;
        double d4 = d3;
        if (d < d2 || d > d3) {
            commandSender.sendMessage("\u00a7cInvalid size, please choose a number between " + this.trim(d2) + " and " + this.trim(d3) + " (inclusive).");
            return true;
        }
        if (stringArray.length >= 2) {
            if (!bl) {
                commandSender.sendMessage("\u00a7cYou don't have permission to change another player's size.");
                return true;
            }
            player = Bukkit.getPlayerExact((String)stringArray[1]);
            if (player == null) {
                commandSender.sendMessage("\u00a7cPlayer \u00a7e" + stringArray[1] + "\u00a7c must be online.");
                return true;
            }
        } else {
            Player player3;
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("\u00a7cConsole must specify a player: /" + string + " <size> <player>");
                return true;
            }
            player = player3 = (Player)commandSender;
        }
        this.setScale(player, d);
        player.sendMessage("\u00a7aYour size has been set to \u00a7e" + this.trim(d) + "\u00a7a!");
        if (commandSender != player) {
            commandSender.sendMessage("\u00a7e" + player.getName() + "'s \u00a7asize has been set to \u00a7e" + this.trim(d) + "\u00a7a!");
        }
        return true;
    }

    private void setScale(Player player, double d) {
        AttributeInstance attributeInstance = player.getAttribute(Attribute.SCALE);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(d);
        }
    }

    private String trim(double d) {
        if (d == Math.floor(d)) {
            return String.valueOf((long)d);
        }
        return String.valueOf(d);
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String string, String[] stringArray) {
        if (!commandSender.hasPermission(PERM_USE)) {
            return Collections.emptyList();
        }
        if (stringArray.length == 1) {
            return commandSender.hasPermission(PERM_ADMIN) ? List.of("0", "0.5", "1", "1.5", "2", "3", "5", "10", "16") : List.of("0.6", "1", "1.5", "2", "3");
        }
        if (stringArray.length == 2 && commandSender.hasPermission(PERM_ADMIN)) {
            ArrayList<String> arrayList = new ArrayList<String>();
            Bukkit.getOnlinePlayers().forEach(player -> arrayList.add(player.getName()));
            return arrayList;
        }
        return Collections.emptyList();
    }
}

