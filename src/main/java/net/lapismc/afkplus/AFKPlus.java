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

import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import net.lapismc.afkplus.util.LapisUpdater;
import net.lapismc.afkplus.util.Metrics;
import net.lapismc.lapiscore.LapisCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public final class AFKPlus extends LapisCorePlugin {

    public LapisUpdater updater;
    public AFKPlusPermissions permissions;
    Logger logger = getLogger();
    private HashMap<UUID, AFKPlusPlayer> players = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        update();
        registerConfiguration(new AFKPlusConfiguration(this));
        registerPermissions(new AFKPlusPermissions(this));
        new AFKPlusCommands(this);
        new Metrics(this);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, getRepeatingTask(), 20, 20);
        logger.info(getName() + " v." + getDescription().getVersion() + " has been enabled!");
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
                logger.info("A new update is available for AFKPlus");
            }
        } else {
            logger.info("No update available for AFKPlus");
        }
    }

    private Runnable getRepeatingTask() {
        return () -> {
            for (AFKPlusPlayer player : players.values()) {
                player.getRepeatingTask().run();
            }
        };
    }

}
