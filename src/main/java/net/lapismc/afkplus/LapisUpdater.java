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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.logging.Logger;

class LapisUpdater {

    private String ID;
    private String jarName;
    private String username;
    private String repoName;
    private String branch;
    private JavaPlugin plugin;
    private Logger logger;
    private Boolean force;
    private String newVersionRawString;

    LapisUpdater(JavaPlugin plugin, String jarName, String username, String repoName, String branch) {
        this.plugin = plugin;
        this.jarName = jarName;
        this.username = username;
        this.repoName = repoName;
        this.branch = branch;
        this.logger = Bukkit.getLogger();
    }

    boolean checkUpdate() {
        this.ID = "AFKPlus";
        this.force = false;
        return updateCheck();
    }

    void downloadUpdate() {
        this.ID = "AFKPlus";
        this.force = true;
        downloadUpdateJar();
    }

    private void downloadUpdateJar() {
        if (updateCheck()) {
            try {
                URL changeLogURL = new URL(
                        "https://raw.githubusercontent.com/" + username + "/" + repoName + "/" + branch + "/updater" +
                                "/changelog.yml");
                ReadableByteChannel changelogByteChannel = Channels.newChannel(changeLogURL.openStream());
                File changeLogFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator +
                        "changelog.yml");
                URL jarURL = new URL(
                        "https://raw.githubusercontent.com/" + username + "/" + repoName + "/" + branch + "/updater/"
                                + ID + "/" + jarName + ".jar");
                ReadableByteChannel jarByteChannel = Channels.newChannel(jarURL.openStream());
                File update = plugin.getServer().getUpdateFolderFile();
                if (!update.exists()) {
                    if (!update.mkdir()) {
                        logger.severe("Failed to generate " + update.getName());
                    }
                }
                File jar = new File(update.getAbsolutePath() + File.separator + jarName + ".jar");
                if (!jar.exists()) {
                    if (!jar.createNewFile()) {
                        logger.severe("Failed to generate " + jar.getName());
                    }
                }
                FileOutputStream jarOutputStream = new FileOutputStream(jar);
                jarOutputStream.getChannel().transferFrom(jarByteChannel, 0, Long.MAX_VALUE);
                jarByteChannel.close();
                jarOutputStream.flush();
                jarOutputStream.close();

                FileOutputStream changeLogOutputStream = new FileOutputStream(changeLogFile);
                changeLogOutputStream.getChannel().transferFrom(changelogByteChannel, 0, Long.MAX_VALUE);
                changelogByteChannel.close();
                changeLogOutputStream.flush();
                changeLogOutputStream.close();
                YamlConfiguration changeLog = YamlConfiguration.loadConfiguration(changeLogFile);
                logger.info("Changes in newest Version \n" +
                        changeLog.getStringList(newVersionRawString).toString().replace("[", "").replace("]", ""));
            } catch (IOException e) {
                logger.severe("HomeSpawn updater failed to download updates!");
                logger.severe("Please check your internet connection and" +
                        " firewall settings and try again later");
            }
        }
    }

    private boolean updateCheck() {
        Integer oldVersion;
        Integer newVersion;
        File f;
        YamlConfiguration yaml;
        try {
            URL website = new URL(
                    "https://raw.githubusercontent.com/" + username + "/" + repoName + "/" + branch + "/updater" +
                            "/update.yml");
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            f = new File(plugin.getDataFolder().getAbsolutePath() + File.separator +
                    "update.yml");
            Date d = new Date(f.lastModified());
            Date d0 = new Date();
            d0.setTime(d0.getTime() - 3600);
            if (!f.exists() || force || d.before(d0)) {
                FileOutputStream fos = new FileOutputStream(f);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                rbc.close();
                fos.flush();
                fos.close();
                if (!f.setLastModified(d0.getTime())) {
                    logger.info("Failed to set modified time for " + f.getName());
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to check for updates!");
            logger.severe("Please check your internet and firewall settings" +
                    " and try again later!");
            return false;
        }
        try {
            yaml = YamlConfiguration.loadConfiguration(f);
            if (!yaml.contains(ID)) {
                return false;
            }
            String oldVersionString = plugin.getDescription().getVersion()
                    .replace(".", "").replace("Beta ", "");
            newVersionRawString = yaml.getString(ID);
            String newVersionString = yaml.getString(ID).replace(".", "")
                    .replace("Beta ", "");
            oldVersion = Integer.parseInt(oldVersionString);
            newVersion = Integer.parseInt(newVersionString);
        } catch (Exception e) {
            logger.severe("Failed to load update.yml or parse the values!" +
                    " It may be corrupt!");
            logger.severe("Please try again later");
            if (!f.delete()) {
                logger.info("Failed to delete " + f.getName());
            }
            return false;
        }
        Boolean update = false;
        if (yaml.getString(ID).contains("Beta") && !plugin.getDescription()
                .getVersion().contains("Beta")) {
            update = true;
        }
        if (!yaml.getString(ID).contains("Beta") && plugin.getDescription()
                .getVersion().contains("Beta")) {
            update = true;
        }
        if (yaml.getString(ID).contains("Beta") && plugin.getDescription()
                .getVersion().contains("Beta")) {
            update = oldVersion < newVersion;
        }
        if (!yaml.getString(ID).contains("Beta") && !plugin.getDescription()
                .getVersion().contains("Beta")) {
            update = oldVersion < newVersion;
        }
        return update;
    }
}
