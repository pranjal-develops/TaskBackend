package com.example.task.helpers.task.service;

import com.example.task.helpers.task.entity.Task;
import com.example.task.helpers.task.entity.TaskOccurrence;
import com.example.task.helpers.task.entity.TaskRecurrenceRule;
import com.example.task.helpers.task.enums.DayOfWeek;
import com.example.task.helpers.task.enums.TaskFrequency;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class RecurrenceGenerator {

    private static final int MAX_OCCURRENCES = 500;

    public List<TaskOccurrence> generateOccurrences(TaskRecurrenceRule rule, LocalDateTime windowStart, LocalDateTime windowEnd) {
        validateInputs(rule, windowStart, windowEnd);

        List<TaskOccurrence> occurrences = new ArrayList<>();
        
        // 1. Jump ahead optimization
        LocalDateTime current = calculateJumpAheadStart(rule, windowStart);

        int iterations = 0;
        int maxSafetyIterations = 5000; 

        while ((current.isBefore(windowEnd) || current.isEqual(windowEnd))) {
            if (++iterations > maxSafetyIterations) {
                throw new IllegalStateException("Recurrence generation exceeded safety limits. Check rule parameters.");
            }

            if (isValidOccurrence(rule, current)) {
                if (!current.isBefore(windowStart)) {
                    occurrences.add(createOccurrence(rule.getTask(), rule, current));
                }
            }

            if (occurrences.size() >= MAX_OCCURRENCES) break;

            current = getNextPotentialOccurrence(rule, current);
        }

        return occurrences;
    }

    private void validateInputs(TaskRecurrenceRule rule, LocalDateTime start, LocalDateTime end) {
        if (rule == null) throw new IllegalArgumentException("Rule cannot be null");
        if (start == null || end == null) throw new IllegalArgumentException("Window bounds cannot be null");
        if (end.isBefore(start)) throw new IllegalArgumentException("Window end cannot be before start");

        if (rule.getFrequency() == TaskFrequency.EVERY_N_DAYS && (rule.getNDays() == null || rule.getNDays() <= 0)) {
            throw new IllegalArgumentException("nDays must be > 0");
        }
    }

    private LocalDateTime calculateJumpAheadStart(TaskRecurrenceRule rule, LocalDateTime windowStart) {
        LocalDateTime seriesStart = rule.getSeriesStartDateTime();
        if (!seriesStart.isBefore(windowStart)) return seriesStart;

        return switch (rule.getFrequency()) {
            case DAILY -> {
                long days = ChronoUnit.DAYS.between(seriesStart.toLocalDate(), windowStart.toLocalDate());
                yield seriesStart.plusDays(Math.max(0, days));
            }
            case EVERY_N_DAYS -> {
                long totalDays = ChronoUnit.DAYS.between(seriesStart.toLocalDate(), windowStart.toLocalDate());
                long intervals = totalDays / rule.getNDays();
                yield seriesStart.plusDays(intervals * rule.getNDays());
            }
            case WEEKLY -> {
                long weeks = ChronoUnit.WEEKS.between(seriesStart.toLocalDate(), windowStart.toLocalDate());
                yield seriesStart.plusWeeks(Math.max(0, weeks));
            }
            case MONTHLY -> {
                long months = ChronoUnit.MONTHS.between(seriesStart.toLocalDate(), windowStart.toLocalDate());
                yield seriesStart.plusMonths(Math.max(0, months));
            }
        };
    }

    private LocalDateTime getNextPotentialOccurrence(TaskRecurrenceRule rule, LocalDateTime current) {
        return switch (rule.getFrequency()) {
            case DAILY, WEEKLY -> current.plusDays(1);
            case MONTHLY -> current.plusMonths(1);
            case EVERY_N_DAYS -> current.plusDays(rule.getNDays());
        };
    }

    private boolean isValidOccurrence(TaskRecurrenceRule rule, LocalDateTime current) {
        // WeekdaysOnly flag
        if (rule.isWeekdaysOnly()) {
            java.time.DayOfWeek dow = current.getDayOfWeek();
            if (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY) return false;
        }

        // Weekly specific days
        if (rule.getFrequency() == TaskFrequency.WEEKLY && rule.getWeekdays() != null && !rule.getWeekdays().isEmpty()) {
            if (!rule.getWeekdays().contains(mapDayOfWeek(current.getDayOfWeek()))) return false;
        }

        // Monthly day-of-month check (Strict)
        if (rule.getFrequency() == TaskFrequency.MONTHLY && rule.getDayOfMonth() != null) {
            // If the current date's day doesn't match requested (e.g. requested 31, it's Feb 28)
            // We only return true if the day matches, OR if it's the last day of a shorter month
            int requestedDay = rule.getDayOfMonth();
            int actualDay = current.getDayOfMonth();
            
            if (actualDay != requestedDay) {
                // Check if requestedDay is actually impossible for this month
                boolean isLastDayOfMonth = current.toLocalDate().plusDays(1).getMonth() != current.getMonth();
                if (!(isLastDayOfMonth && requestedDay > actualDay)) {
                    return false;
                }
            }
        }

        return true;
    }

    private TaskOccurrence createOccurrence(Task task, TaskRecurrenceRule rule, LocalDateTime start) {
        LocalTime startTime = rule.getStartTime();
        LocalTime endTime = rule.getEndTime();
        
        LocalDateTime occurrenceStart = start.with(startTime);
        LocalDateTime occurrenceEnd = start.with(endTime);

        // Overnight logic: If end <= start, it's an overnight task (ends next day)
        if (!endTime.isAfter(startTime)) {
            occurrenceEnd = occurrenceEnd.plusDays(1);
        }

        ZoneId taskZone = ZoneId.of(task.getTaskTimezone());
        Instant startUtc = occurrenceStart.atZone(taskZone).toInstant();
        Instant endUtc = occurrenceEnd.atZone(taskZone).toInstant();

        return new TaskOccurrence(
                null, task, startUtc, endUtc,
                startUtc, endUtc, false, false,  null, "CONFIRMED"
        );
    }

    private DayOfWeek mapDayOfWeek(java.time.DayOfWeek dow) {
        return DayOfWeek.valueOf(dow.name());
    }
}