/*
 * Copyright 2025 Benjamin Martin
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

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import net.lapismc.lapiscore.utils.LapisCoreDiscordSRVHook;

import java.awt.*;

public class AFKPlusDiscordSRVHook extends LapisCoreDiscordSRVHook {

    private final AFKPlus plugin;

    public AFKPlusDiscordSRVHook(AFKPlus plugin) {
        this.plugin = plugin;
    }

    public void sendAFKEmbed(AFKPlusPlayer player, boolean isStartingAFK, String timeAFK) {
        String channelName = plugin.getConfig().getString("Broadcast.DiscordSRV.Channel", "global");
        String avatarURL = plugin.getConfig().getString("Broadcast.DiscordSRV.AvatarURL", "https://api.mineatar.io/face/%UUID%?scale=16");
        TextChannel globalChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName);
        String startMessage = plugin.config.getMessage("Broadcast.DiscordSRV.Start").replace("{PLAYER}", player.getName());
        String stopMessage = plugin.config.getMessage("Broadcast.DiscordSRV.Stop").replace("{PLAYER}", player.getName());
        String title = isStartingAFK ? startMessage : stopMessage;
        avatarURL = avatarURL.replace("%UUID%", player.getUUID().toString().replace("-", ""));
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(title, null, avatarURL)
                .setColor(isStartingAFK ? Color.GREEN : Color.RED);
        if (!isStartingAFK && timeAFK != null) {
            embedBuilder.addField(plugin.config.getMessage("Broadcast.DiscordSRV.TimeAFK"), timeAFK, true);
        }
        globalChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }


}
