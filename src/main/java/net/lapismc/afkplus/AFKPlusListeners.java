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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;

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
        plugin.removePlayer(e.getPlayer().getUniqueId());
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
                if (plugin.getPlayer(p).isAFK()) {
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
        AFKPlayer player = plugin.getPlayer(p);
        if (player.isAFK()) {
            player.stopAFK();
        }
        player.setLastInteract();
    }
}
