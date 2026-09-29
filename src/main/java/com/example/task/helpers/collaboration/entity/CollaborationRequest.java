package com.example.task.helpers.collaboration.entity;

import com.example.task.helpers.auth.entity.User;
import com.example.task.helpers.collaboration.enums.CollabStatus;
import com.example.task.helpers.task.entity.Task;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaborator_id", nullable = false)
    private User collaborator;
    @Enumerated(EnumType.STRING)
    private CollabStatus status;
    @Column(length = 1000)
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
}
