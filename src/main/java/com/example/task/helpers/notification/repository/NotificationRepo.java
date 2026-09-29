package com.example.task.helpers.notification.repository;

import com.example.task.helpers.auth.entity.User;
import com.example.task.helpers.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);
    long countByRecipientAndIsReadFalse(User recipient);
}
