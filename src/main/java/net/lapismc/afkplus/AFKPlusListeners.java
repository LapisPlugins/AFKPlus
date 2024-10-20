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

package net.lapismc.afkplus;

import net.lapismc.afkplus.api.AFKMachineDetectEvent;
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import net.lapismc.afkplus.util.EntitySpawnManager;
import net.lapismc.afkplus.util.PlayerMovementMonitoring;
import net.lapismc.afkplus.util.PlayerMovementStorage;
import net.lapismc.lapiscore.commands.CommandRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class AFKPlusListeners implements Listener {

    private final AFKPlus plugin;
    private final HashMap<UUID, Location> playerLocations = new HashMap<>();
    private BukkitTask AfkMachineDetectionTask;
    private final EntitySpawnManager spawnManager;
    private final PlayerMovementMonitoring monitoring;

    AFKPlusListeners(AFKPlus plugin) {
        this.plugin = plugin;
        monitoring = new PlayerMovementMonitoring();
        startAFKMachineDetection();
        spawnManager = new EntitySpawnManager(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /*
    AFK management and interact events
     */

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        plugin.getPlayer(e.getPlayer()).forceStopAFK();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        plugin.getPlayer(e.getPlayer()).stopAFK(true);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.Chat")) {
            Bukkit.getScheduler().runTask(plugin, () -> plugin.getPlayer(e.getPlayer()).interact());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        PlayerMovementStorage movement = new PlayerMovementStorage(e);

        if (plugin.getConfig().getBoolean("MovementMagnitude.Enabled")) {
            //This will update the didLook and didMove values based on the config settings
            double posTrigger = plugin.getConfig().getDouble("MovementMagnitude.PositionTrigger");
            float lookTrigger = (float) plugin.getConfig().getDouble("MovementMagnitude.LookTrigger");
            monitoring.logAndCheckMovement(e.getPlayer().getUniqueId(), movement, posTrigger, lookTrigger);
        }

        //Only interact if the player performed the action and the detection is enabled for it
        if ((movement.didLook && plugin.getConfig().getBoolean("EnabledDetections.Look")) ||
                (movement.didMove && plugin.getConfig().getBoolean("EnabledDetections.Move"))) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        //Run the attack detection if the attacker is a player
        if (plugin.getConfig().getBoolean("EnabledDetections.Attack") && e.getDamager() instanceof Player p) {
            plugin.getPlayer(p).interact();
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.Command")) {
            //Check if the player is running an alias of /AFK
            if (e.getMessage().contains("afk"))
                return;
            for (String command : CommandRegistry.getCommand("afk").getTakenAliases()) {
                if (e.getMessage().contains(command)) {
                    return;
                }
            }
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.Interact")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    @EventHandler
    public void onPlayerInventoryInteract(InventoryInteractEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        if (plugin.getConfig().getBoolean("EnabledDetections.GUI")) {
            plugin.getPlayer(e.getWhoClicked().getUniqueId()).interact();
        }
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.BlockPlace")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.BlockBreak")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    /*
    AFK protection events
     */

    @EventHandler
    public void onPlayerMoveProtect(PlayerMoveEvent e) {
        //Make sure the player is AFK before doing any work
        if (!plugin.getPlayer(e.getPlayer()).isAFK())
            return;
        PlayerMovementStorage movement = new PlayerMovementStorage(e);

        //Check if bump protection is enabled and the player has only moved but not looked
        boolean isBumpProtected = isMovementCausedByEntityBump(e.getPlayer());
        if (isBumpProtected && (movement.didMove && !movement.didLook)) {
            //Make sure they haven't moved up in the Y direction (this allows jumping but not falling)
            if (movement.to.getY() <= movement.from.getY()) {
                //The players has only moved in the X or Z directions so we cancel the event since it could be a bump
                e.setCancelled(true);
            }
        }
    }

    private boolean isMovementCausedByEntityBump(Player p) {
        //How close does the mob need to be to the player before they are considered to be "bumping" the player
        double requiredDistance = 0.5;

        //If both bump and hurt by mob protections are disabled then we can just ignore this
        if (!(plugin.getConfig().getBoolean("Protections.Bump") || plugin.getConfig().getBoolean("Protections.HurtByMob")))
            return false;

        //Check if the player has been attacked by a mob in bump range
        boolean playerAttacked = false;
        EntityDamageEvent event = p.getLastDamageCause();
        Entity damager = null;
        if (event instanceof EntityDamageByEntityEvent) {
            damager = ((EntityDamageByEntityEvent) event).getDamager();
        }

        boolean isEntityClose = false;
        //Find all entities within the required range
        for (Entity e : p.getNearbyEntities(requiredDistance, requiredDistance, requiredDistance)) {
            if (e instanceof Monster || e instanceof Player) {
                if (e.equals(damager)) {
                    playerAttacked = true;
                }
                isEntityClose = true;
                break;
            }
        }
        //If bump is enabled and the player was bumped by an entity then we report as a bump
        if (plugin.getConfig().getBoolean("Protections.Bump") && isEntityClose)
            return true;
        //Report as bump is mob protection is on and they were attacked or a mob is close
        return plugin.getConfig().getBoolean("Protections.HurtByMob") && (playerAttacked || isEntityClose);
    }

    @EventHandler
    public void onEntityDamageProtection(EntityDamageByEntityEvent e) {
        //Check if the damager is a player or an arrow shot by a player
        boolean damageCausedByPlayer = e.getDamager() instanceof Player;
        if (e.getDamager() instanceof Arrow)
            damageCausedByPlayer = ((Arrow) e.getDamager()).getShooter() instanceof Player;

        //Check if the attacked is a player and if we should be protecting them
        if (e.getEntity() instanceof Player && plugin.getPlayer((Player) e.getEntity()).isAFK()) {
            if (plugin.getConfig().getBoolean("Protections.HurtByPlayer") && damageCausedByPlayer) {
                e.setCancelled(true);
            }
            if (plugin.getConfig().getBoolean("Protections.HurtByMob") && !damageCausedByPlayer) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerHurt(EntityDamageEvent e) {
        if ((e instanceof EntityDamageByEntityEvent) || !(e.getEntity() instanceof Player))
            return;
        //We should have a player that is being damaged by something other than an entity
        AFKPlusPlayer p = plugin.getPlayer(e.getEntity().getUniqueId());
        if (plugin.getConfig().getBoolean("Protections.HurtByOther") && p.isAFK())
            e.setCancelled(true);
    }

    /*
    Mob Spawning Protections
     */

    @EventHandler
    public void onNaturalMonsterSpawn(CreatureSpawnEvent e) {
        if (!plugin.getConfig().getBoolean("Protections.MobSpawning"))
            return;
        //Return if it's not a monster or if it isn't natural
        if (!(e.getEntity() instanceof Monster) || !e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL))
            return;
        boolean shouldSpawn = spawnManager.shouldNaturalSpawn(e.getLocation());
        if (!shouldSpawn)
            e.setCancelled(true);
    }

    @EventHandler
    public void onSpawnerMonsterSpawn(SpawnerSpawnEvent e) {
        if (!plugin.getConfig().getBoolean("Protections.MobSpawning"))
            return;
        //Return if it's not a monster
        if (!(e.getEntity() instanceof Monster))
            return;
        boolean shouldSpawn = spawnManager.shouldSpawnerSpawn(e.getSpawner());
        if (!shouldSpawn)
            e.setCancelled(true);
    }

    /*
    Mob targeting protection
     */
    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent e) {
        if (!plugin.getConfig().getBoolean("Protections.MobTargeting"))
            return;
        if (!(e.getEntity() instanceof Monster))
            return;
        if (!(e.getTarget() instanceof Player player))
            return;
        if (player == null) return;
        if (!plugin.getPlayer(player).isAFK())
            return;
        e.setCancelled(true);
    }

    /*
    AFK Machine detection
     */

    /**
     * This task should be canceled on disable, this is the task that attempts to stop AFK machines from working
     *
     * @return The AFK machine detection task being run by Bukkit
     */
    public BukkitTask getAfkMachineDetectionTask() {
        return AfkMachineDetectionTask;
    }

    private void startAFKMachineDetection() {
        AfkMachineDetectionTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            playerLocations.clear();
            //Save all players current locations
            for (Player p : Bukkit.getOnlinePlayers()) {
                playerLocations.put(p.getUniqueId(), p.getLocation());
            }
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                //Go through each saved location and see if the player is moving
                for (UUID uuid : playerLocations.keySet()) {
                    if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                        Location savedLoc = playerLocations.get(uuid);
                        Location loc = Bukkit.getPlayer(uuid).getLocation();
                        //Check if the player is moving in both rotation and transform
                        boolean inactive = false;
                        if (plugin.getConfig().getBoolean("AggressiveAFKDetection")) {
                            //If aggressive is enabled we want to check if the player isn't moving in both look and transform
                            boolean isNotLooking = checkRotation(savedLoc, loc);
                            boolean isNotMoving = checkTransform(savedLoc, loc);
                            if (isNotLooking && isNotMoving)
                                inactive = true;
                        }
                        //This is sent to the player object, if the player is deemed to not be moving they will not
                        //be able to reset their interact timer. This wil force them into AFK even
                        //if they are triggering detection events
                        if (inactive) {
                            //Trigger the AFKMachine event if the player is deemed to be avoiding AFK
                            Bukkit.getScheduler().runTask(plugin, () ->
                                    Bukkit.getPluginManager().callEvent(new AFKMachineDetectEvent(plugin.getPlayer(uuid))));
                        }
                        plugin.getPlayer(uuid).setInactive(inactive);

                    }
                }
            }, 20 * 2);
        }, 20 * 5, 20 * 5);
    }

    private boolean checkRotation(Location oldLoc, Location newLoc) {
        boolean yaw = oldLoc.getYaw() == newLoc.getYaw();
        boolean pitch = oldLoc.getPitch() == newLoc.getPitch();
        return yaw && pitch;
    }


    private boolean checkTransform(Location oldLoc, Location newLoc) {
        boolean x = oldLoc.getX() == newLoc.getX();
        boolean y = oldLoc.getY() == newLoc.getY();
        boolean z = oldLoc.getZ() == newLoc.getZ();
        return x && y && z;
    }
}
