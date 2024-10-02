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

package net.lapismc.afkplus.api;

import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * A class for managing statistics related to individual players
 * For now this only tracks the total accumulated time that a player has been afk
 */
public class AFKStatisticManager {

    private final AFKPlus plugin;
    private final File statisticsFile;
    private final YamlConfiguration statistics;
    private boolean enabled;

    /**
     * Initialize the manager, you will need an instance of the AFKPlus plugin
     *
     * @param plugin An instance of the AFKPlus plugin
     */
    public AFKStatisticManager(AFKPlus plugin) {
        this.plugin = plugin;
        //Load the statistics enabled status from the config, default to true
        //We won't make the file to attempt to load data into it if this is false
        this.enabled = plugin.getConfig().getBoolean("EnableStatistics", true);
        statisticsFile = new File(plugin.getDataFolder(), "statistics.yml");
        //Make the file if statistics are enabled and the file doesn't exist
        if (!statisticsFile.exists() && enabled) {
            try {
                statisticsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create statistics file!");
                enabled = false;
                statistics = null;
                return;
            }
        }
        //Load the yaml
        statistics = YamlConfiguration.loadConfiguration(statisticsFile);
    }

    /**
     * Get the status of the statistics subsystem
     *
     * @return true if statistics are enabled, otherwise false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the total accumulated time that this player has been AFK
     *
     * @param player The player you wish to get stats for
     * @return The time in ms that the player has been AFK, 0L if no record, -1L if stats are disabled
     */
    public Long getTotalTimeAFK(AFKPlusPlayer player) {
        if (!enabled) return -1L;
        return statistics.getLong(player.getName() + ".TimeSpentAFK", 0L);
    }

    /**
     * Increase the total time that this player has been afk by toAdd
     *
     * @param player The player to increment
     * @param toAdd  The time in ms to add to their total time
     */
    public void incrementTotalTimeAFK(AFKPlusPlayer player, Long toAdd) {
        if (!enabled) return;
        long currentTotalTime = getTotalTimeAFK(player);
        setTotalTimeAFK(player, currentTotalTime + toAdd);
    }

    /**
     * Set the total time AFK for this player to an absolute value
     *
     * @param player  The player you wish to set for
     * @param timeAFK The number of ms to set this value too, null to delete the entry
     */
    public void setTotalTimeAFK(AFKPlusPlayer player, Long timeAFK) {
        if (!enabled) return;
        statistics.set(player.getName() + ".TimeSpentAFK", timeAFK);
        try {
            statistics.save(statisticsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write to statistics.yml");
        }
    }

}
