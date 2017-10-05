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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class AFKPlusFileWatcher implements Runnable {
    private AFKPlus plugin;

    AFKPlusFileWatcher(AFKPlus p) {
        plugin = p;
    }

    @Override
    public void run() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get(plugin.getDataFolder().getAbsolutePath());
            dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            plugin.logger.info("AFK+'s file watcher started!");
            WatchKey key = watcher.take();
            while (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();
                    File f = fileName.toFile();
                    if (kind == ENTRY_DELETE) {
                        if (f.getName().endsWith(".yml")) {
                            String name = f.getName().replace(".yml", "");
                            switch (name) {
                                case "config":
                                    plugin.saveDefaultConfig();
                                    break;
                                case "Messages":
                                    plugin.AFKConfig.getMessages();
                                    break;
                            }
                        }
                    } else if (kind == ENTRY_MODIFY) {
                        if (f.getName().endsWith(".yml")) {
                            checkConfig(f);
                        }
                    }
                }
                key.reset();
                key = watcher.take();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        plugin.logger.severe("AFK+'s file watcher has stopped, please report any errors to dart2112 if this was not intended");
    }

    private void checkConfig(File f) {
        String name = f.getName().replace(".yml", "");
        switch (name) {
            case "config":
                plugin.reloadConfig();
                plugin.AFKPerms.loadPermissions();
                plugin.logger.info("Changes made to AFK+'s config have been loaded");
                break;
            case "Messages":
                plugin.AFKConfig.reloadMessages(f);
                plugin.logger.info("Changes made to AFK+'s Messages.yml have been loaded");
                break;
        }
    }

}

