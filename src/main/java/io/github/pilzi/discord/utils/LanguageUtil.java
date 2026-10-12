package io.github.pilzi.discord.utils;

import io.github.pilzi.discord.enums.Role;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

import static io.github.pilzi.discord.Bot.DOTENV;

@NullMarked
public class LanguageUtil {
    public static final String ENGLISH_LOCAL_CODE = "en";
    @Nullable
    private static String language = null;
    public static final String LANGUAGE_ENV = "LANGUAGE";
    private static final String[] AGENT_SELECTION_HEADER_TEXTS = {"## ⚔️ Agent Selection\n" + "Which agents would you like to play on %s?", "## ⚔️ Agenten Auswahl\n" + "Mit welchen Agenten möchtest du auf %s spielen? "};
    private static final String[] COUNT_ME_OUT_TEXTS = {"Count me out", "Ich kann nicht"};
    private static final String[] SELECT_AGENTS_TEXTS = {"Select Agents", "Agenten Auswählen"};
    private static final String[] AGENT_SELECTION_PLACEHOLDER_TEXTS = {"Choose your %s agents", "Wählen deinen %s Agenten aus "};
    private static final String[] VOTE_NOT_POSSIBLE_TEXTS = {"Since the week is over, voting is no longer possible", "Da die Woche vorbei ist, ist eine Abstimmung nicht mehr möglich"};
    private static final String[] CLICK_HERE_TO_GO_TO_CURRENT_POLL_TEXTS = {"Click here to go to the current poll", "Hier klicken, um zur aktuellen Umfrage zu gelangen"};
    private static final Locale[] supportedLocales = {Locale.ENGLISH, Locale.GERMAN};

    private static int getLanguageIndex() {
        if (language == null) {
            language = DOTENV.get(LANGUAGE_ENV);
        }

        return Objects.equals(language, ENGLISH_LOCAL_CODE) ? 0 : 1;
    }

    public static String getAgentSelectionText(String map) {
        return String.format(AGENT_SELECTION_HEADER_TEXTS[getLanguageIndex()], map);
    }

    public static String getCountMeOutMessage() {
        return COUNT_ME_OUT_TEXTS[getLanguageIndex()];
    }

    public static String getDayOfWeek(DayOfWeek dayOfWeek) {
        return dayOfWeek.getDisplayName(TextStyle.FULL, supportedLocales[getLanguageIndex()]);
    }

    public static String getAgentSelectionPlaceholder(Role role) {
        return String.format(AGENT_SELECTION_PLACEHOLDER_TEXTS[getLanguageIndex()], role.getDisplayName().toLowerCase());
    }

    public static String getSelectAgentsText() {
        return SELECT_AGENTS_TEXTS[getLanguageIndex()];
    }

    public static String getAgentVoteNotPossibleAnymore() {
        return VOTE_NOT_POSSIBLE_TEXTS[getLanguageIndex()];
    }

    public static String getClickHereToGoToPollText() {
        return CLICK_HERE_TO_GO_TO_CURRENT_POLL_TEXTS[getLanguageIndex()];
    }
}
