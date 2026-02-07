/*
 * Copyright 2026 Benjamin Martin
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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

/**
 * This class allows Spigot servers to not execute this class while paper servers do,
 * ensuring compatibility
 */
public class PlayerHoverMessage {

    /**
     * Send a message to a player with an additional message when the player hovers their mouse over the message
     *
     * @param p         The player to send the message to
     * @param message   The displayed text
     * @param hoverText The text to show when they hover their mouse over the message
     */
    public PlayerHoverMessage(Player p, String message, String hoverText) {
        TextComponent hoverableText = Component.text(message)
                .hoverEvent(HoverEvent.showText(Component.text(hoverText)));
        p.sendMessage(hoverableText);
    }

}
