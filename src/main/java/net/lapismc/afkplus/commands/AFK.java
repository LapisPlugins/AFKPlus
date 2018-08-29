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

import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.AFKPlusPlayer;
import net.lapismc.afkplus.playerdata.Permission;
import net.lapismc.afkplus.util.AFKPlusCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class AFK extends AFKPlusCommand {

    public AFK(AFKPlus plugin) {
        super(plugin, "afk", "Toggle AFK status", new ArrayList<>());
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            // /afk
            //Check that the sender is a player
            if (isNotPlayer(sender, "Error.MustBePlayer")) {
                return;
            }
            AFKPlusPlayer player = getPlayer((Player) sender);
            //Check that they are permitted to use the command
            if (!player.isPermitted(Permission.AFKSelf)) {
                sendMessage(sender, "Error.NotPermitted");
                return;
            }
            //Toggle their AFK status
            toggleAFK(player);
        } else if (args.length == 1) {
            // /afk [PlayerName]
            //Check that they are permitted to use this command
            if (!isPermitted(sender, Permission.AFKOthers)) {
                sendMessage(sender, "Error.NotPermitted");
                return;
            }
            //Check that the player is online
            OfflinePlayer op = getOfflinePlayer(args[0]);
            if (!op.isOnline()) {
                sendMessage(sender, "Error.PlayerNotFound");
                return;
            }
            //Get the player and toggle their AFK status
            toggleAFK(getPlayer(op));
        } else {
            sendMessage(sender, "Help.AFK");
        }
    }

    private void toggleAFK(AFKPlusPlayer player) {
        //If the player is AFK stop it, If they aren't then start it
        if (player.isAFK()) {
            player.stopAFK();
            broadcast(false, player.getName());
        } else {
            player.startAFK();
            broadcast(true, player.getName());
        }
    }

    private OfflinePlayer getOfflinePlayer(String name) {
        //noinspection deprecation
        return Bukkit.getOfflinePlayer(name);
    }

    private void broadcast(boolean start, String name) {
        //Get the appropriate broadcast message and insert the players name
        String message = plugin.config.getMessage(start ? "Broadcast.Start" : "Broadcast.Stop")
                .replace("%PLAYER%", name);
        Bukkit.broadcastMessage(message);
    }
}
