/*
 * Copyright 2019 Benjamin Martin
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

import net.lapismc.afkplus.api.AFKPlusAPI;
import net.lapismc.afkplus.api.AFKPlusPlayerAPI;
import net.lapismc.afkplus.commands.AFK;
import net.lapismc.afkplus.commands.AFKPlusCmd;
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import net.lapismc.lapiscore.LapisCoreConfiguration;
import net.lapismc.lapiscore.LapisCoreFileWatcher;
import net.lapismc.lapiscore.LapisCorePlugin;
import net.lapismc.lapiscore.LapisUpdater;
import net.lapismc.lapiscore.utils.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public final class AFKPlus extends LapisCorePlugin {

    public LapisUpdater updater;
    private LapisCoreFileWatcher fileWatcher;
    private BukkitTask repeatingTask;
    private HashMap<UUID, AFKPlusPlayer> players = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerConfiguration(new LapisCoreConfiguration(this, 2, 2));
        registerPermissions(new AFKPlusPermissions(this));
        update();
        fileWatcher = new LapisCoreFileWatcher(this);
        new AFK(this);
        new AFKPlusCmd(this);
        new AFKPlusListeners(this);
        new AFKPlusAPI(this);
        new AFKPlusPlayerAPI(this);
        new Metrics(this);
        repeatingTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, runRepeatingTasks(), 20, 20);
        getLogger().info(getName() + " v." + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        fileWatcher.stop();
        repeatingTask.cancel();
        getLogger().info(getName() + " has been disabled!");
    }

    public AFKPlusPlayer getPlayer(UUID uuid) {
        if (!players.containsKey(uuid)) {
            players.put(uuid, new AFKPlusPlayer(this, uuid));
        }
        return players.get(uuid);
    }

    public AFKPlusPlayer getPlayer(OfflinePlayer op) {
        return getPlayer(op.getUniqueId());
    }

    private void update() {
        updater = new LapisUpdater(this, "AFKPlus", "Dart2112", "AFKPlus", "master");
        if (updater.checkUpdate()) {
            if (getConfig().getBoolean("UpdateDownload")) {
                updater.downloadUpdate();
            } else {
                getLogger().info(config.getMessage("Updater.UpdateFound"));
            }
        } else {
            getLogger().info(config.getMessage("Updater.NoUpdate"));
        }
    }

    private Runnable runRepeatingTasks() {
        return () -> {
            for (AFKPlusPlayer player : players.values()) {
                player.getRepeatingTask().run();
            }
        };
    }

}
