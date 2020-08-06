/*
 * Copyright 2020 Benjamin Martin
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

package net.lapismc.afkplus.playerdata;

import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.api.AFKActionEvent;
import net.lapismc.afkplus.api.AFKStartEvent;
import net.lapismc.afkplus.api.AFKStopEvent;
import net.lapismc.lapiscore.compatibility.XSound;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.Date;
import java.util.UUID;

/**
 * This class can be used to start, stop and check AFK as well as the values used to start and stop AFK
 * Please read the documentation for each method before using it
 */
@SuppressWarnings("WeakerAccess")
public class AFKPlusPlayer {

    private final AFKPlus plugin;
    private final UUID uuid;
    private Long lastInteract;
    private Long afkStart;
    private boolean isAFK;
    private boolean isInactive;
    private boolean isWarned;

    public AFKPlusPlayer(AFKPlus plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
        isAFK = false;
        isInactive = false;
        isWarned = false;
        lastInteract = System.currentTimeMillis();
    }

    /**
     * Get the UUID of the player
     *
     * @return Returns the UUID of the player
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get the players username
     *
     * @return Returns the name of the player
     */
    public String getName() {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    /**
     * Setting the player as inactive will stop {@link #interact()} from doing anything
     * This is used to deal with AFK machines
     *
     * @param isInactive true to enable blocking of {@link #interact()}
     */
    public void setInactive(boolean isInactive) {
        this.isInactive = isInactive;
    }

    /**
     * Check if the player is permitted to do something
     * Permissions are stored in {@link Permission} as an Enumeration
     *
     * @param perm The permission you wish to check
     * @return Returns true if the player DOESN'T have the permission
     */
    public boolean isNotPermitted(Permission perm) {
        return !plugin.perms.isPermitted(uuid, perm.getPermission());
    }

    /**
     * Warn the player with a message and sound if enabled
     */
    public void warnPlayer() {
        isWarned = true;
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            return;
        }
        //Send the player the warning message
        p.sendMessage(plugin.config.getMessage("Warning"));
        //Check if warning sounds are enabled
        if (!"".equals(plugin.getConfig().getString("WarningSound"))) {
            //Get the sound from Compat Bridge
            Sound sound;
            try {
                sound = XSound.valueOf(plugin.getConfig().getString("WarningSound")).parseSound();
            } catch (IllegalArgumentException e) {
                sound = XSound.ENTITY_PLAYER_LEVELUP.parseSound();
            }
            //Play the sound at the players current location
            p.playSound(p.getLocation(), sound, 1, 1);
        }
    }

    /**
     * Check if the player is AFK
     *
     * @return returns true if the player is currently AFK
     */
    public boolean isAFK() {
        return isAFK;
    }

    /**
     * Get the system time when the player became AFK
     * Could be null if the player is not AFK
     *
     * @return Returns the System.currentTimeMillis() when the player was set AFK
     */
    public Long getAFKStart() {
        return afkStart;
    }

    /**
     * Starts AFK for this player with a broadcast, Use {@link #forceStartAFK()} for silent AFK
     * This can be cancelled with {@link AFKStartEvent}
     */
    public void startAFK() {
        if (Bukkit.getPlayer(getUUID()) == null) {
            //Player isn't online, stop here
            return;
        }
        //Call the AKFStart event
        AFKStartEvent event = new AFKStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        //Broadcast the AFK start message
        String message = plugin.config.getMessage("Broadcast.Start")
                .replace("{PLAYER}", getName());
        broadcast(message);
        //Start the AFK
        forceStartAFK();
    }

    /**
     * Silently starts AFK for this player
     */
    public void forceStartAFK() {
        //Record the time that the player was set AFK
        afkStart = System.currentTimeMillis();
        //Set the player as AFK
        isAFK = true;
    }

    /**
     * Stops AFK for this player with a broadcast, Use {@link #forceStopAFK()} for a silent stop
     * This can be cancelled with {@link AFKStopEvent}
     */
    public void stopAFK() {
        if (Bukkit.getPlayer(getUUID()) == null) {
            //Player isn't online, stop here
            return;
        }
        //Call the AKFStop event
        AFKStopEvent event = new AFKStopEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        //Get a string that is the user friendly version of how long the player was AFK
        //This will replace the {TIME} variable, if present
        String afkTime = plugin.prettyTime.format(plugin.reduceDurationList
                (plugin.prettyTime.calculatePreciseDuration(new Date(afkStart))));
        String message = plugin.config.getMessage("Broadcast.Stop")
                .replace("{PLAYER}", getName()).replace("{TIME}", afkTime);
        broadcast(message);
        forceStopAFK();

    }

