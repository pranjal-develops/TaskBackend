package com.example.task.helpers.task.DTO;

import com.example.task.helpers.task.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Timezone is required")
    private String taskTimezone;

    @NotNull(message = "Recurrence type is required (SINGLE or RECURRING)")
    private RecurrenceType recurrenceType;

    private boolean allDay = false;

    private LocalDate allDayStartDate;
    private LocalDate allDayEndDate;

    // --- Timed Configuration (Used if allDay is false) ---
    // For SINGLE: The exact start and end of the task.
    // For RECURRING: The start/end time of the first occurrence and the series start.
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    // --- Recurrence Logic (Only required if recurrenceType is RECURRING) ---
    private TaskFrequency frequency;
    private boolean weekdaysOnly = false;
    private Set<DayOfWeek> weekdays;
    private MonthMode monthMode;
    private Integer dayOfMonth;
    private Integer nDays;
    
    // The end date for the initial generation of occurrences
    private LocalDateTime occurrenceGenerationEndDate;

    // --- Collaboration ---
    private List<String> collaboratorUsernames;
}