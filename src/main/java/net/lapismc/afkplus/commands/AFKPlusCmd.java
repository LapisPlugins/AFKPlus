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

package net.lapismc.afkplus.commands;

import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.util.AFKPlusCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class AFKPlusCmd extends AFKPlusCommand {

    public AFKPlusCmd(AFKPlus plugin) {
        super(plugin, "afkplus", "Shows plugin, player and help information", new ArrayList<>());
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        //afkplus
        if (args.length == 0) {
            //TODO show command info
        } else if (args.length == 1) {
            // /afkplus update
            if (args[0].equalsIgnoreCase("update")) {
                //TODO run updater
                // /afkplus help
            } else {
                //TODO send help
            }
            // /afkplus player
        } else if (args[0].equalsIgnoreCase("player")) {
            playerCommand(sender, args);
        } else {
            //TODO send help
        }
    }

    private void playerCommand(CommandSender sender, String[] args) {

    }
}
