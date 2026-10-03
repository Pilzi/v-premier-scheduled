package io.github.pilzi.service.utils;

import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

public class CalendarUtil {
    public static final int MONDAY_INDEX = 1;
    public static Supplier<ZonedDateTime> timeSource = () -> ZonedDateTime.now(ZoneId.systemDefault());

    private static ZonedDateTime getFirstMomentOfCurrentWeekZoned() {
        return timeSource.get()
                .with(ChronoField.DAY_OF_WEEK, MONDAY_INDEX)
                .with(ChronoField.NANO_OF_DAY, 0);
    }

    @NonNull
    public static LocalDate getFirstDayOfCurrentWeek() {
        return getFirstMomentOfCurrentWeekZoned().toLocalDate();
    }

    @NonNull
    public static Instant getFirstInstantOfCurrentWeek() {
        return getFirstMomentOfCurrentWeekZoned().toInstant();
    }

    @NonNull
    public static LocalDate getFirstDayOfNextWeek() {
        return getFirstDayOfCurrentWeek().plusWeeks(1);
    }

    @NonNull
    public static Instant getLastInstantOfCurrentWeek() {
        return getFirstInstantOfCurrentWeek().plus(7, ChronoUnit.DAYS);
    }

    public static boolean doesMatchWeekBounds(@NonNull Instant start,
                                              @NonNull Instant end) {
        LocalDate firstDayOfCurrentWeek = CalendarUtil.getFirstDayOfCurrentWeek();
        LocalDate firstDayOfNextWeek = CalendarUtil.getFirstDayOfNextWeek();

        return firstDayOfCurrentWeek.isEqual(LocalDate.ofInstant(start, ZoneId.systemDefault()))
                && firstDayOfNextWeek.isEqual(LocalDate.ofInstant(end, ZoneId.systemDefault()));
    }

    public static boolean isInCurrentWeek(@NonNull Instant start,
                                          @NonNull Instant end) {
        LocalDate firstDayOfCurrentWeek = CalendarUtil.getFirstDayOfCurrentWeek();
        LocalDate lastDayOfCurrentWeek = CalendarUtil.getFirstDayOfNextWeek();

        return !LocalDate.ofInstant(start, ZoneId.systemDefault()).isBefore(firstDayOfCurrentWeek)
                && !LocalDate.ofInstant(end, ZoneId.systemDefault()).isAfter(lastDayOfCurrentWeek);
    }
}
