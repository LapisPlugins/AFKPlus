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

package net.lapismc.afkplus;

import org.bukkit.Bukkit;

import java.util.UUID;

public class AFKPlusPlayer {

    private AFKPlus plugin;
    private UUID uuid;
    private Long lastInteract;
    private boolean isAFK;
    private boolean isWarned;

    public AFKPlusPlayer(AFKPlus plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
    }

    public boolean isWarned() {
        return isWarned;
    }

    public boolean isAFK() {
        return isAFK;
    }

    public void startAFK() {
        //TODO send afk message and take action
        isAFK = true;
    }

    public void forceStartAFK() {
        isAFK = true;
    }

    public void stopAFK() {
        //TODO send afk message and take action
        isAFK = false;
    }

    public void forceStopAFK() {
        isAFK = false;
    }

    public void interact() {
        lastInteract = System.currentTimeMillis();
        if (isAFK) {
            stopAFK();
        }
    }

    Runnable getRepeatingTask() {
        return () -> {
            if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                //TODO check if player should be AFK, Warned or Kicked
            }
        };
    }

}
