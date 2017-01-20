/*
 * Copyright 2017 Benjamin Martin
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
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (!p.hasPermission("afkplus.admin")) {
                    p.sendMessage(plugin.AFKConfig.getColoredMessage("NoPerms"));
                    return true;
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
