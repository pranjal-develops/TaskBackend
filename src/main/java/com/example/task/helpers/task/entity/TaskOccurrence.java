package com.example.task.helpers.task.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class TaskOccurrence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

//    @Column(name = "start_date_time", nullable = false)
//    private LocalDateTime startDateTime;
//    @Column(name = "end_date_time", nullable = false)
//    private LocalDateTime endDateTime;
//    private LocalDateTime originalStartDateTime;
//    private LocalDateTime originalEndDateTime;
    @Column(name = "start_time_utc", nullable = false)
    private Instant startTimeUtc;
    @Column(name = "end_time_utc", nullable = false)
    private Instant endTimeUtc;
    private Instant originalStartDateTimeUTC;
    private Instant originalEndDateTimeUTC;
    private boolean isComplete = false;


    /**
     * Indicates if this occurrence was individually rescheduled (locked from bulk overwrite)
     */
    @Column(name = "locked", nullable = false)
    private boolean locked = false;

    private Integer sequenceNumber;
    private String status;

}
