package com.example.task.helpers.notification.entity;

import com.example.task.helpers.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 1000)
    private String message;
    private String type;
    private String relatedEntityId;
    private boolean isRead;
    private Instant createdAt;
}
