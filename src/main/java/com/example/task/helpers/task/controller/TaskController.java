package com.example.task.helpers.task.controller;

import com.example.task.helpers.task.DTO.SchedulingAssistantResponse;
import com.example.task.helpers.task.DTO.TaskCreateRequest;
import com.example.task.helpers.task.DTO.TaskDTO;
import com.example.task.helpers.task.DTO.TaskOccurrenceDTO;
import com.example.task.helpers.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TaskDTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/scheduling-assistant")
    public ResponseEntity<SchedulingAssistantResponse> getSchedulingAssistant(
            @RequestParam(required = false) List<String> usernames,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to) {
        return ResponseEntity.ok(taskService.getSchedulingAssistantData(usernames, from, to));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO createTask(@Valid @RequestBody TaskCreateRequest request){
        return taskService.createTask(request);
    }

    @GetMapping("/{id}")
    public TaskDTO getTask (@PathVariable Long id){
        return taskService.getTaskById(id);
    }

    @GetMapping("/occurrence/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskOccurrenceDTO getOccurrenceById(@PathVariable Long id) {
        return taskService.getOccurrenceById(id);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<TaskOccurrenceDTO>> getCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to) {
        return ResponseEntity.ok(taskService.getCalendarOccurrences(from, to));
    }

    @GetMapping("/occurrences")
    public ResponseEntity<Page<TaskOccurrenceDTO>> getOccurrences(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            @PageableDefault(size = 20, sort = "startTimeUtc") Pageable pageable) {
        return ResponseEntity.ok(taskService.getPaginatedOccurrences(from, to, pageable));
    }

    @PatchMapping("/occurrences/{occurrenceId}/reschedule")
    public ResponseEntity<TaskOccurrenceDTO> reschedule(
            @PathVariable Long occurrenceId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newStart,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newEnd) {
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant newStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant newEnd) {
        return ResponseEntity.ok(taskService.rescheduleOccurrence(occurrenceId, newStart, newEnd));
    }

    @PatchMapping("/occurrences/{occurrenceId}/complete")
    public ResponseEntity<TaskOccurrenceDTO> setComplete(@PathVariable Long occurrenceId){
        return ResponseEntity.ok(taskService.setOccurrenceComplete(occurrenceId));
    }
}
