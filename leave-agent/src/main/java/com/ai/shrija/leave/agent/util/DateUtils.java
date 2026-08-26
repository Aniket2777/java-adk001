package com.ai.shrija.leave.agent.util;

import com.ai.shrija.leave.agent.exception.LeaveValidationException;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Date helpers shared across the leave service and tools.
 */
public final class DateUtils {

    private DateUtils() {
    }

    /**
     * Counts business days (Mon-Fri) between start and end, inclusive of both ends.
     */
    public static double countBusinessDays(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new LeaveValidationException("Both startDate and endDate are required");
        }
        if (end.isBefore(start)) {
            throw new LeaveValidationException("endDate must not be before startDate");
        }

        double days = 0;
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            if (cursor.getDayOfWeek() != DayOfWeek.SATURDAY && cursor.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days++;
            }
            cursor = cursor.plusDays(1);
        }
        return days;
    }

    public static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
