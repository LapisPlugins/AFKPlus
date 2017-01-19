package net.lapismc.afkplus;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class AFKPlusConfiguration {

    private AFKPlus plugin;

    public AFKPlusConfiguration(AFKPlus p) {
        plugin = p;
    }

    private YamlConfiguration getMessages() {
        File f = new File(plugin.getDataFolder() + File.separator + "messages.yml");
        if (!f.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        return YamlConfiguration.loadConfiguration(f);
    }

    public String getMessage(String path) {
        return ChatColor.stripColor(getColoredMessage(path));
    }

    public String getColoredMessage(String path) {
        if (getMessages().contains(path)) {
            return ChatColor.translateAlternateColorCodes('&', getMessages().getString(path));
        } else {
            return null;
        }
    }

}
