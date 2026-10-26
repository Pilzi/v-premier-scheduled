package io.github.pilzi.database.workers;

import io.github.pilzi.database.domain.AgentVoteEntity;
import io.github.pilzi.discord.enums.Role;
import org.hibernate.Session;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class AgentVoteWorker {

    public void deleteVoteByRole(@NonNull Session session,
                                 @NonNull Role agentRole,
                                 @NonNull Long discordUserId) {
        session.createQuery("DELETE" +
                        " FROM AgentVoteEntity vote" +
                        " WHERE vote.agentRole = :agentRole" +
                        " AND vote.discordUserId = :discordUserId")
                .setParameter("agentRole", agentRole)
                .setParameter("discordUserId", discordUserId)
                .executeUpdate();
    }

    @NonNull
    public List<AgentVoteEntity> getAllVotes(@NonNull Session session,
                                             @NonNull Long activePollId) {
        return session.createQuery("SELECT vote" +
                        " FROM AgentVoteEntity vote" +
                        " WHERE vote.activePoll.id = :activePollId", AgentVoteEntity.class)
                .setParameter("activePollId", activePollId)
                .list();
    }

    @NonNull
    public List<AgentVoteEntity> getVotesForUser(@NonNull Session session,
                                                 @NonNull Long activePollId,
                                                 @NonNull Long discordUserId) {
        return session.createQuery("SELECT vote" +
                        " FROM AgentVoteEntity vote" +
                        " WHERE vote.activePoll.id = :activePollId" +
                        " AND vote.discordUserId = :discordUserId", AgentVoteEntity.class)
                .setParameter("activePollId", activePollId)
                .setParameter("discordUserId", discordUserId)
                .list();
    }

    public void deleteVotesForGuild(@NonNull Session session,
                                    @NonNull Long guildId) {
        session.createQuery("DELETE" +
                        " FROM AgentVoteEntity vote" +
                        " WHERE vote.activePoll.guild.discordId = :guildId")
                .setParameter("guildId", guildId)
                .executeUpdate();
    }
}
