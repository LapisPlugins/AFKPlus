/*
 * Copyright 2017 Benjamin Martin
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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;

import java.util.Date;

public class AFKPlusListeners implements Listener {

    private AFKPlus plugin;

    AFKPlusListeners(AFKPlus p) {
        plugin = p;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (plugin.getConfig().getBoolean("EnabledListeners.Join")) {
            interact(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (plugin.timeSinceLastInteract.containsKey(p.getUniqueId())) {
            plugin.timeSinceLastInteract.remove(p.getUniqueId());
        }
        if (plugin.playersAFK.containsKey(p.getUniqueId())) {
            plugin.playersAFK.remove(p.getUniqueId());
        }
        if (plugin.commandAFK.containsKey(p.getUniqueId())) {
            plugin.commandAFK.remove(p.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        if (plugin.getConfig().getBoolean("EnabledListeners.Attack")) {
            if (e.getDamager() instanceof Player) {
                Player p = (Player) e.getDamager();
                interact(p);
            }
        }
        if (plugin.getConfig().getBoolean("AFKDamage")) {
            if (e.getEntity() instanceof Player) {
                Player p = (Player) e.getEntity();
                if (plugin.playersAFK.containsKey(p.getUniqueId())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (plugin.getConfig().getBoolean("EnabledListeners.BlockPlace")) {
            interact(e.getPlayer());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (plugin.getConfig().getBoolean("EnabledListeners.BlockBreak")) {
            interact(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (plugin.getConfig().getBoolean("EnabledListeners.Move")) {
            interact(e.getPlayer());
        }
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent e) {
        if (plugin.getConfig().getBoolean("EnabledListeners.BlockInteract")) {
            interact(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (plugin.getConfig().getBoolean("EnabledListeners.Chat")) {
            interact(e.getPlayer());
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (plugin.getConfig().getBoolean("EnabledListeners.Command")) {
            interact(e.getPlayer());
        }
    }

    private void interact(Player p) {
        Date date = new Date();
        if (plugin.playersAFK.containsKey(p.getUniqueId())) {
            plugin.stopAFK(p.getUniqueId());
        }
        plugin.timeSinceLastInteract.put(p.getUniqueId(), date.getTime());
    }
}
