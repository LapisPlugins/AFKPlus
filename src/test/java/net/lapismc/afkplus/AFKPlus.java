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

package net.lapismc.afkplus;

import net.lapismc.afkplus.commands.AFKPlusAFK;
import net.lapismc.afkplus.util.LapisUpdater;
import net.lapismc.afkplus.util.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public final class AFKPlus extends JavaPlugin {

    public AFKPlusConfiguration AFKConfig;
    public AFKPlusPerms AFKPerms;
    public LapisUpdater updater;
    Logger logger = getLogger();
    private HashMap<UUID, AFKPlayer> players = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        update();
        new net.lapismc.afkplus.commands.AFKPlus(this);
        new AFKPlusAFK(this);
        new Metrics(this);
        new AFKPlusListeners(this);
        AFKConfig = new AFKPlusConfiguration(this);
        AFKPerms = new AFKPlusPerms(this);
        Thread watcher = new Thread(new AFKPlusFileWatcher(this));
        watcher.start();
        startTimer();
        logger.info(getName() + " v." + getDescription().getVersion() + " has been enabled!");
    }

    private void update() {
        updater = new LapisUpdater(this, "AFKPlus", "Dart2112", "AFKPlus", "master");
        if (updater.checkUpdate()) {
            if (getConfig().getBoolean("UpdateDownload")) {
                updater.downloadUpdate();
            } else {
                logger.info("A new update is available for AFKPlus");
            }
        } else {
            logger.info("No update available for AFKPlus");
        }
    }

    private Runnable runnable() {
        return () -> {
            Date date = new Date();
            for (AFKPlayer player : players.values()) {
                if (!player.isAFK()) {
                    Long time = player.getLastInteract();
                    Long difference = (date.getTime() - time) / 1000;
                    if (difference.intValue() >= AFKPerms.getPermissionValue(player.getUuid(), AFKPlusPerms.Perm.TimeToAFK)) {
                        player.startAFK(false);
                    }
                }
            }
            for (AFKPlayer player : players.values()) {
                if (!AFKPerms.isPermitted(player.getUuid(), AFKPlusPerms.Perm.Admin) && player.isAFK()) {
                    Long time = player.getAFKTime();
                    Long difference = (date.getTime() - time) / 1000;
                    if (difference.intValue() >= AFKPerms.getPermissionValue(player.getUuid(), AFKPlusPerms.Perm.TimeToWarn)) {
                        player.warnPlayer();
                    }
                    if (difference.intValue() >= AFKPerms.getPermissionValue(player.getUuid(), AFKPlusPerms.Perm.TimeToAction)) {
                        Player p = Bukkit.getPlayer(player.getUuid());
                        player.setAFK(false);
                        switch (getConfig().getString("Action")) {
                            case "COMMAND":
                                if (getConfig().getString("ActionVariable").contains(":")) {
                                    for (String s : getConfig().getString("ActionVariable").split(":")) {
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                                s.replace("%NAME%", p.getName()));
                                    }
                                } else {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                            getConfig().getString("ActionVariable").replace("%NAME%", p.getName()));
                                }
                                break;
                            case "MESSAGE":
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('&',
                                        getConfig().getString("ActionVariable").replace("%NAME%", p.getName()))));
                                break;
                            case "KICK":
                                p.kickPlayer(ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('&',
                                        getConfig().getString("ActionVariable").replace("%NAME%", p.getName()))));
                                break;
                            default:
                                logger.severe("The AFK+ action is not correctly set in the config!");
                        }
                    }
                }
            }
        };
    }

    private void startTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, runnable(), 20, 20);
    }

    public AFKPlayer getPlayer(Player p) {
        if (!players.containsKey(p.getUniqueId())) {
            players.put(p.getUniqueId(), new AFKPlayer(this, p));
        }
        return players.get(p.getUniqueId());
    }

    public AFKPlayer getPlayer(OfflinePlayer op) {
        if (!players.containsKey(op.getUniqueId())) {
            players.put(op.getUniqueId(), new AFKPlayer(this, op));
        }
        return players.get(op.getUniqueId());
    }

    void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

}
