/*
 * Copyright 2023 Benjamin Martin
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

package net.lapismc.afkplus.playerdata;


import net.lapismc.lapiscore.permissions.LapisPermission;

/**
 * This class is used to store the LapisPermissions that this plugin can access
 */
public enum Permission {

    /**
     * Allows a player to set their own AFK status
     */
    AFKSelf(new AFKSelf()),
    /**
     * Allows a player to set the AFK status of other players
     */
    AFKOthers(new AFKOthers()),
    /**
     * Allows a player to enable a fake AFK state in an attempt to avoid user interactions
     * Suggested in issue #27 AFK+ Mod-Relief suggestion
     */
    FakeAFK(new FakeAFK()),
    /**
     * The time in seconds of inactivity required to set a player AFK
     * Setting this to -1 disables automatic AFK setting
     */
    TimeToAFK(new TimeToAFK()),
    /**
     * The time in seconds that a player must be AFK before being warned of action
     * Set to -1 to disable
     */
    TimeToWarning(new TimeToWarning()),
    /**
     * The time in seconds that a player must be AFK before being acted upon
     * Action disabled if set to -1
     */
    TimeToAction(new TimeToAction()),
    /**
     * Allows a player to update the plugin using /afkplus update
     */
    CanUpdate(new Update());

    private final LapisPermission permission;

    Permission(LapisPermission permission) {
        this.permission = permission;
    }

    /**
     * Get the permission class for this enum item
     *
     * @return A LapisPermission class for this permission item
     */
    public LapisPermission getPermission() {
        return this.permission;
    }

    private static class AFKSelf extends LapisPermission {
        AFKSelf() {
            super("AFKSelf", 1);
        }
    }

    private static class AFKOthers extends LapisPermission {
        AFKOthers() {
            super("AFKOthers", 0);
        }
    }

    private static class FakeAFK extends LapisPermission {
        public FakeAFK() {
            super("FakeAFK", 0);
        }
    }

    private static class TimeToAFK extends LapisPermission {
        TimeToAFK() {
            super("TimeToAFK", 30);
        }
    }

    private static class TimeToWarning extends LapisPermission {
        TimeToWarning() {
            super("TimeToWarning", 90);
        }
    }

    private static class TimeToAction extends LapisPermission {
        TimeToAction() {
            super("TimeToAction", 120);
        }
    }

    private static class Update extends LapisPermission {
        Update() {
            super("CanUpdate", 0);
        }
    }
}
