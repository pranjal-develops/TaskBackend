package com.example.task.helpers.task.DTO;
import com.example.task.helpers.collaboration.dto.CollaborationResponseDto;
import com.example.task.helpers.task.enums.RecurrenceType;
import com.example.task.helpers.task.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private String creator;
    private String taskTimezone;
    private boolean allDay;
    private LocalDate allDayStartDate;
    private LocalDate allDayEndDate;
    private RecurrenceType recurrenceType;
    private RecurrenceRuleResponse recurrenceRule;
    private List<TaskOccurrenceDTO> occurrences = new ArrayList<>();
    private List<CollaborationResponseDto> collaborations = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
    @Version
    private Long version;
}
