/*
 * Copyright 2021 Benjamin Martin
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

package net.lapismc.afkplus.util;

import net.lapismc.afkplus.AFKPlus;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class used to detect if entity spawning should occur near players who are AFK
 * Uses code from the following classes
 * <p>
 * https://github.com/kickash32/DistributedMobSpawns/blob/version-2/src/main/java/me/kickash32/distributedmobspawns/EventListener.java#L22
 * https://github.com/kickash32/DistributedMobSpawns/blob/version-2/src/main/java/me/kickash32/distributedmobspawns/EntityProcessor.java#L51
 */
public class EntitySpawnManager {

    private final AFKPlus plugin;
    private int spawnRange;

    public EntitySpawnManager(AFKPlus plugin) {
        this.plugin = plugin;
        try {
            //Get the spigot.yml file and attempt to load the mob spawn range value from it
            YamlConfiguration spigotConfig = new YamlConfiguration();
            spigotConfig.load("spigot.yml");
            spawnRange = spigotConfig.getConfigurationSection("world-settings").getConfigurationSection("default")
                    .getInt("mob-spawn-range", 8);
        } catch (IOException | InvalidConfigurationException e) {
            //This probably means spigot isn't installed, just use the default
            spawnRange = 8;
        }
    }

    public boolean shouldSpawn(Location loc, CreatureSpawnEvent.SpawnReason reason) {
        Collection<Player> nearbyPlayers;
        if (reason.equals(CreatureSpawnEvent.SpawnReason.NATURAL))
            nearbyPlayers = getPlayersInSquareRange(loc, spawnRange * 16, false);
        else if (reason.equals(CreatureSpawnEvent.SpawnReason.SPAWNER))
            nearbyPlayers = getPlayersInSquareRange(loc, 16, true);
        else
            return true;
        for (Player p : nearbyPlayers) {
            if (!plugin.getPlayer(p).isAFK())
                return true;
        }
        return nearbyPlayers.size() == 0;
    }

    public List<Player> getPlayersInSquareRange(Location location, int range, boolean checkY) {

        List<Player> players = location.getWorld().getPlayers();
        List<Player> filteredPlayers = new ArrayList<>();

        for (Player player : players) {
            Location playerLocation = player.getLocation();

            if (player.getGameMode() != GameMode.SPECTATOR &&
                    Math.abs(playerLocation.getBlockX() - location.getBlockX()) < range &&
                    Math.abs(playerLocation.getBlockZ() - location.getBlockZ()) < range) {
                if (!checkY || Math.abs(playerLocation.getBlockY() - location.getBlockY()) < range) {
                    filteredPlayers.add(player);
                }
            }
        }
        return filteredPlayers;
    }

    enum SpawnType {

    }

}
