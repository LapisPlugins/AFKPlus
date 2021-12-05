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

import org.bukkit.Location;

import java.util.HashMap;
import java.util.UUID;

public class PlayerMovementMonitoring {

    private final HashMap<UUID, RollingLocations> playerRollingTotals = new HashMap<>();

    public void logAndCheckMovement(UUID uuid, PlayerMovementStorage movement, double posTrigger, float lookTrigger) {
        //Log movement
        if (!playerRollingTotals.containsKey(uuid)) {
            playerRollingTotals.put(uuid, new PlayerMovementMonitoring.RollingLocations());
        }
        RollingLocations locs = playerRollingTotals.get(uuid);
        locs.addLocation(movement.to);

        //Check movement
        //Only change it if they might have moved only a little
        //Don't let them just sit there
        if (movement.didMove)
            movement.didMove = locs.checkPosition(posTrigger);

        if (movement.didLook)
            movement.didLook = locs.checkLook(lookTrigger);
    }

    private static class RollingLocations {

        //TODO: Refine this
        private final int samples = 20;
        private final Location[] locations = new Location[samples];
        private int i = 0;

        /**
         * Add the location to the locations array
         *
         * @param l the location to be added
         */
        public void addLocation(Location l) {
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
