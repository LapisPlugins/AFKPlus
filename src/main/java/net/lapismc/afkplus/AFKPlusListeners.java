package net.lapismc.afkplus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Date;

public class AFKPlusListeners implements Listener {

    private AFKPlus plugin;

    public AFKPlusListeners(AFKPlus p) {
        plugin = p;
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
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (e.getFrom().getBlock() != e.getTo().getBlock()) {
            interact(p);
        }
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        interact(p);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        interact(p);
    }

    public void interact(Player p) {
        Date date = new Date();
        if (plugin.playersAFK.containsKey(p.getUniqueId())) {
            plugin.stopAFK(p.getUniqueId());
            plugin.timeSinceLastInteract.put(p.getUniqueId(), date.getTime());
        } else {
            plugin.timeSinceLastInteract.put(p.getUniqueId(), date.getTime());
        }
    }
}
