/*
 * Copyright 2020 Benjamin Martin
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
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import net.lapismc.afkplus.playerdata.Permission;
import net.lapismc.lapiscore.commands.LapisCoreCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Date;

public abstract class AFKPlusCommand extends LapisCoreCommand {

    protected AFKPlus plugin;

    protected AFKPlusCommand(AFKPlus plugin, String name, String desc, ArrayList<String> aliases) {
        super(plugin, name, desc, aliases, true);
        this.plugin = plugin;
    }

    protected boolean isNotPermitted(CommandSender sender, Permission permission) {
        return !isPermitted(sender, permission.getPermission());
    }

    protected String getTimeDifference(Long epoch) {
        return plugin.prettyTime.format(plugin.reduceDurationList(plugin.prettyTime.calculatePreciseDuration(new Date(epoch))));
    }

    protected AFKPlusPlayer getPlayer(OfflinePlayer op) {
        return plugin.getPlayer(op);
    }

}
