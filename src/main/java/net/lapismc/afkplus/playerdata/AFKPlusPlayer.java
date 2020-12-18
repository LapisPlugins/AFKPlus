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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
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
        p.sendMessage(getMessage("Warning"));
        //Play the warning sound from the config
        playSound("WarningSound", XSound.ENTITY_PLAYER_LEVELUP);
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
        //Get the command and message for the AFK start event
        String command = plugin.getConfig().getString("Commands.AFKStart");
        String message = getMessage("Broadcast.Start");
        //Call the AKFStart event
        AFKStartEvent event = new AFKStartEvent(this, command, message);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        //Broadcast the AFK start message
        broadcast(event.getBroadcastMessage().replace("{PLAYER}", getName()));
        //Run AFK command
        runCommand(event.getCommand());
        //Play the AFK start sound
        playSound("AFKStartSound", XSound.BLOCK_ANVIL_HIT);
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
        //Get the command and broadcast message
        String command = plugin.getConfig().getString("Commands.AFKStop");
        String message = getMessage("Broadcast.Stop");
        //Call the AKFStop event
        AFKStopEvent event = new AFKStopEvent(this, command, message);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        runCommand(event.getCommand());
        //Get a string that is the user friendly version of how long the player was AFK
        //This will replace the {TIME} variable, if present
        String afkTime = plugin.prettyTime.formatDuration(plugin.reduceDurationList
                (plugin.prettyTime.calculatePreciseDuration(new Date(afkStart))));
        broadcast(event.getBroadcastMessage().replace("{PLAYER}", getName()).replace("{TIME}", afkTime));
        //Stop the AFK status
        forceStopAFK();

    }

    /**
     * Silently stops AFK for this player
     */
    public void forceStopAFK() {
        //Record the new value for the time AFK statistic
        if (isAFK())
            recordTimeStatistic();
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
        boolean console = plugin.getConfig().getBoolean("Broadcast.Console");
        boolean otherPlayers = plugin.getConfig().getBoolean("Broadcast.OtherPlayers");
        boolean self = plugin.getConfig().getBoolean("Broadcast.Self");
        if (!vanish && isVanished()) {
            //Stop the broadcast from going to other players, but it still shows to the player and console
            otherPlayers = false;
        }
        Player player = Bukkit.getPlayer(getUUID());
        if (console) {
            Bukkit.getLogger().info(msg);
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
        String command = plugin.getConfig().getString("Commands.Action");
        AFKActionEvent event = new AFKActionEvent(this, command);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            forceStopAFK();
            runCommand(event.getCommand());
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
     * Get the total time that a player has been AFK
     * This is the sum of all time that the player has been AFK
     *
     * @return The total time spent AFK, 0 if there is no record for this player
     */
    public long getTotalTimeAFK() {
        //Get or create the statistics file
        File f = new File(plugin.getDataFolder(), "statistics.yml");
        if (!f.exists()) {
            return 0L;
        }
        YamlConfiguration statistics = YamlConfiguration.loadConfiguration(f);
        //Grab the current value of the statistic so that we can add to it, or get 0L if there is no current value
        return statistics.getLong(getName() + ".TimeSpentAFK", 0L);
    }

    /**
     * Handles the running of a command with a player variable, this is used for AFK start/stop/warn/action commands
     *
     * @param command The command to be run with "[PLAYER]" in place of the players name
     */
    private void runCommand(String command) {
        //Ignore the command if it is blank, this is so that start/stop/warn events dont need to have commands
        if (command.equals(""))
            return;
        //Replace the player variable with the players name
        String cmd = command.replace("[PLAYER]", getName());
        //Dispatch the command on the next game tick
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
    }

    /**
     * Uses the PAPI LapisCore integration to attempt placeholder replacement
     *
     * @param key The key for the message in the messages.yml
     * @return the formatted message
     */
    private String getMessage(String key) {
        return plugin.config.getMessage(key, Bukkit.getOfflinePlayer(getUUID()));
    }

    /**
     * Plays a sound from the config or the default sound if its not available
     *
     * @param pathToSound The path to the sounds name in the config.yml
     * @param def         The sound to be used if the sound in the config isn't valid
     */
    private void playSound(String pathToSound, XSound def) {
        Player p = Bukkit.getPlayer(getUUID());
        if (p == null || !p.isOnline())
            return;
        String soundName = plugin.getConfig().getString(pathToSound);
        if ("".equals(soundName) || soundName == null)
            return;
        XSound sound;
        Optional<XSound> retrievedSound = XSound.matchXSound(soundName);
        sound = retrievedSound.orElse(def);
        sound.playSound(p);
    }

    /**
     * Records the time spent AFK and adds it to the already existing value in the statistics file
     */
    private void recordTimeStatistic() {
        //Get or create the statistics file
        File f = new File(plugin.getDataFolder(), "statistics.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration statistics = YamlConfiguration.loadConfiguration(f);
        //Grab the current value of the statistic so that we can add to it, or get 0L if there is no current value
        Long currentTimeSpendAFK = statistics.getLong(getName() + ".TimeSpentAFK", 0L);
        //Calculate the amount of time that the player was AFK for
        Long timeAFK = System.currentTimeMillis() - afkStart;
        //Set the value to be the old value plus the most recent amount of time AFK
        statistics.set(getName() + ".TimeSpentAFK", currentTimeSpendAFK + timeAFK);
        //Save the file
        try {
            statistics.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    boolean isAtPlayerRequirement;
                    int playersRequired = plugin.getConfig().getInt("ActionPlayerRequirement");
                    if (playersRequired != 0) {
                        isAtPlayerRequirement = true;
                    } else {
                        isAtPlayerRequirement = Bukkit.getOnlinePlayers().size() > playersRequired;
                    }
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
                        //Check for action and if we are taking action yet
                        if (secondsSinceAFKStart >= timeToAction && isAtPlayerRequirement) {
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
