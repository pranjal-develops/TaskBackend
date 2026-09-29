package com.example.task.helpers.task.repository;

import com.example.task.helpers.task.entity.TaskRecurrenceRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRecurrenceRuleRepo extends JpaRepository<TaskRecurrenceRule, Long> {
}
