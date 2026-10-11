package io.github.pilzi.database.domain;

import io.github.pilzi.discord.enums.Role;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "agent_vote")
public class AgentVoteEntity {
    @Nullable
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @NonNull
    @Column(name = "discord_user_id", nullable = false)
    private Long discordUserId;

    @NonNull
    @Column(name = "agent_name", nullable = false, length = 20)
    private String agentName;

    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "agent_role", nullable = false)
    private Role agentRole;

    @NonNull
    @ManyToOne()
    @JoinColumn(name = "active_poll_id", referencedColumnName = "id", nullable = false)
    private ActivePollEntity activePoll;

    public AgentVoteEntity(@NonNull Long discordUserId,
                           @NonNull String agentName,
                           @NonNull Role agentRole,
                           @NonNull ActivePollEntity activePoll) {
        this.discordUserId = discordUserId;
        this.agentName = agentName;
        this.agentRole = agentRole;
        this.activePoll = activePoll;
    }

    protected AgentVoteEntity() {}

    public @NonNull Long getDiscordUserId() {
        return discordUserId;
    }

    public void setDiscordUserId(@NonNull Long discordUserId) {
        this.discordUserId = discordUserId;
    }

    public @NonNull String getAgentName() {
        return agentName;
    }

    public void setAgentName(@NonNull String agentName) {
        this.agentName = agentName;
    }

    public @NonNull Role getAgentRole() {
        return agentRole;
    }

    public void setAgentRole(@NonNull Role agentRole) {
        this.agentRole = agentRole;
    }

    public @NonNull ActivePollEntity getActivePoll() {
        return activePoll;
    }

    public void setActivePoll(@NonNull ActivePollEntity activePoll) {
        this.activePoll = activePoll;
    }
}
