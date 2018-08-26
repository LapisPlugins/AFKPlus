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
import net.lapismc.afkplus.AFKPlusPermissions;
import net.lapismc.afkplus.AFKPlusPlayer;
import net.lapismc.lapiscore.LapisCoreCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public abstract class AFKPlusCommand extends LapisCoreCommand {

    protected AFKPlus plugin;

    public AFKPlusCommand(AFKPlus plugin, String name, String desc, ArrayList<String> aliases) {
        super(plugin, name, desc, aliases);
        this.plugin = plugin;
    }

    protected boolean isPermitted(CommandSender sender, AFKPlusPermissions.AFKPlusPermission permission) {
        if (sender instanceof Player) {
            return getPlayer((Player) sender).isPermitted(permission);
        }
        return true;
    }

    protected AFKPlusPlayer getPlayer(UUID uuid) {
        return plugin.getPlayer(uuid);
    }

    protected AFKPlusPlayer getPlayer(OfflinePlayer op) {
        return plugin.getPlayer(op);
    }

}
