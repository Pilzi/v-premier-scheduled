package io.github.pilzi.discord.utils.Message;

import io.github.pilzi.discord.enums.Agent;
import io.github.pilzi.discord.enums.Role;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DropdownUtil {
    @NonNull
    private static final String AGENT_SELECTION_CUSTOM_ID_PREFIX = "agent-selection-";
    @NonNull
    private static final String AGENT_SELECTION_PLACEHOLDER = "Choose your %s agents";
    public static final int MIN_VALUES = 0;

    private static List<StringSelectMenu> buildAgendSelectMenus() {
        List<StringSelectMenu> menus = new ArrayList<>();

        for (Role role : Role.values()) {
            List<Agent> agentsForRole = Arrays.stream(Agent.values())
                    .filter(agent -> agent.getRole() == role)
                    .toList();

            StringSelectMenu.Builder menuBuilder = StringSelectMenu
                    .create(AGENT_SELECTION_CUSTOM_ID_PREFIX + role.name().toLowerCase())
                    .setPlaceholder(String.format(AGENT_SELECTION_PLACEHOLDER, role.getDisplayName().toLowerCase()))
                    .setMinValues(MIN_VALUES)
                    .setMaxValues(agentsForRole.size());

            for (Agent agent : agentsForRole) {
                menuBuilder.addOption(agent.getDisplayName(), agent.name().toLowerCase());
            }

            menus.add(menuBuilder.build());
        }

        return menus;
    }

    public static List<ActionRow> buildAgendActionRows() {
        return buildAgendSelectMenus().stream()
                .map(ActionRow::of)
                .toList();
    }
}
