/*
 * Copyright 2025 Benjamin Martin
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
import net.lapismc.afkplus.api.AFKStatisticManager;
import net.lapismc.afkplus.api.AFKStopEvent;
import net.lapismc.afkplus.util.AFKPlusDiscordSRVHook;
import net.lapismc.afkplus.util.EssentialsAFKHook;
import net.lapismc.lapiscore.compatibility.XSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.Date;
import java.util.List;
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
    private boolean isFakeAFK;
    private boolean isInactive;
    private boolean isWarned;

    /**
     * @param plugin The plugin instance for config and permissions access
     * @param uuid   The UUID of the player that this class should control
     */
    public AFKPlusPlayer(AFKPlus plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
        isAFK = false;
        isFakeAFK = false;
        isInactive = false;
        isWarned = false;
        lastInteract = System.currentTimeMillis();
    }

    /**
     * Get the UUID of the player
     *
     * @return the UUID of the player
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get the players username
     *
     * @return the name of the player
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
     * Check if the player has been marked as inactive by aggressive AFK detection
     * This will be true if the player has been exhibiting the behaviours tested for in the aggressive AFK detection code
     *
     * @return true if the player is labeled as inactive
     */
    public boolean isInactive() {
        return isInactive;
    }

    /**
     * Check if the player is permitted to do something
     * Permissions are stored in {@link Permission} as an Enumeration
     *
     * @param perm The permission you wish to check
     * @return true if the player DOESN'T have the permission
     */
    public boolean isNotPermitted(Permission perm) {
        return !plugin.perms.isPermitted(uuid, perm.getPermission());
    }

    /**
     * Warn the player with a message and sound if enabled
     */
    public void warnPlayer() {
        if (!isOnline()) {
            //Make sure the player is online
            return;
        }
        isWarned = true;
        Player p = Bukkit.getPlayer(uuid);
        //Send the player the warning message
        p.sendMessage(getMessage("Warning"));
        //Play the warning sound from the config
        playSound("WarningSound", XSound.ENTITY_PLAYER_LEVELUP);
    }

    /**
     * Check if the user has received a warning about being acted upon for being AFK
     *
     * @return true if the player has been warned, otherwise false
     */
    public boolean isWarned() {
        return isWarned;
    }

    /**
     * Set the warned state of the user, this is only used when resuming sessions
     *
     * @param isWarned the desired state of isWarned
     */
    protected void setIsWarned(boolean isWarned) {
        this.isWarned = isWarned;
    }

    /**
     * Check if the player is AFK
     *
     * @return true if the player is currently AFK
     */
    public boolean isAFK() {
        return isAFK;
    }

    /**
     * Check if the players AFK state is fake
     *
     * @return true if the player is both AFK and the AFK state is faked
     */
    public boolean isFakeAFK() {
        return isAFK && isFakeAFK;
    }

    /**
     * Set the fake AFK attribute after the fact, this is only used to restore sessions
     *
     * @param isFakeAFK the desired value for isFakeAFK
     */
    protected void setFakeAFK(boolean isFakeAFK) {
        this.isFakeAFK = isFakeAFK;
    }

    /**
     * Get the system time when the player became AFK
     * Could be null if the player is not AFK
     *
     * @return the System.currentTimeMillis() when the player was set AFK
     */
    public Long getAFKStart() {
        return afkStart;
    }

    /**
     * Set the time when the player entered AFK, this is used for resuming AFK after reconnect
     *
     * @param afkStart The UNIX Epoch of when the player entered AFK
     */
    protected void setAFKStart(Long afkStart) {
        this.afkStart = afkStart;
    }

    /**
     * Starts AFK for this player with a broadcast, Use {@link #forceStartAFK()} for silent AFK
     * This can be cancelled with {@link AFKStartEvent}
     */
    public void startAFK() {
        if (!isOnline()) {
            //Player isn't online, stop here
            return;
        }
        //Get the command and message for the AFK start event
        String command = plugin.getConfig().getString("Commands.AFKStart");
        String broadcastMessage = getMessage("Broadcast.Start");
        String selfMessage = getMessage("Self.Start");
        //Call the AKFStart event
        AFKStartEvent event = new AFKStartEvent(this, command, broadcastMessage, selfMessage);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        //Broadcast the AFK start message
        broadcastOthers(event.getBroadcastMessage().replace("{PLAYER}", getName()), true, null);
        selfMessage(event.getSelfMessage().replace("{PLAYER}", getName()));
        //Run AFK command
        runCommands(event.getCommand());
        //Play the AFK start sound
        playSound("AFKStartSound", XSound.BLOCK_ANVIL_HIT);
        //Start the AFK
        forceStartAFK();
    }

    /**
     * Overloads {@link #startAFK()} to set the AFK as fake
     *
     * @param isFake true if the player is to be set as "fake" AFK
     */
    public void startAFK(boolean isFake) {
        startAFK();
        //Check to make sure that AFK did start e.g. wasn't cancelled by the event API
        if (isAFK)
            isFakeAFK = isFake;
    }

    /**
     * Silently starts AFK for this player
     */
    public void forceStartAFK() {
        //Record the time that the player was set AFK
        afkStart = System.currentTimeMillis();
        //Set the player as AFK
        isAFK = true;
        //Update the players AFK status with the essentials plugin
        updateEssentialsAFKState();
        //Set if the player should be ignored for sleeping
        if (plugin.getConfig().getBoolean("IgnoreAFKPlayersForSleep")) {
            Player p = Bukkit.getPlayer(getUUID());
            if (p != null) {
                p.setSleepingIgnored(true);
            }
        }
    }

    /**
     * Stops AFK for this player with a broadcast, Use {@link #forceStopAFK()} or {@link #stopAFK(boolean)} for a silent stop
     * This can be cancelled with {@link AFKStopEvent}
     */
    public void stopAFK() {
        stopAFK(false);
    }

    /**
     * Stops AFK for this player with a broadcast, Use {@link #forceStopAFK()} for a silent stop
     * This can be cancelled with {@link AFKStopEvent}
     *
     * @param silent Skips broadcasting when true, used to cleanly exit afk when a player disconnects
     */
    public void stopAFK(boolean silent) {
        //Don't run if the player isn't actually AFK since we are running commands and messages
        if (!isAFK())
            return;
        //Get the command and broadcast message
        String command = plugin.getConfig().getString("Commands.AFKStop");
        String broadcastMessage = getMessage("Broadcast.Stop");
        String selfMessage = getMessage("Self.Stop");
        //Call the AKFStop event
        AFKStopEvent event = new AFKStopEvent(this, command, broadcastMessage, selfMessage);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        runCommands(event.getCommand());
        //Get a string that is the user-friendly version of how long the player was AFK
        //This will replace the {TIME} variable, if present
        String afkTime = plugin.prettyTime.formatDuration(plugin.reduceDurationList
                (plugin.prettyTime.calculatePreciseDuration(new Date(afkStart))));
        if (!silent) {
            broadcastOthers(event.getBroadcastMessage().replace("{PLAYER}", getName()).replace("{TIME}", afkTime), false, afkTime);
            selfMessage(event.getSelfMessage().replace("{PLAYER}", getName()).replace("{TIME}", afkTime));
        }
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
        isFakeAFK = false;
        //Disable inactivity to allow the interact events to register
        isInactive = false;
        //Interact to update the last interact value
        interact();
        //Update the players AFK status with the essentials plugin
        updateEssentialsAFKState();
        //Set the player back to being counted for sleep counts
        if (plugin.getConfig().getBoolean("IgnoreAFKPlayersForSleep")) {
            Player p = Bukkit.getPlayer(getUUID());
            if (p != null) {
                p.setSleepingIgnored(false);
            }
        }
    }

    /**
     * Check if the player's AFK status should be broadcast to other players
     *
     * @return whether the player's AFK message should be broadcast to other players
     */
    public boolean shouldBroadcastToOthers() {
        boolean vanish = plugin.getConfig().getBoolean("Broadcast.Vanish");
        boolean otherPlayers = plugin.getConfig().getBoolean("Broadcast.OtherPlayers");
        return (vanish || !isVanished()) && otherPlayers;
    }

    /**
     * Broadcast a message using the settings from the config
     *
     * @param msg           The message to broadcast
     * @param isStartingAFK Should be true if this broadcast is for a player starting AFK, if they are ending AFK it should be false
     * @param timeAFK       The human-readable string of how long the player was AFK, null if not applicable
     */
    public void broadcastOthers(String msg, boolean isStartingAFK, String timeAFK) {
        boolean console = plugin.getConfig().getBoolean("Broadcast.Console");
        //Only send to DiscordSRV if the option is enabled and the plugin is enabled
        boolean discordSrv = plugin.getConfig().getBoolean("Broadcast.DiscordSRV.Enabled") &&
                Bukkit.getServer().getPluginManager().isPluginEnabled("DiscordSRV");
        boolean otherPlayers = shouldBroadcastToOthers();
        //Don't broadcast if the message is empty
        if (!msg.isEmpty()) {
            //Broadcast the message to console and other players if its enabled
            if (console) {
                Bukkit.getConsoleSender().sendMessage(msg);
            }
            if (discordSrv) {
                if (plugin.getConfig().getBoolean("Broadcast.DiscordSRV.UseEmbeds")) {
                    timeAFK = plugin.getConfig().getBoolean("Broadcast.DiscordSRV.EmbedTimeAFK") ? timeAFK : null;
                    new AFKPlusDiscordSRVHook(plugin).sendAFKEmbed(this, isStartingAFK, timeAFK);
                } else {
                    String channelName = plugin.getConfig().getString("Broadcast.DiscordSRV.Channel", "global");
                    AFKPlusDiscordSRVHook.pushMessageToDiscord(channelName, msg);
                }
            }
            if (otherPlayers) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    //Skip the player represented by this class, messages to them will be handled by selfMessage
                    if (p.getUniqueId().equals(getUUID()))
                        continue;
                    //TODO: Handle chat component messages for hover text
                    p.sendMessage(msg);
                }
            }
        }
    }

    /**
     * Send the self message to the player if it is enabled
     *
     * @param msg The message to send
     */
    public void selfMessage(String msg) {
        //Get the config option for if players should be notified when they become AFK
        boolean self = plugin.getConfig().getBoolean("Broadcast.Self");
        //Get the player so we can null check them, this ensures that they are online
        Player p = Bukkit.getPlayer(uuid);
        //Check that the message has content, the player should receive messages and that the player is online
        if (!msg.isEmpty() && self && p != null) {
            //TODO: Also handle chat components here
            p.sendMessage(msg);
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
            stopAFK(true);
            runCommands(event.getCommand());
            plugin.tasks.runTaskLater(() -> {
                //Check if the player has been removed by the action, warn if they haven't
                //This can be silenced by setting ActionNoKick: true in the config
                if (Bukkit.getOfflinePlayer(getUUID()).isOnline()) {
                    if (plugin.getConfig().getBoolean("ActionNoKick"))
                        return;
                    plugin.getLogger().warning(getName() + " was acted upon by AFKPlus but is still online!");
                    plugin.getLogger().warning("This should not happen, please check your Action command in the config");
                    plugin.getLogger().warning("This message can be disabled by adding \"ActionNoKick: true\" to the config");
                }
            }, 2, false);
        }
    }

    /**
     * Log an interact, used by events for tracking when the player last did something
     * This will stop AFK if a player is AFK and update the lastInteract value
     */
    public void interact() {
        //Don't allow the player to interact when the player is inactive
        //Inactivity is decided by the listener class checking location data
        if (isInactive)
            return;
        lastInteract = System.currentTimeMillis();
        //Only take AFK stopping action if the player is AFK and not fake AFK
        if (isAFK && !isFakeAFK)
            stopAFK();
    }

    /**
     * This is the UNIX Epoch at the time that the player last triggered an interact event based on the current config
     *
     * @return the last time the player interacted
     */
    public Long getLastInteract() {
        return lastInteract;
    }

    /**
     * Set the last interact time for this player, used when resuming sessions
     *
     * @param lastInteract the UNIX Epoch at the time the player last interacted with the world
     */
    protected void setLastInteract(Long lastInteract) {
        this.lastInteract = lastInteract;
    }

    /**
     * Check if a player is currently vanished
     *
     * @return true if the player is currently vanished
     */
    public boolean isVanished() {
        if (!isOnline()) {
            return false;
        }
        Player p = Bukkit.getPlayer(getUUID());
        for (MetadataValue meta : p.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }


    /**
     * Check if this player is currently online
     *
     * @return true if the player is online, otherwise false
     */
    public boolean isOnline() {
        return Bukkit.getOfflinePlayer(getUUID()).isOnline();
    }

    /**
     * Get the total time that a player has been AFK
     * This is the sum of all time that the player has been AFK
     *
     * @return The total time spent AFK, 0 if there is no record for this player, -1 if stats are not enabled
     */
    public long getTotalTimeAFK() {
        //Get the statistics manager
        AFKStatisticManager statisticManager = new AFKStatisticManager(plugin);
        //Get and return the total time AFK
        return statisticManager.getTotalTimeAFK(this);
    }

    /**
     * Updates the players current AFK State within the essentials plugin
     */
    private void updateEssentialsAFKState() {
        //Allow users to configure if this feature is enabled
        if (!plugin.getConfig().getBoolean("EssentialsAFKHook"))
            return;
        //Update the AFK state with essentials if it is installed
        if (!Bukkit.getPluginManager().isPluginEnabled("Essentials"))
            return;
        EssentialsAFKHook essHook = new EssentialsAFKHook();
        essHook.setAFK(getUUID(), isAFK);
    }

    /**
     * Handles the running of a command with a player variable, this is used for AFK start/stop/warn/action commands
     * The string can split commands using a semicolon
     *
     * @param commandsString The command to be run with "[PLAYER]" in place of the players name
     */
    private void runCommands(String commandsString) {
        //Ignore the command if it is blank, this is so that start/stop/warn events don't need to have commands
        if (commandsString.isEmpty())
            return;
        //Replace the player variable with the players name
        String finalCommandsString = commandsString.replace("[PLAYER]", getName());
        //Define a runnable to dispatch the command
        Runnable commandTask = () -> {
            boolean activeState = isInactive;
            isInactive = true;
            List<String> commands = List.of(finalCommandsString.split(";"));
            for (String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
            isInactive = activeState;
        };
        //Check if we are on the primary thread
        //If we aren't then we need to run in a task on the main thread to dispatch a command
        //If we are, then we might be mid-disable which means we cant register a task
        if (Bukkit.isPrimaryThread()) {
            //We are on the main thread so we can run the command here
            commandTask.run();
        } else {
            //Dispatch the command on the next game tick
            plugin.tasks.runTask(commandTask, false);
        }
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
        if (!isOnline())
            return;
        Player p = Bukkit.getPlayer(getUUID());
        String soundName = plugin.getConfig().getString(pathToSound);
        if ("".equals(soundName) || soundName == null)
            return;
        XSound sound;
        Optional<XSound> retrievedSound = XSound.matchXSound(soundName);
        sound = retrievedSound.orElse(def);
        sound.play(p);
    }

    /**
     * Records the time spent AFK and adds it to the already existing value in the statistics file
     */
    private void recordTimeStatistic() {
        //Load the statistics manager
        AFKStatisticManager statisticManager = new AFKStatisticManager(plugin);
        //Calculate the amount of time that the player was AFK for
        Long timeAFK = System.currentTimeMillis() - afkStart;
        //Tell the manager to increment the players time by this amount
        statisticManager.incrementTotalTimeAFK(this, timeAFK);
    }

    /**
     * This is the runnable that detects players who need to be set as AFK, warned or acted upon
     * It is run every second by default
     * This should not be used else where
     *
     * @return the runnable used for AFK detection
     */
    public Runnable getRepeatingTask() {
        return () -> {
            if (isOnline()) {
                if (isAFK) {
                    boolean isAtPlayerRequirement;
                    int playersRequired = plugin.getConfig().getInt("ActionPlayerRequirement");
                    if (playersRequired == 0) {
                        isAtPlayerRequirement = true;
                    } else {
                        isAtPlayerRequirement = Bukkit.getOnlinePlayers().size() > playersRequired;
                    }
                    //Get the values that need to be met for warnings and action
                    Integer timeToWarning = plugin.perms.getPermissionValue(uuid, Permission.TimeToWarning.getPermission());
                    Integer timeToAction = plugin.perms.getPermissionValue(uuid, Permission.TimeToAction.getPermission());
                    //Get the number of seconds since the player went AFK
                    long secondsSinceAFKStart = (System.currentTimeMillis() - afkStart) / 1000;
                    //Don't check if we need to warn the player if waring is disabled
                    if (!timeToWarning.equals(-1)) {
                        //Check for warning
                        if (!isWarned && secondsSinceAFKStart >= timeToWarning) {
                            plugin.tasks.runTask(this::warnPlayer, false);
                        }
                    }
                    //Check if the player can have an action taken
                    if (!timeToAction.equals(-1)) {
                        //Check for action and if we are taking action yet
                        if (secondsSinceAFKStart >= timeToAction && isAtPlayerRequirement) {
                            plugin.tasks.runTask(this::takeAction, false);
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
                    long secondsSinceLastInteract = (System.currentTimeMillis() - lastInteract) / 1000;
                    //Set them as AFK if it is the same or longer than the time to AFK
                    if (secondsSinceLastInteract >= timeToAFK) {
                        plugin.tasks.runTask(this::startAFK, false);
                    }
                }
            }
        };
    }
}
