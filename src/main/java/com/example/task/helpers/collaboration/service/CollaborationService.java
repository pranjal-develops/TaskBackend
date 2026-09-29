package com.example.task.helpers.collaboration.service;

import com.example.task.helpers.auth.entity.User;
import com.example.task.helpers.auth.exception.UserNotFoundException;
import com.example.task.helpers.auth.repo.UserRepo;
import com.example.task.helpers.collaboration.entity.CollaborationRequest;
import com.example.task.helpers.collaboration.enums.CollabStatus;
import com.example.task.helpers.collaboration.repository.CollaborationRequestRepo;
import com.example.task.helpers.notification.service.NotificationService;
import com.example.task.helpers.task.entity.Task;
import com.example.task.helpers.task.repository.TaskRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class CollaborationService {
    private final TaskRepo taskRepo;
    private final UserRepo userRepo;
    private final CollaborationRequestRepo collabRepo;
    private final NotificationService notificationService;

    private User getCurrentUser(){
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return userRepo.findByUsername(username).orElseThrow(() -> {
            log.warn("User not found");
            return new UserNotFoundException("User not found");
        });
    }

    @Transactional
    public void inviteCollaborators(Long taskId, List<String> collaboratorUsernames){
        Task task = taskRepo.findById(taskId).orElseThrow(()-> new EntityNotFoundException("Task not found"));
        if(!task.getCreator().equals(getCurrentUser())){
            throw new RuntimeException("Only the creator can invite collaborators");
        }

        for (String username : collaboratorUsernames){
            Optional<User> userOpt = userRepo.findByUsername(username);
            if (userOpt.isPresent()) {
                User collaborator = userOpt.get();
                if(collaborator == task.getCreator()) continue;
                if(collabRepo.findByTaskAndCollaborator(task, collaborator).isPresent()) continue;

                CollaborationRequest request = CollaborationRequest.builder()
                        .task(task)
                        .collaborator(collaborator)
                        .status(CollabStatus.PENDING_ACCEPTANCE)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                collabRepo.save(request);
                task.getCollabs().add(request);

                notificationService.createNotification(
                    collaborator,
                    "New Task Invitation",
                    String.format("%s invited you to collaborate on: %s", task.getCreator().getUsername(), task.getTitle()),
                    "INVITATION",
                    task.getId().toString()
                );
            }
        }

        log.info("Inviting collaborators {} to task {}", collaboratorUsernames, taskId);
    }

    @Transactional
    public void acceptInvitation(Long requestId) {
        CollaborationRequest request = collabRepo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Collaboration request not found"));

        if (!request.getCollaborator().equals(getCurrentUser())) {
            throw new RuntimeException("Unauthorized to accept this invitation");
        }

        request.setStatus(CollabStatus.ACCEPTED);
        request.setUpdatedAt(Instant.now());
        collabRepo.save(request);

        notificationService.createNotification(
            request.getTask().getCreator(),
            "Invitation Accepted",
            String.format("%s accepted your invitation for: %s", getCurrentUser().getUsername(), request.getTask().getTitle()),
            "ACCEPTANCE",
            request.getTask().getId().toString()
        );
    }

    @Transactional
    public void rejectInvitation(Long requestId, String reason) {
        CollaborationRequest request = collabRepo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Collaboration request not found"));

        if (!request.getCollaborator().equals(getCurrentUser())) {
            throw new RuntimeException("Unauthorized to reject this invitation");
        }

        request.setStatus(CollabStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setUpdatedAt(Instant.now());
        collabRepo.save(request);

        notificationService.createNotification(
            request.getTask().getCreator(),
            "Invitation Rejected",
            String.format("%s rejected your invitation for: %s. Reason: %s", 
                getCurrentUser().getUsername(), request.getTask().getTitle(), reason != null ? reason : "None"),
            "REJECTION",
            request.getTask().getId().toString()
        );
    }

    @Transactional
    public void approveCollaboration(Long requestId) {
        CollaborationRequest request = collabRepo.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Collaboration request not found"));

        if (!request.getTask().getCreator().equals(getCurrentUser())) {
            throw new RuntimeException("Only the creator can approve collaboration");
        }

        if (!CollabStatus.AWAITING_APPROVAL.equals(request.getStatus())) {
            throw new RuntimeException("Request is not in a state that can be approved");
        }

        request.setStatus(CollabStatus.APPROVED);
        request.setUpdatedAt(Instant.now());
        collabRepo.save(request);

        notificationService.createNotification(
            request.getCollaborator(),
            "Collaboration Approved",
            String.format("Your collaboration on task '%s' has been approved by %s", 
                request.getTask().getTitle(), getCurrentUser().getUsername()),
            "APPROVAL",
            request.getTask().getId().toString()
        );
    }
}
