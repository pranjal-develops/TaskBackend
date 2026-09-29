package com.example.task.helpers.task.entity;

import com.example.task.helpers.task.enums.DayOfWeek;
import com.example.task.helpers.task.enums.MonthMode;
import com.example.task.helpers.task.enums.TaskFrequency;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class TaskRecurrenceRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private Task task;
    @Enumerated(EnumType.STRING)
    private TaskFrequency frequency;
    @Column(name = "weekdays_only", nullable = false)
    private boolean weekdaysOnly;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_weekdays", joinColumns =
    @JoinColumn(name = "task_recurrence_rule_id"))
    @Column(name = "weekdays")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> weekdays;
    @Enumerated(EnumType.STRING)
    private MonthMode monthMode;

    // For monthly mode DAY_OF_MONTH: e.g., 15
    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    // For EVERY_N_DAYS mode, e.g., 28
    @Column(name = "n_days")
    private Integer nDays;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "series_start_date_time", nullable = false)
    private LocalDateTime seriesStartDateTime;


    // Schedule version used for tracking reschedules
    @Column(name = "schedule_version", nullable = false)
    private Integer scheduleVersion = 1;

}
