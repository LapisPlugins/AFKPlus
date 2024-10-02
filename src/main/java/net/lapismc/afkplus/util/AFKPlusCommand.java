/*
 * Copyright 2024 Benjamin Martin
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

    /**
     * Check if a command sender is given the provided permission by the configured permission system
     *
     * @param sender     A command sender, could be a player or the console, who needs a permissions check
     * @param permission The permission type to check
     * @return true if the player is NOT permitted, otherwise false
     */
    protected boolean isNotPermitted(CommandSender sender, Permission permission) {
        return !isPermitted(sender, permission.getPermission());
    }

    /**
     * Get a formatted string that is relative (Includes ago/from now)
     * @param epoch The system time to calculate to
     * @return a formatted relative string for how far away the given time is
     */
    protected String getTimeDifference(Long epoch) {
        return plugin.prettyTime.format(plugin.reduceDurationList(plugin.prettyTime.calculatePreciseDuration(new Date(epoch))));
    }

    /**
     * Get a relative time string but without the relative time language at the end (e.g. no from now/ago)
     *
     * @param epoch The system time to calculate to
     * @return a raw relative string for how far away the given time is
     */
    protected String getTimeDuration(Long epoch) {
        return plugin.prettyTime.formatDuration(plugin.reduceDurationList(plugin.prettyTime.calculatePreciseDuration(new Date(epoch))));
    }

    /**
     * Get an AFKPlus Player class instance from a given offline player
     * @param op The player that the class should represent
     * @return an AFKPlus Player class for the provided player
     */
    protected AFKPlusPlayer getPlayer(OfflinePlayer op) {
        return plugin.getPlayer(op);
    }

}
