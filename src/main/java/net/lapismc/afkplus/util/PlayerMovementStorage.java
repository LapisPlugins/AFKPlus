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
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * This class is used to cleanup duplicate code from different {@link PlayerMoveEvent}s in the {@link net.lapismc.afkplus.AFKPlusListeners} class
 */
public class PlayerMovementStorage {

    public Location to, from;
    public boolean didLook, didMove;

    public PlayerMovementStorage(PlayerMoveEvent e) {
        //Check if the player has looked and/or moved
        to = e.getTo();
        from = e.getFrom();
        didLook = to.getPitch() != from.getPitch() || to.getYaw() != from.getYaw();
        didMove = to.getX() != from.getX() || to.getY() != from.getY() || to.getZ() != from.getZ();
    }

}
