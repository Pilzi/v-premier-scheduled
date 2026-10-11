package io.github.pilzi.discord.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

import static io.github.pilzi.discord.Bot.DOTENV;
import static io.github.pilzi.discord.Bot.POLL_CHANNEL_NAME_ENV;

public class GuildUtil {

    public static @NonNull Optional<TextChannel> findChannelInGuild(@NonNull Guild guild) {
        return guild.getTextChannels().stream()
                .filter(channel -> channel.getName().equals(DOTENV.get(POLL_CHANNEL_NAME_ENV)))
                .findFirst();
    }

}
