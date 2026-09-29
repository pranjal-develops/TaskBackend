package com.example.task.helpers.task.entity;

import com.example.task.helpers.auth.entity.User;
import com.example.task.helpers.collaboration.entity.CollaborationRequest;
import com.example.task.helpers.task.enums.RecurrenceType;
import com.example.task.helpers.task.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(length = 5000)
    private String description;
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;
    @Column(name = "task_timezone", nullable = false, length = 80)
    private String taskTimezone;

    private boolean allDay;
    @Column(name = "all_day_start_date")
    private LocalDate allDayStartDate;

    @Column(name = "all_day_end_date")
    private LocalDate allDayEndDate;

    @Enumerated(EnumType.STRING)
    private RecurrenceType recurrenceType;
    @OneToOne(
            mappedBy = "task",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private TaskRecurrenceRule recurrenceRule;
    @OneToMany(
            mappedBy = "task",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<TaskOccurrence> occurrences = new ArrayList<>();
    @OneToMany(
           mappedBy = "task",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<CollaborationRequest>  collabs = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
    @Version
    private Long version;


    public void setRecurrenceRule(TaskRecurrenceRule recurrenceRule) {
        this.recurrenceRule = recurrenceRule;

        if (recurrenceRule != null && recurrenceRule.getTask() != this) {
            recurrenceRule.setTask(this);
        }
    }

    public void removeRecurrenceRule() {
        if (this.recurrenceRule != null) {
            this.recurrenceRule.setTask(null);
            this.recurrenceRule = null;
        }
    }

    public void addOccurrence(TaskOccurrence occurrence) {
        occurrences.add(occurrence);
        occurrence.setTask(this);
    }

    public void removeOccurrence(TaskOccurrence occurrence) {
        occurrences.remove(occurrence);
        occurrence.setTask(null);
    }

    public void clearOccurrences() {
        for (TaskOccurrence occurrence : occurrences) {
            occurrence.setTask(null);
        }
        occurrences.clear();
    }

    public void addCollaboration(CollaborationRequest collaboration) {
        collabs.add(collaboration);
        collaboration.setTask(this);
    }

    public void removeCollaboration(CollaborationRequest collaboration) {
        collabs.remove(collaboration);
        collaboration.setTask(null);
    }

}
