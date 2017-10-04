/*
 * Copyright 2017 Benjamin Martin
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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class AFKPlusPerms {

    HashMap<Permission, HashMap<Perm, Integer>> pluginPerms = new HashMap<>();
    private AFKPlus plugin;
    private HashMap<UUID, Permission> playerPerms = new HashMap<>();

    AFKPlusPerms(AFKPlus p) {
        plugin = p;
        loadPermissions();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> playerPerms = new HashMap<>(), 20 * 60 * 1, 20 * 60 * 1);
    }

    void loadPermissions() {
        pluginPerms.clear();
        ConfigurationSection permsSection = plugin.getConfig().getConfigurationSection("Permissions");
        Set<String> perms = permsSection.getKeys(false);
        for (String perm : perms) {
            String permName = perm.replace(",", ".");
            int Default = plugin.getConfig().getInt("Permissions." + perm + ".Default");
            int priority = plugin.getConfig().getInt("Permissions." + perm + ".Priority");
            int Admin = plugin.getConfig().getInt("Permissions." + perm + ".Admin");
            int UseCommand = plugin.getConfig().getInt("Permissions." + perm + ".UseCommand");
            int TimeToAFK = plugin.getConfig().getInt("Permissions." + perm + ".TimeToAFK");
            int TimeToWarn = plugin.getConfig().getInt("Permissions." + perm + ".TimeToWarn");
            int TimeToAction = plugin.getConfig().getInt("Permissions." + perm + ".TimeToAction");
            HashMap<Perm, Integer> permMap = new HashMap<>();
            permMap.put(Perm.Default, Default);
            permMap.put(Perm.Priority, priority);
            permMap.put(Perm.Admin, Admin);
            permMap.put(Perm.UseCommand, UseCommand);
            permMap.put(Perm.TimeToAFK, TimeToAFK);
            permMap.put(Perm.TimeToWarn, TimeToWarn);
            permMap.put(Perm.TimeToAction, TimeToAction);
            PermissionDefault permissionDefault;
            switch (Default) {
                case 1:
                    permissionDefault = PermissionDefault.TRUE;
                    break;
                case 2:
                    permissionDefault = PermissionDefault.OP;
                    break;

                case 0:
                default:
                    permissionDefault = PermissionDefault.FALSE;
                    break;
            }
            Permission permission = new Permission(permName, permissionDefault);
            if (Bukkit.getPluginManager().getPermission(permName) == null) {
                Bukkit.getPluginManager().addPermission(permission);
            } else {
                plugin.logger.severe("Couldn't add permission " + permName + " as it already exists!");
            }
            pluginPerms.put(permission, permMap);
        }
    }

    void setPerms(UUID uuid, Permission p) {
        playerPerms.put(uuid, p);
        plugin.AFKConfig.setPlayerPermission(uuid, p.getName());
    }

    public Permission getPlayerPermission(UUID uuid) {
        Permission p = null;
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        String permFromFile = plugin.AFKConfig.getPlayerPermission(uuid);
        if (op.isOnline()) {
            Player player = op.getPlayer();
            if (!playerPerms.containsKey(uuid) || playerPerms.get(uuid) == null) {
                Integer priority = -1;
                for (Permission perm : pluginPerms.keySet()) {
                    if (player.hasPermission(perm) &&
                            (pluginPerms.get(perm).get(Perm.Priority) > priority)) {
                        p = perm;
                        priority = pluginPerms.get(perm).get(Perm.Priority);
                    }
                }
                if (p == null) {
                    return null;
                } else {
                    playerPerms.put(uuid, p);
                }
            } else {
                p = playerPerms.get(uuid);
            }
            return p;
        } else {
            for (Permission perm : pluginPerms.keySet()) {
                if (perm.getName().equals(permFromFile)) {
                    return perm;
                }
            }
            return null;
        }
    }

    public Boolean isPermitted(UUID uuid, Perm perm) {
        HashMap<Perm, Integer> permMap;
        Permission p = getPlayerPermission(uuid);
        if (!pluginPerms.containsKey(p) || pluginPerms.get(p) == null) {
            loadPermissions();
            permMap = pluginPerms.get(p);
        } else {
            permMap = pluginPerms.get(p);
        }
        return permMap.get(perm) != null && permMap.get(perm) == 1;
    }

    public Integer getPermissionValue(UUID uuid, Perm p) {
        Permission perm = getPlayerPermission(uuid);
        if (perm == null || pluginPerms.get(perm) == null) {
            loadPermissions();
        }
        return pluginPerms.get(perm).get(p);
    }

    public enum Perm {
        Default, Priority, Admin, UseCommand, TimeToAFK, TimeToWarn, TimeToAction;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

}
