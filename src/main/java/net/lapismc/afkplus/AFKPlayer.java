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
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AFKPlayer {

    private AFKPlus plugin;
    private UUID uuid;
    private Long lastInteract;
    private Long timeAFK;
    private boolean isCommandAFK;
    private boolean isWarned;
    private boolean isAFK;

    public AFKPlayer(AFKPlus plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
    }

    public AFKPlayer(AFKPlus plugin, OfflinePlayer op) {
        this(plugin, op.getUniqueId());
    }

    public AFKPlayer(AFKPlus plugin, Player p) {
        this(plugin, p.getUniqueId());
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isAFK() {
        return isAFK;
    }

    public void setAFK(boolean afk) {
        isAFK = afk;
    }

    public boolean isWarned() {
        return isWarned;
    }

    public boolean isCommandAFK() {
        return isCommandAFK;
    }

    public Long getLastInteract() {
        return lastInteract;
    }

    public void setLastInteract() {
        lastInteract = System.currentTimeMillis();
    }

    public void warnPlayer() {
        Player p = Bukkit.getPlayer(uuid);
        p.sendMessage(plugin.AFKConfig.getColoredMessage("WarnMessage"));
        playWarningTone(p);
    }

    public void startAFK(Boolean command) {
        if (!isAFK) {
            timeAFK = System.currentTimeMillis();
            isCommandAFK = command;
            isAFK = true;
            lastInteract = null;
            Bukkit.broadcastMessage(plugin.AFKConfig.getColoredMessage("AFKStart")
                    .replace("%NAME", Bukkit.getPlayer(uuid).getName()));
        }
    }

    public void stopAFK() {
        if (isAFK) {
            isWarned = false;
            isAFK = false;
            isCommandAFK = false;
            lastInteract = System.currentTimeMillis();
            Bukkit.broadcastMessage(plugin.AFKConfig.getColoredMessage("AFKStop")
                    .replace("%NAME", Bukkit.getPlayer(uuid).getName()));
        }
    }

    private void playWarningTone(Player p) {
        for (int i = 0; i < 5; i++) {
            p.playNote(p.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.C));
        }
    }
}
