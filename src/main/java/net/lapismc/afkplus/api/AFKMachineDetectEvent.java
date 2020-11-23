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

package net.lapismc.afkplus.api;

import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import net.lapismc.lapiscore.events.LapisCoreEvent;

/**
 * An event to notify plugins when a player is deemed to be avoiding afk
 * This can be fired by normal activity so don't do anything drastic with just one trigger
 */
public class AFKMachineDetectEvent extends LapisCoreEvent {

    private final AFKPlusPlayer player;

    public AFKMachineDetectEvent(AFKPlusPlayer player) {
        this.player = player;
    }

    public AFKPlusPlayer getPlayer() {
        return player;
    }

}
