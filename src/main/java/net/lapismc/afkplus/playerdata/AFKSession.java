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
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * A class for tracking players who have disconnected from the server
 * This class is used to ensure that players cannot skirt AFK tracking by relogging
 */
public class AFKSession {

    private final AFKPlus plugin;
    private final UUID playerUUID;
    private final boolean isAFK, isFakeAFK, isWarned, isInactive;
    private final Long disconnectTime, relativeLastInteract, relativeAFKStart;

    /**
     * Create a session for the provided player and record the disconnect time as now
     *
     * @param plugin The plugins main class for config access
     * @param player The player who is disconnecting
     */
    public AFKSession(AFKPlus plugin, AFKPlusPlayer player) {
        this.plugin = plugin;
        playerUUID = player.getUUID();
        isAFK = player.isAFK();
        relativeAFKStart = System.currentTimeMillis() - player.getAFKStart();
        isFakeAFK = player.isFakeAFK();
        isWarned = player.isWarned();
        isInactive = player.isInactive();
        relativeLastInteract = System.currentTimeMillis() - player.getLastInteract();
        disconnectTime = System.currentTimeMillis();
    }

    /**
     * Process a player reconnecting, this is mostly setting AFKPlayer values
     *
     * @param p The player who has connected
     */
    public void processReconnect(AFKPlusPlayer p) {
        //Check if it's a short enough offline time to process this as a reconnect vs a new session
        //Milliseconds since the user disconnected
        long millisOffline = System.currentTimeMillis() - disconnectTime;
        //Minutes since the user disconnected
        float minutesOffline = millisOffline / 1000.0f / 60.0f;
        double sessionLength = plugin.getConfig().getDouble("SessionLength");
        if (sessionLength < minutesOffline) {
            //The user has been offline longer than the session length
            //Therefore we ignore the users session and reset them
            p.forceStopAFK();
            //return so that the session logic is skipped
            return;
        }
        if (isAFK) {
            //If the player was AFK, set them as AFK and set the AFKTime
            p.forceStartAFK();
            //Calculate what the AFK start time would've been if the disconnect didn't happen
            Long AFKStart = System.currentTimeMillis() - relativeAFKStart;
            p.setAFKStart(AFKStart);
            //Make sure we update the FakeAFK status
            if (isFakeAFK)
                p.setFakeAFK(true);
            if (isWarned)
                p.setIsWarned(true);
            //Send a message to the player to let them know that their AFK state has been resumed
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    Bukkit.getPlayer(playerUUID).sendMessage(plugin.config.getMessage("Self.Resume")), 20);
        }
        p.setInactive(isInactive);
        //Set the last interact time based on the value at disconnect
        p.setLastInteract(System.currentTimeMillis() - relativeLastInteract);
    }

    /**
     * Get the UUID of the player that this class represents
     *
     * @return a player UUID
     */
    public UUID getUUID() {
        return playerUUID;
    }

}
