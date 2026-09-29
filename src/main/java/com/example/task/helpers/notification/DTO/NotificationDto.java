package com.example.task.helpers.notification.DTO;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String title;
    private String message;
    private String type;
    private String relatedEntityId;
    private boolean isRead;
    private Instant createdAt;
}
