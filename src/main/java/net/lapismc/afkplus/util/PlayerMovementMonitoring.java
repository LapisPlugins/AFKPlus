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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class PlayerMovementMonitoring {

    private final HashMap<UUID, RollingLocations> playerRollingTotals = new HashMap<>();

    public PlayerMovementMonitoring() {
        //TODO: setup a way to cancel this task onDisable
        Bukkit.getScheduler().runTaskTimerAsynchronously(AFKPlus.getInstance(), getRepeatingTask(), 5, 5);
    }

    public void logAndCheckMovement(UUID uuid, PlayerMovementStorage movement, double posTrigger, float lookTrigger) {
        //Log movement
        logMovement(uuid, movement.to);

        //Check movement
        //Only change it if they might have moved only a little
        //Don't let them just sit there
        RollingLocations locs = getPlayerRollingTotal(uuid);
        if (movement.didMove)
            movement.didMove = locs.checkPosition(posTrigger);

        if (movement.didLook)
            movement.didLook = locs.checkLook(lookTrigger);
    }

    public void logMovement(UUID uuid, Location loc) {
        RollingLocations locs = getPlayerRollingTotal(uuid);
        locs.addLocation(loc);
    }

    private RollingLocations getPlayerRollingTotal(UUID uuid) {
        if (!playerRollingTotals.containsKey(uuid)) {
            playerRollingTotals.put(uuid, new PlayerMovementMonitoring.RollingLocations());
        }
        return playerRollingTotals.get(uuid);
    }

    private Runnable getRepeatingTask() {
        return () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                RollingLocations locs = getPlayerRollingTotal(p.getUniqueId());
                //Number of ticks the players must stop for before we update
                long allowedTime = 2 * 50L;
                if (locs.time + allowedTime < new Date().getTime()) {
                    //The players' location hasn't updated in 2 ticks
                    logMovement(p.getUniqueId(), p.getLocation());
                }
            }
        };
    }

    private static class RollingLocations {

        private final int samples = 10;
        private final Location[] locations = new Location[samples];
        private int i = 0;
        protected long time;

        /**
         * Add the location to the locations array
         *
         * @param l the location to be added
         */
        public void addLocation(Location l) {
            //Log the current time for stationary updating
            time = new Date().getTime();
            //Put the location in
            locations[i] = l;
            //Increment the index
            i++;
            //Reset the index to zero, so we can't overflow the array
            if (i == samples) i = 0;
        }

        /**
         * Check if the player has moved far enough recently
         *
         * @param trigger The distance the player needs to have moved
         * @return True if the player has moved more than the trigger distance, otherwise false
         */
        public boolean checkPosition(double trigger) {
            double totalMovement = 0.0f;
            for (int j = 1; j < locations.length; j++) {
                Location to = locations[j];
                Location from = locations[j - 1];
                if (to == null || from == null) {
                    if (totalMovement == 0)
                        return false;
                    else
                        continue;
                }
                try {
                    totalMovement += to.distanceSquared(from);
                } catch (IllegalArgumentException e) {
                    return true;
                }
            }
            totalMovement = Math.sqrt(totalMovement);
            return totalMovement > trigger;
        }

        /**
         * Check if the player has looked far enough recently
         *
         * @param trigger The angle the player needs to have looked
         * @return True if the player has looked around more than the trigger angle, otherwise false
         */
        public boolean checkLook(float trigger) {
            float totalLookAngle = 0.0f;
            for (int j = 1; j < locations.length; j++) {
                Location to = locations[j];
                Location from = locations[j - 1];
                if (to == null || from == null) {
                    if (totalLookAngle == 0)
                        return false;
                    else
                        continue;
                }
                totalLookAngle += to.getDirection().angle(from.getDirection());
            }
            return totalLookAngle > trigger;
        }

    }

}
