/*
 * Copyright 2022 Benjamin Martin
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
import net.lapismc.lapiscore.utils.luckperms.LapisCoreContextCalculator;
import net.lapismc.lapiscore.utils.luckperms.LapisCoreContexts;
import net.luckperms.api.context.ContextConsumer;
import org.bukkit.entity.Player;

/**
 * This class adds a LuckPerms context for when a player is or is not AFK
 * <a href="https://luckperms.net/wiki/Context">LuckPerms Wiki</a>
 */
public class AFKPlusContext extends LapisCoreContextCalculator<Player> {

    AFKPlus plugin = (AFKPlus) super.plugin;

    public AFKPlusContext() {
        LapisCoreContexts luckPermsContexts = new LapisCoreContexts(plugin);
        luckPermsContexts.registerContext(this);
        plugin.tasks.addShutdownTask(luckPermsContexts::unregisterAll);
    }

    @Override
    public void calculate(Player target, ContextConsumer consumer) {
        AFKPlusPlayer p = plugin.getPlayer(target);
        boolean playerAFKState = p.isAFK();
        consumer.accept("afkstate", playerAFKState ? "true" : "false");
    }
}
