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

import net.lapismc.lapiscore.events.LapisCoreCancellableEvent;

/**
 * An event for all command based events to extend
 * This includes AFKStart, AFKStop, AFKWarn and AFKAction events
 */
public class AFKCommandEvent extends LapisCoreCancellableEvent {

    private String command;

    /**
     * @param command The command to be run when this event finishes, use an empty string for no command
     */
    public AFKCommandEvent(String command) {
        this.command = command;
    }

    /**
     * Get the command that will be run when this event finishes
     * will return an empty string if no command is to be run
     *
     * @return a string representing the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Set the command to be run when this event finishes
     *
     * @param command The command to be run (excluding the leading slash), set to an empty string to run no command
     */
    public void setCommand(String command) {
        this.command = command;
    }

}
