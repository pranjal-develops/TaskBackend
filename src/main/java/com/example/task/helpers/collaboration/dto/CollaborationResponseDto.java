package com.example.task.helpers.collaboration.dto;

import com.example.task.helpers.collaboration.enums.CollabStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationResponseDto {
    private Long id;
    private String collaboratorUsername;
    private CollabStatus status;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
}
