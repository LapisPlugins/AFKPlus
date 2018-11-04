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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AFKPlusListeners implements Listener {

    AFKPlus plugin;

    AFKPlusListeners(AFKPlus plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.Chat")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.Move")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.Attack")) {
            if (e.getDamager() instanceof Player) {
                Player p = (Player) e.getDamager();
                plugin.getPlayer(p).interact();
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.Command")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.Interact")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.BlockPlace")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent e) {
        if (plugin.getConfig().getBoolean("EnabledDetections.BlockBreak")) {
            plugin.getPlayer(e.getPlayer()).interact();
        }
    }

}
