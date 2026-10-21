package io.github.pilzi.discord;

import io.github.pilzi.service.services.PollService;
import net.dv8tion.jda.api.entities.Guild;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class BotScheduler {
    public static final int MAX_GUILD_UPDATES_PER_SECOND = 30;
    public static final int MAX_GUILD_UPDATES_PER_SECOND_LIMITED = 10;
    @NonNull
    private final Set<Guild> guildsToSendActivePoll = new HashSet<>();
    @NonNull
    private final Set<Guild> guildsToUpdateAgentsPoll = new HashSet<>();
    @NonNull
    private final PollService pollService;

    public BotScheduler(@NonNull PollService pollService) {
        this.pollService = pollService;
    }

    public void updateActivePollForGuilds() {
        List<Guild> guildsToUpdate = new ArrayList<>();

        guildsToSendActivePoll.stream()
                .limit(MAX_GUILD_UPDATES_PER_SECOND)
                .forEach(guildsToUpdate::add);

        pollService.handleScheduledPollForAllGuilds(guildsToUpdate).forEach(CompletableFuture::join);
        guildsToUpdate.forEach(guildsToSendActivePoll::remove);

    }

    public void updateAgentPollForGuilds() {
        Set<Guild> guildsToUpdate = new HashSet<>();

        int amountOfGuildsToUpdate = guildsToSendActivePoll.isEmpty() ? MAX_GUILD_UPDATES_PER_SECOND : MAX_GUILD_UPDATES_PER_SECOND_LIMITED;

        guildsToUpdateAgentsPoll.stream()
                .limit(amountOfGuildsToUpdate)
                .forEach(guildsToUpdate::add);
        guildsToUpdate.forEach(guildsToUpdateAgentsPoll::remove);

        pollService.updateAgentPollForGuilds(guildsToUpdate).forEach(CompletableFuture::join);

    }

    public void addGuildToUpdateAgentPoll(@NonNull Guild guild) {
        guildsToUpdateAgentsPoll.add(guild);
    }

    public void resetGuildsToSendActivePollTo(@NonNull List<Guild> guilds) {
        guildsToSendActivePoll.clear();
        guildsToSendActivePoll.addAll(guilds);
    }
}
