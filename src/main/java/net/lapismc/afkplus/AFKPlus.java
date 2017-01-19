package net.lapismc.afkplus;

import net.lapismc.afkplus.commands.AFKPlusAFK;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public final class AFKPlus extends JavaPlugin {

    public HashMap<UUID, Long> timeSinceLastInteract = new HashMap<>();
    public HashMap<UUID, Boolean> commandAFK = new HashMap<>();
    public HashMap<UUID, Long> playersAFK = new HashMap<>();
    public Logger logger = Bukkit.getLogger();
    public AFKPlusListeners AFKListeners;
    public AFKPlusConfiguration AFKConfig;
    Integer timer;

    @Override
    public void onEnable() {
        this.getCommand("afkplus").setExecutor(new net.lapismc.afkplus.commands.AFKPlus(this));
        this.getCommand("afk").setExecutor(new AFKPlusAFK(this));
        saveDefaultConfig();
        AFKListeners = new AFKPlusListeners(this);
        AFKConfig = new AFKPlusConfiguration(this);
        Bukkit.getPluginManager().registerEvents(AFKListeners, this);
        startTimer();
    }

    private Runnable runnable(AFKPlus plugin) {
        return new Runnable() {
            @Override
            public void run() {
                Date date = new Date();
                for (UUID uuid : timeSinceLastInteract.keySet()) {
                    Long time = timeSinceLastInteract.get(uuid);
                    Long difference = (date.getTime() - time) / 1000;
                    if (difference.intValue() >= getConfig().getInt("TimeUntilAFK")) {
                        startAFK(uuid);
                        commandAFK.put(uuid, false);
                    }
                }
                for (UUID uuid : playersAFK.keySet()) {
                    Long time = playersAFK.get(uuid);
                    Long difference = (date.getTime() - time) / 1000;
                    if (difference.intValue() >= getConfig().getInt("TimeUntilAction")) {
                        Player p = Bukkit.getPlayer(uuid);
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                            @Override
                            public void run() {
                                playersAFK.remove(uuid);
                            }
                        }, 10l);
                        switch (getConfig().getString("Action")) {
                            case "COMMAND":
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                        getConfig().getString("ActionVariable").replace("%NAME", p.getName()));
                                break;
                            case "MESSAGE":
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('&',
                                        getConfig().getString("ActionVariable").replace("%NAME", p.getName()))));
                                break;
                            case "KICK":
                                p.kickPlayer(ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('&',
                                        getConfig().getString("ActionVariable").replace("%NAME", p.getName()))));
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
        timer = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, runnable(this), 20, 20);
    }

    public void startAFK(UUID uuid) {
        if (!playersAFK.containsKey(uuid)) {
            Date date = new Date();
            playersAFK.put(uuid, date.getTime());
            timeSinceLastInteract.remove(uuid);
            Bukkit.broadcastMessage(AFKConfig.getColoredMessage("AFKStart").replace("%NAME", Bukkit.getPlayer(uuid).getName()));
        }
    }

    public void stopAFK(UUID uuid) {
        if (playersAFK.containsKey(uuid)) {
            Date date = new Date();
            playersAFK.remove(uuid);
            commandAFK.remove(uuid);
            timeSinceLastInteract.put(uuid, date.getTime());
            Bukkit.broadcastMessage(AFKConfig.getColoredMessage("AFKStop").replace("%NAME", Bukkit.getPlayer(uuid).getName()));
        }
    }
}
