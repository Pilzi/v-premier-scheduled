package io.github.pilzi.database.workers;

import io.github.pilzi.discord.enums.Role;
import org.hibernate.Session;
import org.jspecify.annotations.NonNull;

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
}
