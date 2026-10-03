package io.github.pilzi;

import io.github.pilzi.service.utils.CalendarUtil;
import junit.framework.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class CalendarUtilTest {
    @AfterEach
    void resetTimeSource() {
        // Each test does modify the timeSource in calendar util
        CalendarUtil.timeSource = () -> ZonedDateTime.now(ZoneId.systemDefault());
    }

    @BeforeEach
    void manipulateTimeSource() {
        ZonedDateTime fixedInstant = ZonedDateTime.parse("2023-10-04T00:00:00Z");
        CalendarUtil.timeSource = () -> fixedInstant;
    }

    @Test
    public void does_first_day_of_week_match_expected() {
        String expectedFirstDay =  "2023-10-02";
        String result = CalendarUtil.getFirstDayOfCurrentWeek().toString();

        Assert.assertEquals(expectedFirstDay, result);
    }

    @Test
    public void does_last_day_of_week_match_expected() {
        String expectedFirstDay =  "2023-10-09";
        String result = CalendarUtil.getFirstDayOfNextWeek().toString();

        Assert.assertEquals(expectedFirstDay, result);
    }

    @Test
    public void does_match_week_bounds() {
        Instant start = Instant.parse("2023-10-02T00:00:00Z");
        Instant end = Instant.parse("2023-10-09T00:00:00Z");

        boolean isInCurrentWeek = CalendarUtil.doesMatchWeekBounds(start, end);

        Assert.assertTrue(isInCurrentWeek);
    }

    @Test
    public void does_is_in_current_week_detect_day_in_week() {
        Instant start = Instant.parse("2023-10-02T00:00:00Z");
        Instant end = Instant.parse("2023-10-08T00:00:00Z");

        boolean isInCurrentWeek = CalendarUtil.isInCurrentWeek(start, end);

        Assert.assertTrue(isInCurrentWeek);
    }

    @Test
    public void does_is_in_current_week_detect_start_date_outside_week() {

        Instant start = Instant.parse("2023-10-01T00:00:00Z");
        Instant end = Instant.parse("2023-10-08T00:00:00Z");

        boolean isInCurrentWeek = CalendarUtil.isInCurrentWeek(start, end);

        Assert.assertFalse(isInCurrentWeek);
    }


    @Test
    public void does_is_in_current_week_detect_end_date_outside_week() {
        Instant start = Instant.parse("2023-10-01T10:00:00Z");
        Instant end = Instant.parse("2023-10-09T00:00:00Z");

        boolean isInCurrentWeek = CalendarUtil.isInCurrentWeek(start, end);

        Assert.assertFalse(isInCurrentWeek);
    }
}
