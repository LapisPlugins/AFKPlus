/*
 * Copyright 2023 Benjamin Martin
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
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
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
        List<Player> playersInRange = new ArrayList<>();
        //for all chunks in spawnRange-1 chunk radius of location
        //Get all player entities and add them to nearbyPlayers
        //This code is derived from information provided in this spigot post
        //https://www.spigotmc.org/threads/what-exactly-does-mob-spawn-range-do.176889/#post-3221175
        /*for (int i = -(spawnRange); i < (spawnRange); i++) {
            for (int j = -(spawnRange); j < (spawnRange); j++) {

                Chunk chunk = loc.getWorld().getChunkAt(loc.getChunk().getX() + i, loc.getChunk().getZ() + j);
                for (Entity e : chunk.getEntities()) {
                    if (!(e instanceof Player))
                        continue;
                    Player p = (Player) e;
                    //Ignore players who are in spectator mode as they cannot spawn mobs
                    if (p.getGameMode() == GameMode.SPECTATOR)
                        continue;
                    //We have found a player in the spawn radius, adding them to the list
                    playersInRange.add(p);
                }
            }
        }*/
        //Theoretically faster way of checking players
        //TODO: Test this and remove if not worth it
        int chunkX = loc.getChunk().getX();
        int maxX = chunkX + spawnRange;
        int minX = chunkX - spawnRange;
        int chunkZ = loc.getChunk().getZ();
        int maxZ = chunkZ + spawnRange;
        int minZ = chunkZ - spawnRange;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR)
                continue;
            Chunk c = p.getLocation().getChunk();
            int playerChunkX = p.getLocation().getChunk().getX();
            if (playerChunkX < maxX && playerChunkX > minX) {
                int playerChunkZ = p.getLocation().getChunk().getZ();
                if (playerChunkZ < maxZ && playerChunkZ > minZ) {
                    playersInRange.add(p);
                }
            }

        }

        //Check if all players found are AFK
        if (playersInRange.size() == 0) {
            //No players in range so this is just a distant natural spawn
            return true;
        }
        for (Player p : playersInRange) {
            if (!plugin.getPlayer(p).isAFK()) {
                //A player in the spawn range is not AFK, therefore the spawn is valid
                return true;
            }
        }
        //If we reach this point, there are players in range, but they are all AFK. Disallow spawn
        return false;
    }

    public boolean shouldSpawnerSpawn(CreatureSpawner spawner) {
        double range = 16;
        List<Player> players = spawner.getLocation().getWorld().getPlayers();
        //Remove players who are in spectator mode
        players.removeIf(p -> p.getGameMode().equals(GameMode.SPECTATOR));

        //Spawner mechanics information from the following page
        //https://minecraft.fandom.com/wiki/Monster_Spawner#Mechanics

        //Get the exact location at the center of the spawner block
        Location centerOfSpawner = spawner.getLocation().add(0.5, -0.5, 0.5);
        boolean playerInRange = false;
        boolean playerAFK = false;

        for (Player p : players) {
            if (p.getLocation().distance(centerOfSpawner) > range)
                continue;
            playerInRange = true;
            if (plugin.getPlayer(p).isAFK()) {
                playerAFK = true;
            } else {
                //We found a player in range who is not AFK
                //This needs to trigger a spawn, as such we set AFK to false and break
                playerAFK = false;
                break;
            }
        }
        if (playerInRange && !playerAFK)
            return true;
        return !playerInRange;
    }

}
