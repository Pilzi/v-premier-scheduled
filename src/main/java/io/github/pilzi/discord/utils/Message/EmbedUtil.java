package io.github.pilzi.discord.utils.Message;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class EmbedUtil {
    @NonNull
    public static final String EMBED_RED = "#FF4655";
    @NonNull
    public static final String USER_NOT_FOUND_FALLBACK = "Unknown user";
    @NonNull
    public static final String DISCORD_EMOJI_PREFIX = "<";

    public static @NonNull EmbedBuilder createUserAgentsDisplayEmbedBuilder(@NonNull Map<Long /* discord user id */, List<String /* agent name */>> userForAgents,
                                                                             @NonNull Map<Long, String> memberNames) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        StringBuilder description = new StringBuilder();

        userForAgents.forEach((discordUserId, agents) -> {
            String name = memberNames.getOrDefault(discordUserId, USER_NOT_FOUND_FALLBACK);
            List<String> agentsWithName = agents.stream().filter(agent -> !agent.startsWith(DISCORD_EMOJI_PREFIX)).toList();
            List<String> agentsWithEmoji = agents.stream().filter(agent -> agent.startsWith(DISCORD_EMOJI_PREFIX)).toList();

            description.append("## ").append(name);

            if (!agentsWithEmoji.isEmpty()) {
                description.append("\n").append("## ").append(String.join(" ", agentsWithEmoji));
            }
            if (!agentsWithName.isEmpty()) {
                description.append("\n").append(String.join(", ", agentsWithName));
            }

            description.append("\n");
        });

        embedBuilder.setDescription(description.toString());
        embedBuilder.setColor(Color.decode(EMBED_RED));
        return embedBuilder;
    }
}
