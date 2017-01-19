package net.lapismc.afkplus.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.units.JustNow;
import org.ocpsoft.prettytime.units.Millisecond;

import java.util.Date;
import java.util.Locale;

public class AFKPlus implements CommandExecutor {

    private net.lapismc.afkplus.AFKPlus plugin;
    private PrettyTime pt;

    public AFKPlus(net.lapismc.afkplus.AFKPlus p) {
        plugin = p;
        pt = new PrettyTime(Locale.ENGLISH);
        pt.removeUnit(JustNow.class);
        pt.removeUnit(Millisecond.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("afkplus")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    sender.sendMessage(ChatColor.GOLD + "------------------"
                            + ChatColor.RED + "AFK+" + ChatColor.GOLD
                            + "------------------");
                    sender.sendMessage(ChatColor.RED + "Author:"
                            + ChatColor.GOLD + " Dart2112");
                    sender.sendMessage(ChatColor.RED + "Version: "
                            + ChatColor.GOLD
                            + plugin.getDescription().getVersion());
                    sender.sendMessage(ChatColor.GOLD
                            + "-----------------------------------------");
                } else {
                    sender.sendMessage("------------------AFK+------------------");
                    sender.sendMessage("Author: Dart2112");
                    sender.sendMessage("Version: "
                            + plugin.getDescription().getVersion());
                    sender.sendMessage("-----------------------------------------");
                }
            } else if (args.length == 1) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
                if (op.isOnline()) {
                    if (plugin.playersAFK.containsKey(op.getUniqueId())) {
                        Long time = plugin.playersAFK.get(op.getUniqueId());
                        String formattedTime;
                        if (plugin.commandAFK.get(op.getUniqueId())) {
                            formattedTime = pt.format(new Date(time));
                        } else {
                            formattedTime = pt.format(new Date(time -
                                    (plugin.getConfig().getInt("TimeUntilAFK") * 1000)));
                        }
                        if (sender instanceof Player) {
                            sender.sendMessage(plugin.AFKConfig.getColoredMessage("AFKPlusAFK") + formattedTime);
                        } else {
                            sender.sendMessage(plugin.AFKConfig.getMessage("AFKPlusAFK") + formattedTime);
                        }
                    } else {
                        if (sender instanceof Player) {
                            sender.sendMessage(plugin.AFKConfig.getColoredMessage("AFKPlusNotAFK"));
                        } else {
                            sender.sendMessage(plugin.AFKConfig.getMessage("AFKPlusNotAFK"));
                        }
                    }
                } else {
                    if (sender instanceof Player) {
                        sender.sendMessage(plugin.AFKConfig.getColoredMessage("AFKNotOnline"));
                    } else {
                        sender.sendMessage(plugin.AFKConfig.getMessage("AFKNotOnline"));
                    }
                }
            }
        }
        return false;
    }

}
