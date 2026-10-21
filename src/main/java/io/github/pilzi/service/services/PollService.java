package io.github.pilzi.service.services;

import io.github.pilzi.discord.enums.Role;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface PollService {

    /**
     * Sends the poll to the Discord channel for all given guilds.
     *
     * The poll is only sent if there are events scheduled for the current week
     * and no valid poll for this week already exists.
     *
     * @param guilds the guilds to send the poll to
     */
    @NonNull
    List<? extends CompletableFuture<?>> handleScheduledPollForAllGuilds(@NonNull List<Guild> guilds);

    /**
     * Save the agent selection made by a specific user in database for the active poll the user interacted with.
     */
    void importSelectedAgents(@NonNull Long guildId,
                              @NonNull Long messageId,
                              @NonNull Long discordUserId,
                              @NonNull List<String> selectedAgents,
                              @NonNull Role role);


    /**
     * Update the poll with data in {@link io.github.pilzi.database.domain.AgentVoteEntity}
     *
     * @param guilds set of guilds to update active poll
     * @return list of update events to schedule
     */
    @NonNull
    List<? extends CompletableFuture<?>> updateAgentPollForGuilds(@NonNull Set<Guild> guilds);

    void replyToSelectAgentsButtonEventWithAgentPoll(@NonNull ButtonInteractionEvent event);
}
