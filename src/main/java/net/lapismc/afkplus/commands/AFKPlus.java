/*
 * Copyright 2018 Benjamin Martin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lapismc.afkplus.commands;

import net.lapismc.afkplus.AFKPlusPerms;
import net.lapismc.afkplus.util.LapisCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ocpsoft.prettytime.Duration;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.units.JustNow;
import org.ocpsoft.prettytime.units.Millisecond;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AFKPlus extends LapisCommand {

    private net.lapismc.afkplus.AFKPlus plugin;
    private PrettyTime pt;

    public AFKPlus(net.lapismc.afkplus.AFKPlus p) {
        super("afkplus", "Shows plugin info and how long a player has been AFK", new ArrayList<>());
        plugin = p;
        pt = new PrettyTime(Locale.ENGLISH);
        pt.removeUnit(JustNow.class);
        pt.removeUnit(Millisecond.class);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("afkplus")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (!plugin.AFKPerms.isPermitted(p.getUniqueId(), AFKPlusPerms.Perm.Admin)) {
                    p.sendMessage(plugin.AFKConfig.getColoredMessage("NoPerms"));
                    return;
                }
            }
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
                    sender.sendMessage(ChatColor.RED + "Commands:");
                    sender.sendMessage(ChatColor.RED + "/afk (player): " + ChatColor.GOLD + "Toggles AFK");
                    sender.sendMessage(ChatColor.RED + "/afkplus (player): " + ChatColor.GOLD +
                            "See how long a player ask been AFK for");
                    sender.sendMessage(ChatColor.GOLD
                            + "-----------------------------------------");
                } else {
                    sender.sendMessage("------------------AFK+------------------");
                    sender.sendMessage("Author: Dart2112");
                    sender.sendMessage("Version: "
                            + plugin.getDescription().getVersion());
                    sender.sendMessage("Commands:");
                    sender.sendMessage("/afk (player): Toggles AFK");
                    sender.sendMessage("/afkplus (player): See how long a player ask been AFK for");
                    sender.sendMessage("-----------------------------------------");
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    plugin.reloadConfig();
                    if (sender instanceof Player) {
                        sender.sendMessage(ChatColor.GOLD + "AFK+ Reloaded");
                    } else {
                        sender.sendMessage("AFK+ Reloaded");
                    }
                } else if (args[0].equalsIgnoreCase("update")) {
                    if (sender instanceof Player) {
                        sender.sendMessage(ChatColor.GOLD + "Checking for updates...");
                    } else {
                        sender.sendMessage("Checking for updates...");
                    }
                    if (plugin.updater.checkUpdate()) {
                        if (sender instanceof Player) {
                            sender.sendMessage(ChatColor.GOLD + "Found an update \nDownloading it now \nIt will be installed on the next server restart");
                        } else {
                            sender.sendMessage("Found an update \nDownloading it now \nIt will be installed on the next server restart");
                        }
                        plugin.updater.downloadUpdate();
                    } else {
                        if (sender instanceof Player) {
                            sender.sendMessage(ChatColor.GOLD + "No update found");
                        } else {
                            sender.sendMessage("No update found");
                        }
                    }
                }
                //noinspection deprecation
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
                if (op.isOnline()) {
                    if (plugin.playersAFK.containsKey(op.getUniqueId())) {
                        Long time = plugin.playersAFK.get(op.getUniqueId());
                        String formattedTime;
                        if (plugin.commandAFK.get(op.getUniqueId())) {
                            List<Duration> durationList = pt.calculatePreciseDuration(new Date(time));
                            formattedTime = pt.format(durationList);
                        } else {
                            List<Duration> durationList = pt.calculatePreciseDuration(new Date(time -
                                    (plugin.getConfig().getInt("TimeUntilAFK") * 1000)));
                            formattedTime = pt.format(durationList);
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
    }

}
