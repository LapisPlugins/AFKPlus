/*
 * Copyright 2025 Benjamin Martin
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

package net.lapismc.afkplus.commands.tabcomplete;

import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.playerdata.Permission;
import net.lapismc.lapiscore.commands.tabcomplete.LapisTabOption;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerNames implements LapisTabOption {

    private final boolean includeOffline;

    public PlayerNames(boolean includeOffline) {
        this.includeOffline = includeOffline;
    }

    @Override
    public List<String> getOptions(CommandSender sender) {
        List<String> names = new ArrayList<>();
        if (includeOffline) {
            //This is used by /afkplus player command and so can include anyone who has played before
            for (OfflinePlayer op : Bukkit.getServer().getOfflinePlayers()) {
                names.add(op.getName());
            }
        } else {
            //This is used by /afk and so needs a permission check before recommending players other than yourself
            if (!(sender instanceof Player) ||
                    AFKPlus.getInstance().perms.isPermitted(((Player) sender).getUniqueId(), Permission.AFKOthers.getPermission()))
                Bukkit.getServer().getOnlinePlayers().forEach(player -> names.add(player.getName()));
            else
                names.add(sender.getName());
        }
        return names;
    }

    @Override
    public List<LapisTabOption> getChildren(CommandSender sender) {
        return new ArrayList<>();
    }
}
