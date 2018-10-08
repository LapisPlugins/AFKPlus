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

package net.lapismc.afkplus;

import net.lapismc.afkplus.playerdata.Permission;
import net.lapismc.lapiscore.LapisCorePermissions;

@SuppressWarnings("WeakerAccess")
public class AFKPlusPermissions extends LapisCorePermissions {

    AFKPlusPermissions(AFKPlus plugin) {
        super(plugin);
        registerAFKPlusPermissions();
        loadPermissions();
    }

    private void registerAFKPlusPermissions() {
        for (Permission permission : Permission.values()) {
            registerPermissions(permission.getPermission());
        }
    }
}
