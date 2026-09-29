package com.example.task.helpers.task.repository;

import com.example.task.helpers.auth.entity.User;
import com.example.task.helpers.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepo extends JpaRepository<Task, Long> {
    Page<Task> findByCreator(User creator, Pageable pageable);
    List<Task> findByCreator(User creator);

    @Query("SELECT DISTINCT t FROM Task t " +
           "LEFT JOIN t.collabs c " +
           "WHERE t.creator = :user OR (c.collaborator = :user AND c.status in ('ACCEPTED', 'APPROVED'))")
    Page<Task> findByCollaboratorOrCreator(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Task t " +
           "LEFT JOIN t.collabs c " +
           "WHERE t.creator = :user OR (c.collaborator = :user AND c.status in ('ACCEPTED', 'APPROVED'))")
    List<Task> findByCollaboratorOrCreator(@Param("user") User user);
}
