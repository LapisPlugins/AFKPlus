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
    private Long AFKTime;
    private boolean shouldIgnore;
    private boolean isCommandAFK;
    private boolean isAFK;
    private boolean isWarned;
    private boolean shouldCollide;

    private AFKPlayer(AFKPlus plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
    }

    AFKPlayer(AFKPlus plugin, OfflinePlayer op) {
        this(plugin, op.getUniqueId());
    }

    AFKPlayer(AFKPlus plugin, Player p) {
        this(plugin, p.getUniqueId());
    }

    UUID getUuid() {
        return uuid;
    }

    public boolean isAFK() {
        return isAFK;
    }

    void setAFK(boolean afk) {
        isAFK = afk;
        if (!afk) {
            isWarned = false;
        }
    }

    Long getAFKTime() {
        return AFKTime;
    }

    public boolean isCommandAFK() {
        return isCommandAFK;
    }

    public Long getLastInteract() {
        return lastInteract;
    }

    void setLastInteract() {
        lastInteract = System.currentTimeMillis();
    }

    void warnPlayer() {
        if (!isWarned) {
            Player p = Bukkit.getPlayer(uuid);
            p.sendMessage(plugin.AFKConfig.getColoredMessage("WarnMessage"));
            playWarningTone(p);
            isWarned = true;
        }
    }

    public void startAFK(Boolean command) {
        if (!isAFK) {
            setAFK(true);
            AFKTime = System.currentTimeMillis();
            isCommandAFK = command;
            lastInteract = null;
            Bukkit.broadcastMessage(plugin.AFKConfig.getColoredMessage("AFKStart")
                    .replace("%NAME", Bukkit.getPlayer(uuid).getName()));
            startIgnoring();
            // if the player is currently collidable and they shouldn't be while afk
            if (Bukkit.getPlayer(uuid).isCollidable() && !plugin.getConfig().getBoolean("PlayerCollision")) {
                // set the should collide tag so that we know to re-enable collision when they exist AFK
                shouldCollide = true;
                Bukkit.getPlayer(uuid).setCollidable(false);
            } else {
                shouldCollide = false;
            }
            if (!plugin.getConfig().getString("StartCommand").equalsIgnoreCase("")) {
                Player p = Bukkit.getPlayer(uuid);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        plugin.getConfig().getString("StartCommand").replace("%NAME%", p.getName()));
            }
        }
    }

    public void stopAFK() {
        if (isAFK) {
            setAFK(false);
            isCommandAFK = false;
            lastInteract = System.currentTimeMillis();
            Bukkit.broadcastMessage(plugin.AFKConfig.getColoredMessage("AFKStop")
                    .replace("%NAME", Bukkit.getPlayer(uuid).getName()));
            startIgnoring();
            if (shouldCollide) {
                Bukkit.getPlayer(uuid).setCollidable(true);
                shouldCollide = false;
            }
            if (!plugin.getConfig().getString("StopCommand").equalsIgnoreCase("")) {
                Player p = Bukkit.getPlayer(uuid);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        plugin.getConfig().getString("StopCommand").replace("%NAME%", p.getName()));
            }
        }
    }

    private void startIgnoring() {
        shouldIgnore = true;
        Bukkit.getScheduler().runTaskLater(plugin, () -> shouldIgnore = false, 20);
    }

    private void playWarningTone(Player p) {
        for (int i = 0; i < 5; i++) {
            p.playNote(p.getLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.C));
        }
    }

    boolean shouldIgnore() {
        return shouldIgnore;
    }
}