    /**
     * Silently stops AFK for this player
     */
    public void forceStopAFK() {
        //Reset warning
        isWarned = false;
        //Set player as no longer AFK
        isAFK = false;
        //Disable inactivity to allow the interact to register
        isInactive = false;
        //Interact to update the last interact value
        interact();
    }

    /**
     * Broadcast a message using the settings from the config
     *
     * @param msg The message you wish to broadcast
     */
    public void broadcast(String msg) {
        //Don't broadcast if the message is empty
        if (msg.isEmpty()) {
            return;
        }
        boolean vanish = plugin.getConfig().getBoolean("Broadcast.Vanish");
        if (!vanish && isVanished()) {
            return;
        }
        Player player = Bukkit.getPlayer(getUUID());
        boolean console = plugin.getConfig().getBoolean("Broadcast.Console");
        boolean otherPlayers = plugin.getConfig().getBoolean("Broadcast.OtherPlayers");
        boolean self = plugin.getConfig().getBoolean("Broadcast.Self");
        if (console) {
            plugin.getLogger().info(msg);
        }
        if (otherPlayers) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(player)) {
                    continue;
                }
                p.sendMessage(msg);
            }
        }
        if (self) {
            player.sendMessage(msg);
        }
    }

    /**
     * Runs the action command on this player
     */
    public void takeAction() {
        String command = plugin.getConfig().getString("Action").replace("[PLAYER]", Bukkit.getPlayer(uuid).getName());
        AFKActionEvent event = new AFKActionEvent(this, command);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            forceStopAFK();
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), event.getAction()));
        }
    }

    /**
     * Log an interact, used by events for tracking when the player last did something
     * This will stop AFK if a player is AFK and update the lastInteract value
     */
    public void interact() {
        //Dont allow interact when the player is inactive
        //Inactive is decided by the listener class checking location data
        if (isInactive)
            return;
        lastInteract = System.currentTimeMillis();
        if (isAFK)
            stopAFK();
    }

    /**
     * Check if a player is currently vanished
     *
     * @return Returns true if the player is currently vanished
     */
    public boolean isVanished() {
        if (!Bukkit.getOfflinePlayer(getUUID()).isOnline()) {
            return false;
        }
        Player p = Bukkit.getPlayer(getUUID());
        for (MetadataValue meta : p.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    /**
     * This is the runnable that detects players who need to be set as AFK, warned or acted upon
     * It is run every second by default
     * This should not be used else where
     *
     * @return Returns the runnable used for AFK detection
     */
    public Runnable getRepeatingTask() {
        return () -> {
            if (Bukkit.getOfflinePlayer(getUUID()).isOnline()) {
                if (isAFK) {
                    //Get the values that need to be met for warnings and action
                    Integer timeToWarning = plugin.perms.getPermissionValue(uuid, Permission.TimeToWarning.getPermission());
                    Integer timeToAction = plugin.perms.getPermissionValue(uuid, Permission.TimeToAction.getPermission());
                    //Get the number of seconds since the player went AFK
                    Long secondsSinceAFKStart = (System.currentTimeMillis() - afkStart) / 1000;
                    //Don't check if we need to warn the player if waring is disabled  or there is a permission error
                    if (!timeToWarning.equals(-1) && !timeToWarning.equals(0)) {
                        //Check for warning
                        if (!isWarned && secondsSinceAFKStart >= timeToWarning) {
                            Bukkit.getScheduler().runTask(plugin, this::warnPlayer);
                        }
                    }
                    //Check if the player can have an action taken or if there is a permission error
                    if (!timeToAction.equals(-1) && !timeToAction.equals(0)) {
                        //Check for action
                        if (secondsSinceAFKStart >= timeToAction) {
                            Bukkit.getScheduler().runTask(plugin, this::takeAction);
                        }
                    }
                } else {
                    Integer timeToAFK = plugin.perms.getPermissionValue(uuid, Permission.TimeToAFK.getPermission());
                    //Check if the permission is 0 or -1
                    //-1 is for players who shouldn't be put into AFK by timer and 0 is to account for permission errors
                    if (timeToAFK.equals(-1) || timeToAFK.equals(0)) {
                        //This allows player to only be put into AFK by commands
                        return;
                    }
                    //Get the number of seconds since the last recorded interact
                    Long secondsSinceLastInteract = (System.currentTimeMillis() - lastInteract) / 1000;
                    //Set them as AFK if it is the same or longer than the time to AFK
                    if (secondsSinceLastInteract >= timeToAFK) {
                        Bukkit.getScheduler().runTask(plugin, this::startAFK);
                    }
                }
            }
        };
    }
}
