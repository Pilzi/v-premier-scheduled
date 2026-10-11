package io.github.pilzi.discord.utils.Message;

import io.github.pilzi.discord.enums.Agent;
import io.github.pilzi.discord.enums.Role;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class DropdownUtil {
    @NonNull
    private static final String AGENT_SELECTION_CUSTOM_ID_PREFIX = "agent-selection-";
    @NonNull
    private static final String AGENT_SELECTION_PLACEHOLDER = "Choose your %s agents";
    public static final int MIN_VALUES = 0;

    /**
     * Build multiple rows to be able to select agents of any {@link Role}
     * @param defaultsForRole The menu is set user specific when the user clicks it again his selections should be loaded as defaults
     * @return list of {@link StringSelectMenu}
     */
    private static List<StringSelectMenu> buildAgentSelectMenus(@NonNull Map<Role, List<String>> defaultsForRole) {
        List<StringSelectMenu> menus = new ArrayList<>();

        for (Role role : Role.values()) {
            List<String> defaults = defaultsForRole.getOrDefault(role, Collections.emptyList());

            List<Agent> agentsForRole = Arrays.stream(Agent.values())
                    .filter(agent -> agent.getRole() == role)
                    .toList();

            StringSelectMenu.Builder menuBuilder = StringSelectMenu
                    .create(AGENT_SELECTION_CUSTOM_ID_PREFIX + role.name().toLowerCase())
                    .setPlaceholder(String.format(AGENT_SELECTION_PLACEHOLDER, role.getDisplayName().toLowerCase()))
                    .setMinValues(MIN_VALUES)
                    .setDefaultValues(defaults)
                    .setMaxValues(agentsForRole.size());

            for (Agent agent : agentsForRole) {
                menuBuilder.addOption(agent.getDisplayName(), agent.name().toLowerCase());
            }

            menus.add(menuBuilder.setDefaultValues(defaults).build());
        }

        return menus;
    }

    public static List<ActionRow> buildAgendActionRows(@NonNull Map<Role, List<String>> defaultsForRole) {
        return buildAgentSelectMenus(defaultsForRole).stream()
                .map(ActionRow::of)
                .toList();
    }
}
