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

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class AFKPlusConfiguration {

    private AFKPlus plugin;

    public AFKPlusConfiguration(AFKPlus p) {
        plugin = p;
        configVersion();
    }

    private YamlConfiguration getMessages() {
        File f = new File(plugin.getDataFolder() + File.separator + "messages.yml");
        if (!f.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        return YamlConfiguration.loadConfiguration(f);
    }

    private void configVersion() {
        if (plugin.getConfig().getInt("ConfigVersion") != 3) {
            File oldConfig = new File(plugin.getDataFolder() + File.separator + "config.yml");
            File backupConfig = new File(plugin.getDataFolder() + File.separator +
                    "Backup_config.yml");
            oldConfig.renameTo(backupConfig);
            plugin.saveDefaultConfig();
            plugin.logger.info("New config generated!");
            plugin.logger.info("Please transfer values!");
        }
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
