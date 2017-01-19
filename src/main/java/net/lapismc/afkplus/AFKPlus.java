package net.lapismc.afkplus;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public final class AFKPlus extends JavaPlugin {

    HashMap<UUID, Long> timeSinceLastInteract = new HashMap<>();
    HashMap<UUID, Long> playersAFK = new HashMap<>();
    Logger logger = Bukkit.getLogger();

    Integer timer;

    AFKPlusListeners AFKListeners;
    AFKPlusConfiguration AFKConfig;

    @Override
    public void onEnable() {
        //TODO: register commands
        AFKListeners = new AFKPlusListeners(this);
        AFKConfig = new AFKPlusConfiguration(this);
        Bukkit.getPluginManager().registerEvents(AFKListeners, this);
    }

    @Override
    public void onDisable() {

    }

    private Runnable runnable() {
        return new Runnable() {
            @Override
            public void run() {
                Date date = new Date();
                for (UUID uuid : timeSinceLastInteract.keySet()) {
                    Long time = timeSinceLastInteract.get(uuid);
                    Long difference = (date.getTime() - time) / 1000;
                    if (difference.intValue() >= getConfig().getInt("TimeUntilAFK")) {
                        startAFK(uuid);
                    }
                }
                for (UUID uuid : playersAFK.keySet()) {
                    Long time = playersAFK.get(uuid);
                    Long difference = (date.getTime() - time) / 1000;
                    if (difference.intValue() >= getConfig().getInt("TimeUntilAction")) {
                        Player p = Bukkit.getPlayer(uuid);
                        switch (getConfig().getString("Action")) {
                            case "COMMAND":
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                        getConfig().getString("ActionVariable").replace("%NAME", p.getName()));
                                break;
                            case "MESSAGE":
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        getConfig().getString("ActionVariable").replace("%NAME", p.getName())));
                                break;
                            case "KICK":
                                p.kickPlayer(ChatColor.translateAlternateColorCodes('&',
                                        getConfig().getString("ActionVariable").replace("%NAME", p.getName())));
                                break;
                            default:
                                logger.severe("The AFK+ action is not correctly set in the config!");
                        }
                    }
                }
            }
        };
    }

    public void startTimer() {
        timer = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, runnable(), 20, 20);
    }

    public void startAFK(UUID uuid) {
        if (!playersAFK.containsKey(uuid)) {
            Date date = new Date();
            playersAFK.put(uuid, date.getTime());
            timeSinceLastInteract.remove(uuid);
            Bukkit.broadcastMessage(AFKConfig.getColoredMessage("AFKStart"));
        }
    }

    public void stopAFK(UUID uuid) {
        if (playersAFK.containsKey(uuid)) {
            Date date = new Date();
            playersAFK.remove(uuid);
            timeSinceLastInteract.put(uuid, date.getTime());
            Bukkit.broadcastMessage(AFKConfig.getColoredMessage("AFKStop"));
        }
    }
}
