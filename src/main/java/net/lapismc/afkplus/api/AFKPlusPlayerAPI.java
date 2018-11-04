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

package net.lapismc.afkplus.api;

import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public class AFKPlusPlayerAPI {

    private static AFKPlus plugin;

    /**
     * This is for inside use, Please use {@link #AFKPlusPlayerAPI()}
     *
     * @param plugin The AFKPlus main class for static access
     */
    public AFKPlusPlayerAPI(AFKPlus plugin) {
        AFKPlusPlayerAPI.plugin = plugin;
    }

    /**
     * Use this constructor to get access to the player API
     */
    public AFKPlusPlayerAPI() {

    }

    /**
     * Get a players AFKPlus class
     *
     * @param op The player you wish to retrieve
     * @return Returns the {@link AFKPlusPlayer} class for the player given
     */
    public AFKPlusPlayer getPlayer(OfflinePlayer op) {
        return plugin.getPlayer(op);
    }

    /**
     * Get a players AFKPlus class
     *
     * @param uuid The uuid of the player you wish to retrieve
     * @return Returns the {@link AFKPlusPlayer} class for the uuid given
     */
    public AFKPlusPlayer getPlayer(UUID uuid) {
        return plugin.getPlayer(uuid);
    }

}
