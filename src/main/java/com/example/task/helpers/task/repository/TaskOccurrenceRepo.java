package com.example.task.helpers.task.repository;

import com.example.task.helpers.auth.entity.User;
import com.example.task.helpers.task.entity.TaskOccurrence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TaskOccurrenceRepo extends JpaRepository<TaskOccurrence, Long> {
    @Query("SELECT o FROM TaskOccurrence o " +
            "JOIN o.task t " +
            "WHERE (t.creator = :user OR EXISTS (" +
            "SELECT c.id FROM CollaborationRequest c " +
            "WHERE c.task = t AND c.collaborator = :user AND c.status in ('ACCEPTED', 'APPROVED'))) " +
            "AND o.startTimeUtc < :end AND o.endTimeUtc > :start")
//            "AND o.startTimeUtc >= :start AND o.endTimeUtc <= :end")
    Page<TaskOccurrence> findByCollaboratorOrCreatorInRange(
            @Param("user") User user,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);

    @Query("SELECT o FROM TaskOccurrence o " +
            "JOIN o.task t " +
            "WHERE (t.creator = :user OR EXISTS (" +
            "SELECT c.id FROM CollaborationRequest c " +
            "WHERE c.task = t AND c.collaborator = :user AND c.status in ('ACCEPTED', 'APPROVED'))) " +
            "AND o.startTimeUtc < :end AND o.endTimeUtc > :start")
    List<TaskOccurrence> findByCollaboratorOrCreatorInRangeNoPage(
            @Param("user") User user,
            @Param("start") Instant start,
            @Param("end") Instant end);
}
