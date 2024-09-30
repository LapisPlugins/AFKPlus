/*
 * Copyright 2024 Benjamin Martin
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

import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.commands.tabcomplete.AFKPlusPlayerTabOption;
import net.lapismc.afkplus.commands.tabcomplete.OtherAFKPlusOptions;
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import net.lapismc.afkplus.playerdata.Permission;
import net.lapismc.afkplus.util.AFKPlusCommand;
import net.lapismc.lapiscore.commands.tabcomplete.LapisCoreTabCompleter;
import net.lapismc.lapiscore.commands.tabcomplete.LapisTabOption;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class AFKPlusCmd extends AFKPlusCommand {

    public AFKPlusCmd(AFKPlus plugin) {
        super(plugin, "afkplus", "Shows plugin, player and help information", new ArrayList<>());
        LapisCoreTabCompleter tabCompleter = new LapisCoreTabCompleter();
        ArrayList<LapisTabOption> topLevelOptions = new ArrayList<>();
        topLevelOptions.add(new AFKPlusPlayerTabOption());
        topLevelOptions.add(new OtherAFKPlusOptions());
        tabCompleter.registerTopLevelOptions(this, topLevelOptions);
        registerTabCompleter(tabCompleter);
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        //afkplus
        if (args.length == 0) {
            String primary = plugin.primaryColor;
            String secondary = plugin.secondaryColor;
            String bars = secondary + "-------------";
            sender.sendMessage(bars + primary + "   AFKPlus   " + bars);
            sender.sendMessage(primary + "Version: " + secondary + plugin.getDescription().getVersion());
            sender.sendMessage(primary + "Author: " + secondary + plugin.getDescription().getAuthors().get(0));
            sender.sendMessage(primary + "Spigot: " + secondary + "https://goo.gl/yeMGBL");
            sender.sendMessage(primary + "If you need help use " + secondary + "/afkplus help");
            sender.sendMessage(bars + bars + bars);
        } else if (args.length == 1) {
            // /afkplus update
            if (args[0].equalsIgnoreCase("update")) {
                if (isNotPermitted(sender, Permission.CanUpdate)) {
                    sendMessage(sender, "Error.NotPermitted");
                    return;
                }
                if (plugin.updater.checkUpdate()) {
                    //update is available, we need to download it
                    sendMessage(sender, "Updater.UpdateDownloading");
                    plugin.updater.downloadUpdate();
                } else {
                    //no update available
                    sendMessage(sender, "Updater.NoUpdate");
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (isNotPermitted(sender, Permission.CanReload)) {
                    sendMessage(sender, "Error.NotPermitted");
                    return;
                }
                // Reload the configs
                plugin.reloadConfig();
                plugin.perms.loadPermissions();
                plugin.config.reloadMessages();
                // Send the user of the command a message to let them know that the command worked
                sendMessage(sender, "Reload");
                //If the sender is a player, notify the console that AFKPlus has been reloaded
                if (sender instanceof Player)
                    plugin.getLogger().info(sender.getName() + " just reloaded AFKPlus configs!");
            } else if (!args[0].equalsIgnoreCase("player")) {
                // /afkplus help
                sendHelp(sender);
            }
            // /afkplus player
        } else {
            if (args[0].equalsIgnoreCase("player")) {
                playerCommand(sender, args);
            } else {
                sendHelp(sender);
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        YamlConfiguration messages = plugin.config.getMessages();
        sender.sendMessage(plugin.config.getMessage("Help.Help"));
        for (String key : messages.getConfigurationSection("Help").getKeys(false)) {
            if (!key.equalsIgnoreCase("help")) {
                sendMessage(sender, "Help." + key);
            }
        }
    }

    private void playerCommand(CommandSender sender, String[] args) {
        // /afkplus player name
        if (args.length == 2) {
            String name = args[1];
            //noinspection deprecation
            OfflinePlayer op = Bukkit.getOfflinePlayer(name);
            if (!op.hasPlayedBefore()) {
                sendMessage(sender, "Error.PlayerNotFound");
                return;
            }
            AFKPlusPlayer player = getPlayer(op);
            if (player.isAFK()) {
                Long afkStart = player.getAFKStart();
                String message = plugin.config.getMessage("Player.AFK").replace("{PLAYER}", player.getName())
                        .replace("{TIME}", getTimeDifference(afkStart));
                sender.sendMessage(message);
            } else {
                String message = plugin.config.getMessage("Player.NotAFK").replace("{PLAYER}", player.getName());
                sender.sendMessage(message);
            }
        } else {
            sendMessage(sender, "Help.AFKPlusPlayer");
        }
    }
}
