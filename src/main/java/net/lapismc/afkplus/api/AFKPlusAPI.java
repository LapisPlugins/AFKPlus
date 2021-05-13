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

package net.lapismc.afkplus.api;

import net.lapismc.afkplus.AFKPlus;

/**
 * This class is for extensions to get the main class of AFKPlus
 */
@SuppressWarnings("unused")
public class AFKPlusAPI {

    private static AFKPlus plugin;

    /**
     * This initializer is used by AFKPlus to give access to the main class
     * Don't use this!
     *
     * @param plugin The AFKPlus main class
     */
    public AFKPlusAPI(AFKPlus plugin) {
        AFKPlusAPI.plugin = plugin;
    }

    /**
     * Use this initializer
     */
    public AFKPlusAPI() {
    }

    /**
     * Get the main class
     *
     * @return Returns the main class of AFKPlus for extensions to use
     */
    public AFKPlus getPlugin() {
        return plugin;
    }

}
