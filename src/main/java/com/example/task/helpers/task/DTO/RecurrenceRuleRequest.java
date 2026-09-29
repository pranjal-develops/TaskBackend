package com.example.task.helpers.task.DTO;

import com.example.task.helpers.task.enums.DayOfWeek;
import com.example.task.helpers.task.enums.MonthMode;
import com.example.task.helpers.task.enums.TaskFrequency;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for the repetition logic. Used only when recurrenceType is RECURRING.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurrenceRuleRequest {

    @NotNull(message = "Frequency is required for recurring tasks (DAILY, WEEKLY, MONTHLY, EVERY_N_DAYS)")
    private TaskFrequency frequency;

    private boolean weekdaysOnly;

    private Set<DayOfWeek> weekdays;

    private MonthMode monthMode;

    private Integer dayOfMonth;

    private Integer nDays;
}