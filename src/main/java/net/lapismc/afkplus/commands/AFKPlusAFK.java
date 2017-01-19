package net.lapismc.afkplus.commands;

import net.lapismc.afkplus.AFKPlus;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AFKPlusAFK implements CommandExecutor {

    private AFKPlus plugin;

    public AFKPlusAFK(AFKPlus p) {
        plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("afk")) {
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.AFKConfig.getMessage("AFKNotaPlayer"));
                    return true;
                }
                Player p = (Player) sender;
                if (plugin.playersAFK.containsKey(p.getUniqueId())) {
                    plugin.stopAFK(p.getUniqueId());
                } else {
                    plugin.startAFK(p.getUniqueId());
                    plugin.commandAFK.put(p.getUniqueId(), true);
                }
            } else if (args.length == 1) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
                if (op.isOnline()) {
                    Player p = op.getPlayer();
                    if (plugin.playersAFK.containsKey(p.getUniqueId())) {
                        plugin.stopAFK(p.getUniqueId());
                    } else {
                        plugin.startAFK(p.getUniqueId());
                        plugin.commandAFK.put(p.getUniqueId(), true);
                    }
                } else {
                    if (sender instanceof Player) {
                        sender.sendMessage(plugin.AFKConfig.getColoredMessage("AFKNotOnline"));
                    } else {
                        sender.sendMessage(plugin.AFKConfig.getMessage("AFKNotOnline"));
                    }
                }
            } else {
                sender.sendMessage("Usage: /afk (player) Permission required to use another players name");
            }
            return true;
        }
        return false;
    }

}
