package com.example.task.helpers.task.DTO;

import com.example.task.helpers.collaboration.enums.CollabStatus;
import com.example.task.helpers.task.enums.OccurrenceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskOccurrenceDTO {
    private Long id;
    private Long taskId;
    private String taskName;
    private String description;
//    private LocalDateTime startDateTime;
//    private LocalDateTime endDateTime;
    // Timezone specific fields
    private ZonedDateTime startZonedDateTime;
    private ZonedDateTime endZonedDateTime;
    private String userPreferredTimezone;
    private boolean isComplete;
    private boolean locked;
    private Integer sequenceNumber;
//    private String status;
    private OccurrenceStatus status;
    private boolean allDay;
    private List<OccurrenceCollabDto> collaborators;
//    public TaskOccurrenceDTO(Long id, Long id1, String title, ZonedDateTime startZoned, ZonedDateTime endZoned, String targetTimezone, boolean locked, Integer sequenceNumber, String status) {
//    }
}
