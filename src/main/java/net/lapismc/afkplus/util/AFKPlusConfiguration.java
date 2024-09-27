/*
 * Copyright 2024 Benjamin Martin
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
import net.lapismc.lapiscore.LapisCoreConfiguration;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.units.JustNow;
import org.ocpsoft.prettytime.units.Millisecond;

import java.util.Locale;

/**
 * This extension to the LapisCoreConfiguration class is used to reinitialize PrettyTime when the locale has been changed
 */
public class AFKPlusConfiguration extends LapisCoreConfiguration {

    private final AFKPlus plugin;

    public AFKPlusConfiguration(AFKPlus plugin, int configVersion, int messagesVersion) {
        super(plugin, configVersion, messagesVersion);
        this.plugin = plugin;
    }

    @Override
    public void reloadMessages() {
        //Run the super reload to actually reload the messages file
        super.reloadMessages();
        //This is so that this code doesn't run on first load
        //The plugin crashes without this
        if (plugin == null)
            return;
        //Get both the config and pretty time locales and compare them
        Locale configLocale = new Locale(getMessage("PrettyTimeLocale"));
        Locale prettyTimeLocale = plugin.prettyTime.getLocale();
        //If they don't match we need to reinit pretty time
        if (!configLocale.equals(prettyTimeLocale)) {
            plugin.prettyTime = new PrettyTime(configLocale);
            plugin.prettyTime.removeUnit(JustNow.class);
            plugin.prettyTime.removeUnit(Millisecond.class);
        }
    }
}
