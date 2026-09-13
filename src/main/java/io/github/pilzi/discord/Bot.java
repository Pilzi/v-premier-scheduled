package io.github.pilzi.discord;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.pilzi.discord.listener.BotListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jspecify.annotations.NonNull;

import java.util.EnumSet;

public class Bot {
    @NonNull
    public static final String HIBERNATE_CFG_XML = "hibernate.cfg.xml";
    @NonNull
    public static final String DISCORD_API_KEY_ENV = "DISCORD_API_KEY";
    @NonNull
    public static final String POLL_CHANNEL_NAME_ENV = "POLL_CHANNEL_NAME";
    @NonNull
    public static final Dotenv DOTENV = Dotenv.load();

    static void main() {
        JDABuilder.createLight(DOTENV.get(DISCORD_API_KEY_ENV), EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_POLLS))
                .addEventListeners(new BotListener())
                .build();
    }
}
