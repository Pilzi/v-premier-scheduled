package io.github.pilzi.discord.utils.Message;

import io.github.pilzi.database.domain.EventEntity;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public class PollUtil {

    public static final String PRACTICE_DAY_TEXT = "Practice";
    public static final String MATCH_DAY_TEXT = "Match";
    public static final String DAY_TYPE_TIME_SEPARATOR = ": ";

    @NonNull
    public static MessagePollData buildEventPoll(@NonNull String title,
                                                 @NonNull List<EventEntity> eventsInCurrentWeek) {
        MessagePollBuilder pollBuilder = MessagePollData.builder(title);

        eventsInCurrentWeek.stream()
                .sorted(Comparator.comparing(EventEntity::getStartAt))
                .forEach(event -> {
                    String text = (event.isPractice() ? PRACTICE_DAY_TEXT : MATCH_DAY_TEXT)
                            + DAY_TYPE_TIME_SEPARATOR
                            + event.getStartAt();
                    pollBuilder.addAnswer(text.substring(0, Math.min(text.length(), 80)));
                });

    return pollBuilder
            .setMultiAnswer(true)
            .setDuration(Duration.ofHours(72))
            .build();
    }

}
