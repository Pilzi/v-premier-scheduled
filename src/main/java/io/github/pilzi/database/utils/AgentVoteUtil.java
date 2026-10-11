package io.github.pilzi.database.utils;

import io.github.pilzi.database.domain.AgentVoteEntity;
import io.github.pilzi.discord.enums.Role;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentVoteUtil {
    @NonNull
    public static Map<Role, List<String>> collectAgentsForRole(@NonNull List<AgentVoteEntity> agentVotes) {
        Map<Role, List<String>> roleForAgents = new HashMap<>();

        agentVotes.forEach(agentVote -> roleForAgents.computeIfAbsent(agentVote.getAgentRole(), _ -> new ArrayList<>()).add(agentVote.getAgentName()));

        return roleForAgents;
    }

    @NonNull
    public static Map<Long, List<String>> collectAgentsForUser(@NonNull List<AgentVoteEntity> agentVotes) {
        Map<Long /* username */, List<String /* agent name */>> userForAgents = new HashMap<>();

        agentVotes.forEach(agentVote -> {
            userForAgents.computeIfAbsent(agentVote.getDiscordUserId(), a -> new ArrayList<>()).add(agentVote.getAgentName());
        });

        return userForAgents;
    }
}
