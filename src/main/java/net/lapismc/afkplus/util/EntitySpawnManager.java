/*
 * Copyright 2022 Benjamin Martin
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
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

/**
 * Class used to detect if entity spawning should occur near players who are AFK
 * Uses code from the following classes
 * <p>
 * <a href="https://github.com/kickash32/DistributedMobSpawns/blob/version-2/src/main/java/me/kickash32/distributedmobspawns/EventListener.java#L22">...</a>
 * <a href="https://github.com/kickash32/DistributedMobSpawns/blob/version-2/src/main/java/me/kickash32/distributedmobspawns/EntityProcessor.java#L51">...</a>
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

    public boolean shouldNaturalSpawn(Location loc) {
        //for all chunks in spawnRange-1 chunk radius of location
        //Get all player entities and add them to nearbyPlayers
        //This code is derived from information provided in this spigot post
        //https://www.spigotmc.org/threads/what-exactly-does-mob-spawn-range-do.176889/#post-3221175
        for (int i = -(spawnRange - 1); i < (spawnRange - 1); i++) {
            for (int j = -(spawnRange - 1); j < (spawnRange - 1); j++) {
                Chunk chunk = loc.getWorld().getChunkAt(loc.getChunk().getX() + i, loc.getChunk().getZ() + j);
                for (Entity e : chunk.getEntities()) {
                    if (!(e instanceof Player))
                        continue;
                    Player p = (Player) e;
                    //Ignore players who are in spectator mode as they cannot spawn mobs
                    if (p.getGameMode() == GameMode.SPECTATOR)
                        continue;
                    //We have found a player in the spawn radius, If they aren't AFK then the mob spawn is valid
                    if (!plugin.getPlayer(p).isAFK())
                        return true;
                }
            }
        }
        //If the code reaches this point, then no player has been found in the spawn radius who is not AFK
        //Hence the spawn should be cancelled
        return false;
    }

    public boolean shouldSpawnerSpawn(CreatureSpawner spawner) {
        double range = 16;
        List<Player> players = spawner.getLocation().getWorld().getPlayers();
        //Remove players who are in spectator mode
        players.removeIf(p -> p.getGameMode().equals(GameMode.SPECTATOR));
        //Remove players who are AFK
        players.removeIf(p -> plugin.getPlayer(p).isAFK());

        //Spawner mechanics information from the following page
        //https://minecraft.fandom.com/wiki/Monster_Spawner#Mechanics

        //Get the exact location at the center of the spawner block
        Location centerOfSpawner = spawner.getLocation().add(0.5, -0.5, 0.5);
        //Remove players who are outside the range of the spawner
        players.removeIf(p -> p.getLocation().distance(centerOfSpawner) > range);

        //All players who cannot or should not trigger this spawner have been removed
        //Therefore it should only spawn if the list is still populated with players
        return players.size() > 0;
    }

}
