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

package net.lapismc.afkplus.util;

import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.AFKPlusPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;

public abstract class LapisCommand extends BukkitCommand {

    protected AFKPlus plugin;

    public LapisCommand(AFKPlus plugin, String name, String desc, ArrayList<String> aliases) {
        super(name);
        this.plugin = plugin;
        setDescription(desc);
        setAliases(aliases);
        registerCommand(name);
    }


    private void registerCommand(String name) {
        try {
            final Field serverCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            serverCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) serverCommandMap.get(Bukkit.getServer());
            commandMap.register(name, this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    protected boolean isNotAdmin(CommandSender sender) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (!plugin.AFKPerms.isPermitted(p.getUniqueId(), AFKPlusPerms.Perm.Admin)) {
                p.sendMessage(plugin.AFKConfig.getColoredMessage("NoPerms"));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        onCommand(sender, this, commandLabel, args);
        return true;
    }

    public abstract void onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args);

}
