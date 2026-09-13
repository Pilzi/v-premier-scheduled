package io.github.pilzi.service.utils;

import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class CalendarUtil {
    public static final int MONDAY_INDEX = 1;

    private static ZonedDateTime getFirstMomentOfCurrentWeekZoned() {
        return ZonedDateTime.now(ZoneId.systemDefault())
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
    public static LocalDate getLastDayOfCurrentWeek() {
        return getFirstDayOfCurrentWeek().plusWeeks(1);
    }

    @NonNull
    public static Instant getLastInstantOfCurrentWeek() {
        return getFirstInstantOfCurrentWeek().plus(7, ChronoUnit.DAYS);
    }

    public static boolean isInCurrentWeek(@NonNull Instant start,
                                   @NonNull Instant end) {
        LocalDate firstDayOfCurrentWeek = CalendarUtil.getFirstDayOfCurrentWeek();
        LocalDate lastDayOfCurrentWeek = CalendarUtil.getLastDayOfCurrentWeek();

        return LocalDate.ofInstant(start, ZoneId.systemDefault()).isAfter(firstDayOfCurrentWeek)
                && LocalDate.ofInstant(end, ZoneId.systemDefault()).isBefore(lastDayOfCurrentWeek);
    }

}
