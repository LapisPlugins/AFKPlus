/*
 * Copyright 2018 Benjamin Martin
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

@SuppressWarnings("WeakerAccess")
public class AFKPlusPlayer {

    private AFKPlus plugin;
    private UUID uuid;
    private Long lastInteract;
    private Long afkStart;
    private boolean isAFK;
    private boolean isWarned;

    public AFKPlusPlayer(AFKPlus plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
    }

    public String getName() {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    public boolean isPermitted(Permission perm) {
        return plugin.permissions.isPermitted(uuid, perm.getPermission());
    }

    public void warnPlayer() {
        isWarned = true;
        //TODO make the warning happen
    }

    public boolean isAFK() {
        return isAFK;
    }

    public Long getAFKStart() {
        return afkStart;
    }

    public void startAFK() {
        String message = plugin.config.getMessage("Broadcast.Start")
                .replace("%PLAYER%", getName());
        Bukkit.broadcastMessage(message);
        forceStartAFK();
    }

    public void forceStartAFK() {
        afkStart = System.currentTimeMillis();
        isAFK = true;
    }

    public void stopAFK() {
        String message = plugin.config.getMessage("Broadcast.Stop")
                .replace("%PLAYER%", getName());
        Bukkit.broadcastMessage(message);
        forceStopAFK();
    }

    public void forceStopAFK() {
        isWarned = false;
        isAFK = false;
        interact();
    }

    public void takeAction() {
        //TODO take the action
    }

    public void interact() {
        lastInteract = System.currentTimeMillis();
        if (isAFK) {
            stopAFK();
        }
    }

    public Runnable getRepeatingTask() {
        return () -> {
            if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                if (isAFK) {
                    Integer timeToWarning = plugin.permissions.getPermissionValue(uuid, Permission.TimeToWarning.getPermission());
                    Integer timeToAction = plugin.permissions.getPermissionValue(uuid, Permission.TimeToAction.getPermission());
                    //Get the number of seconds since the player went AFK
                    Long secondsSinceAFKStart = (afkStart - System.currentTimeMillis()) / 1000;
                    //Check for warning
                    if (!isWarned && secondsSinceAFKStart >= timeToWarning) {
                        warnPlayer();
                    }
                    //Check for action
                    if (secondsSinceAFKStart >= timeToAction) {
                        takeAction();
                    }
                } else {
                    Integer timeToAFK = plugin.permissions.getPermissionValue(uuid, Permission.TimeToAFK.getPermission());
                    if (timeToAFK.equals(-1)) {
                        //This allows player to only be put into AFK by commands
                        return;
                    }
                    //Get the number of seconds since the last recorded interact
                    Long secondsSinceLastInteract = (lastInteract - System.currentTimeMillis()) / 1000;
                    //Set them as AFK if it is the same or longer than the time to AFK
                    if (secondsSinceLastInteract.intValue() >= timeToAFK) {
                        startAFK();
                    }
                }
            }
        };
    }
}
