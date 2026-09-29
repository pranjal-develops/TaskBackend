package com.example.task.helpers.notification.service;

import com.example.task.helpers.auth.entity.User;
import com.example.task.helpers.auth.repo.UserRepo;
import com.example.task.helpers.notification.DTO.NotificationDto;
import com.example.task.helpers.notification.entity.Notification;
import com.example.task.helpers.notification.repository.NotificationRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;
    private final EmailService emailService;

    private User getCurrentUser() {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return userRepo.findByUsername(username).orElseThrow();
    }

    @Transactional
    public void createNotification(User recipient, String title, String message, String type, String relatedId) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityId(relatedId)
                .isRead(false)
                .createdAt(Instant.now())
                .build();
        notificationRepo.save(notification);

        // Send email if enabled for the user
        if (recipient.isEmailNotification() && recipient.getEmail() != null) {
            emailService.sendMail(
                recipient.getEmail(),
                "Task App: " + title,
                message
            );
        }
    }

    public Page<NotificationDto> getNotifications( Pageable pageable) {
        return notificationRepo.findByRecipientOrderByCreatedAtDesc(getCurrentUser(), pageable)
                .map(this::toDto);
    }

    public long getUnreadCount() {
        return notificationRepo.countByRecipientAndIsReadFalse(getCurrentUser());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        if (!notification.getRecipient().equals(getCurrentUser())) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setRead(true);
        notificationRepo.save(notification);
    }

    private NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .relatedEntityId(notification.getRelatedEntityId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
