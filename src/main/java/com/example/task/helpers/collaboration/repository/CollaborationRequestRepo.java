package com.example.task.helpers.collaboration.repository;

import com.example.task.helpers.auth.entity.User;
import com.example.task.helpers.collaboration.entity.CollaborationRequest;
import com.example.task.helpers.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollaborationRequestRepo extends JpaRepository<CollaborationRequest, Long> {
    Optional<User> findByTaskAndCollaborator(Task task, User collaborator);
}
