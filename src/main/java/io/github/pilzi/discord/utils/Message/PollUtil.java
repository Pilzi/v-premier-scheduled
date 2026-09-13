package io.github.pilzi.discord.utils.Message;

import io.github.pilzi.database.domain.EventEntity;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import org.jspecify.annotations.NonNull;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

public class PollUtil {
    @NonNull
    public static final String PRACTICE_DAY_TEXT = "Practice";
    @NonNull
    public static final String MATCH_DAY_TEXT = "Match";
    @NonNull
    public static final String DAY_TYPE_TIME_SEPARATOR = ": ";
    public static final int DEFAULT_POLL_DURATION_DAYS = 5;
    @NonNull
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM HH:mm");
    @NonNull
    public static final SimpleDateFormat hourFormatter = new SimpleDateFormat("HH:mm");
    @NonNull
    public static final String HOUR_SEPARATOR = " - ";
    @NonNull
    public static final String WEEKDAY_DATE_SEPARATOR = " ";
    @NonNull
    public static MessagePollData buildEventPoll(@NonNull String title,
                                                 @NonNull List<EventEntity> eventsInCurrentWeek) {
        MessagePollBuilder pollBuilder = MessagePollData.builder(title);

        eventsInCurrentWeek.stream()
                .sorted(Comparator.comparing(EventEntity::getStartAt))
                .forEach(event -> {
                    Instant startAt = event.getStartAt();
                    String dayOfWeek = startAt.atZone(ZoneId.systemDefault()).getDayOfWeek().toString();
                    String text = (event.isPractice() ? PRACTICE_DAY_TEXT : MATCH_DAY_TEXT)
                            + DAY_TYPE_TIME_SEPARATOR
                            + dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1).toLowerCase()
                            + WEEKDAY_DATE_SEPARATOR
                            + dateFormatter.format(Date.from(startAt))
                            + HOUR_SEPARATOR
                            + hourFormatter.format(Date.from(event.getEndAt()));
                    pollBuilder.addAnswer(text.substring(0, Math.min(text.length(), 80)));
                });

    return pollBuilder
            .setMultiAnswer(true)
            .setDuration(Duration.ofDays(DEFAULT_POLL_DURATION_DAYS))
            .build();
    }

}
