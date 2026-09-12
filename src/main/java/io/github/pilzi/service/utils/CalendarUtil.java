package io.github.pilzi.service.utils;

import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

public class CalendarUtil {
    public static final int MONDAY_INDEX = 1;

    @NonNull
    public static LocalDate getFirstDayOfCurrentWeek() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant ( Instant.now() , ZoneId.systemDefault());
        return zdt.with ( ChronoField.DAY_OF_WEEK , MONDAY_INDEX ).with(ChronoField.NANO_OF_DAY, 0).toLocalDate();
    }

    @NonNull
    public static LocalDate getLastDayOfCurrentWeek() {
        LocalDate firstDayOfCurrentWeek = getFirstDayOfCurrentWeek();

        return firstDayOfCurrentWeek.plusWeeks ( 1 );
    }
}
