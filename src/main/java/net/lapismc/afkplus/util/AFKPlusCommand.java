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
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import net.lapismc.afkplus.playerdata.Permission;
import net.lapismc.lapiscore.LapisCoreCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.ocpsoft.prettytime.Duration;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.units.JustNow;
import org.ocpsoft.prettytime.units.Millisecond;

import java.util.*;

public abstract class AFKPlusCommand extends LapisCoreCommand {

    private final PrettyTime prettyTime;
    protected AFKPlus plugin;

    public AFKPlusCommand(AFKPlus plugin, String name, String desc, ArrayList<String> aliases) {
        super(plugin, name, desc, aliases);
        this.plugin = plugin;
        Locale loc = new Locale(plugin.config.getMessage("PrettyTimeLocale"));
        prettyTime = new PrettyTime(loc);
        prettyTime.removeUnit(JustNow.class);
        prettyTime.removeUnit(Millisecond.class);
    }

    protected boolean isNotPermitted(CommandSender sender, Permission permission) {
        return !isPermitted(sender, permission.getPermission());
    }

    protected String getTimeDifference(Long epoch) {
        return prettyTime.format(reduceDurationList(prettyTime.calculatePreciseDuration(new Date(epoch))));
    }

    private List<Duration> reduceDurationList(List<Duration> durationList) {
        while (durationList.size() > 2) {
            Duration smallest = null;
            for (Duration current : durationList) {
                if (smallest == null || smallest.getUnit().getMillisPerUnit() > current.getUnit().getMillisPerUnit()) {
                    smallest = current;
                }
            }
            durationList.remove(smallest);
        }
        return durationList;
    }

    protected AFKPlusPlayer getPlayer(UUID uuid) {
        return plugin.getPlayer(uuid);
    }

    protected AFKPlusPlayer getPlayer(OfflinePlayer op) {
        return plugin.getPlayer(op);
    }

}
