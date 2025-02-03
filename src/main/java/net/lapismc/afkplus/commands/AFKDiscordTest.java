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

package net.lapismc.afkplus.commands;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.util.AFKPlusCommand;
import org.bukkit.command.CommandSender;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

//TODO: Delete this when testing is complete
public class AFKDiscordTest extends AFKPlusCommand {

    public AFKDiscordTest(AFKPlus plugin) {
        super(plugin, "AFKDiscordTest", "A command to send test messages via DiscordSRV", new ArrayList<>(Collections.singleton("AFKD")));
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        TextChannel globalChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("global");

        MessageEmbed embedStart = new EmbedBuilder()
                .setAuthor("Player name is AFK", null, "https://api.mineatar.io/face/cc22782090604ebda00d511e4815f125?scale=16")
                .setColor(Color.GREEN)
                .build();

        MessageEmbed embedEnd = new EmbedBuilder()
                .setAuthor("Player name is no longer AFK", null, "https://api.mineatar.io/face/cc22782090604ebda00d511e4815f125?scale=16")
                .addField("They were AFK for:", "1 minute and 30 seconds", true)
                .setColor(Color.RED).build();

        globalChannel.sendMessageEmbeds(embedStart).queue();
        globalChannel.sendMessageEmbeds(embedEnd).queue();
    }
}
