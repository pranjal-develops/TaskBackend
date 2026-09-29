package com.example.task.helpers.task.DTO;

import com.example.task.helpers.task.entity.Task;
import com.example.task.helpers.task.enums.DayOfWeek;
import com.example.task.helpers.task.enums.MonthMode;
import com.example.task.helpers.task.enums.TaskFrequency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class RecurrenceRuleResponse {
    public Long id;
    private TaskFrequency frequency;
    private boolean weekdaysOnly;
    private Set<DayOfWeek> weekdays;
    private MonthMode monthMode;
    private Integer dayOfMonth;
    private Integer nDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime seriesStartDateTime;
    private Integer scheduleVersion;
}
