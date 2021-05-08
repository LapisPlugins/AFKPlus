/*
 * Copyright 2021 Benjamin Martin
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

import net.ess3.api.IEssentials;
import net.ess3.api.IUser;
import org.bukkit.Bukkit;

import java.util.UUID;

public class EssentialsAFKHook {

    private final IEssentials essentials;

    public EssentialsAFKHook() {
        essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }

    public void setAFK(UUID uuid, boolean state) {
        IUser user = essentials.getUser(uuid);
        user.setAfkMessage("");
        user.setAfk(state, net.ess3.api.events.AfkStatusChangeEvent.Cause.UNKNOWN);
    }


}
