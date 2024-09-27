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

package net.lapismc.afkplus.commands.tabcomplete;

import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.playerdata.Permission;
import net.lapismc.lapiscore.commands.tabcomplete.LapisTabOption;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class OtherAFKPlusOptions implements LapisTabOption {

    @Override
    public List<String> getOptions(CommandSender sender) {
        List<String> options = new ArrayList<>();
        if (AFKPlus.getInstance().perms.isPermitted(((Player) sender).getUniqueId(), Permission.CanUpdate.getPermission()))
            options.add("update");
        options.add("help");
        return options;
    }

    @Override
    public List<LapisTabOption> getChildren(CommandSender sender) {
        return List.of();
    }
}
