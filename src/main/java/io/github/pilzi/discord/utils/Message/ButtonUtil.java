package io.github.pilzi.discord.utils.Message;

import io.github.pilzi.discord.utils.LanguageUtil;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import org.jspecify.annotations.NonNull;


public class ButtonUtil {
    @NonNull
    public static final String AGENT_SELECTION_BUTTON_ID = "agent-selection-button";

    public static @NonNull ActionRow getAgentSelectionButton() {
        return ActionRow.of(Button.primary(AGENT_SELECTION_BUTTON_ID, LanguageUtil.getSelectAgentsText()));
    }
}
