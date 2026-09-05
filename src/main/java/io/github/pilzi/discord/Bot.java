package io.github.pilzi.discord;

import io.github.pilzi.discord.listener.BotListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;

public class Bot {
    static void main() {
        JDABuilder.createLight("<Token>", EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
                .addEventListeners(new BotListener())
                .build();
    }
}
