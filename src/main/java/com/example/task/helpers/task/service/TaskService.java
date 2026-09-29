package com.example.task.helpers.task.service;

import com.example.task.helpers.auth.entity.User;
import com.example.task.helpers.auth.exception.UserNotFoundException;
import com.example.task.helpers.auth.repo.UserRepo;
import com.example.task.helpers.collaboration.dto.CollaborationResponseDto;
import com.example.task.helpers.collaboration.entity.CollaborationRequest;
import com.example.task.helpers.collaboration.enums.CollabStatus;
import com.example.task.helpers.collaboration.service.CollaborationService;
import com.example.task.helpers.task.DTO.*;
import com.example.task.helpers.task.entity.*;
import com.example.task.helpers.task.enums.*;
import com.example.task.helpers.task.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class TaskService {
    private final TaskRepo taskRepo;
    private final TaskOccurrenceRepo taskOccurrenceRepo;
    private final TaskRecurrenceRuleRepo recurrenceRepo;
    private final UserRepo userRepo;
    private final RecurrenceGenerator recurrenceGenerator;
    private final CollaborationService collabService;

    private User getCurrentUser(){
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return userRepo.findByUsername(username).orElseThrow(() -> {
            log.warn("User not found");
            return new UserNotFoundException("User not found");
        });
    }

    public List<TaskDTO> getAllTasks(){
        return taskRepo.findByCollaboratorOrCreator(getCurrentUser())
                .stream()
                .map(this::toTaskDTO)
                .toList();
    }

    public TaskOccurrenceDTO getOccurrenceById(Long id){
        return toOccurrenceDTO(
                taskOccurrenceRepo.findById(id)
                .orElseThrow(()-> new UserNotFoundException("Task not found")
                )
        );
    }

    @Transactional
    public TaskDTO createTask(TaskCreateRequest request){
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.DRAFT);
        task.setCreator(getCurrentUser());
        task.setTaskTimezone(request.getTaskTimezone());
        task.setAllDay(request.isAllDay());
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        task.setVersion(1L);
        task.setRecurrenceType(request.getRecurrenceType());

        if (request.isAllDay()) {
            task.setAllDay(true);
            task.setAllDayStartDate(request.getAllDayStartDate());
            task.setAllDayEndDate(request.getAllDayEndDate());
        }

        // Save task first to establish ID for relationships
        Task savedTask = taskRepo.save(task);

        // Process collaboration invitations first
        if (request.getCollaboratorUsernames() != null && !request.getCollaboratorUsernames().isEmpty()) {
            collabService.inviteCollaborators(savedTask.getId(), request.getCollaboratorUsernames());
        }

        if (request.getRecurrenceType() == RecurrenceType.RECURRING) {
            TaskRecurrenceRule recurrence = createRecurrenceRule(savedTask, request);
            TaskRecurrenceRule savedRule = recurrenceRepo.save(recurrence);
            savedTask.setRecurrenceRule(savedRule);

            // Use user-provided end date, fallback to 90 days if not provided
            LocalDateTime start = request.getStartDateTime();
            LocalDateTime end = request.getOccurrenceGenerationEndDate() != null 
                    ? request.getOccurrenceGenerationEndDate() 
                    : start.plusDays(90);
            
            List<TaskOccurrence> occurrences = recurrenceGenerator.generateOccurrences(savedRule, start, end);
            for (int i = 0; i < occurrences.size(); i++) {
                TaskOccurrence occurrence = occurrences.get(i);
                occurrence.setSequenceNumber(i + 1);
                savedTask.addOccurrence(occurrence);
            }


            // Assign sequence numbers manually before saving
//            for (int i = 0; i < occurrences.size(); i++) {
//                occurrences.get(i).setSequenceNumber(i + 1);
//            }
//
//            taskOccurrenceRepo.saveAll(occurrences);
//            savedTask.setOccurrences(occurrences);
        } else {
            // Create a single occurrence for single tasks (both all-day and timed)
            TaskOccurrence occurrence = createSingleOccurrence(savedTask, request);
            savedTask.getOccurrences().add(occurrence);
        }

        return toTaskDTO(taskRepo.save(savedTask));
    }

    private TaskOccurrence createSingleOccurrence(Task task, TaskCreateRequest request) {
        ZoneId taskZone = ZoneId.of(task.getTaskTimezone());
        LocalDateTime startLdt = request.getStartDateTime();
        LocalDateTime endLdt = request.getEndDateTime();

        if (request.isAllDay()) {
            if (startLdt == null && request.getAllDayStartDate() != null) {
                startLdt = request.getAllDayStartDate().atStartOfDay();
            }
            if (endLdt == null && request.getAllDayEndDate() != null) {
                endLdt = request.getAllDayEndDate().atTime(23, 59, 59);
            }
        }

        Instant startUtc = startLdt != null ? startLdt.atZone(taskZone).toInstant() : Instant.now();
        Instant endUtc = endLdt != null ? endLdt.atZone(taskZone).toInstant() : startUtc;

        TaskOccurrence occurrence = new TaskOccurrence(
                null,
                task,
                startUtc,
                endUtc,
                startUtc,
                endUtc,
                false,
                false,
                1,
                "CONFIRMED"
        );
        return taskOccurrenceRepo.save(occurrence);
    }

    private TaskRecurrenceRule createRecurrenceRule(Task task, TaskCreateRequest request) {
        TaskRecurrenceRule recur = new TaskRecurrenceRule();
        recur.setTask(task);
        recur.setFrequency(request.getFrequency());
        recur.setWeekdays(request.getWeekdays());
        recur.setSeriesStartDateTime(request.getStartDateTime());
        recur.setStartTime(request.getStartDateTime().toLocalTime());
        recur.setEndTime(request.getEndDateTime().toLocalTime());
        recur.setScheduleVersion(1);

        if (request.getFrequency() == TaskFrequency.WEEKLY) {
            recur.setWeekdaysOnly(request.isWeekdaysOnly());
        } else if (request.getFrequency() == TaskFrequency.MONTHLY) {
            recur.setMonthMode(request.getMonthMode());
            if (request.getMonthMode() == MonthMode.DAY_OF_MONTH) {
                recur.setDayOfMonth(request.getDayOfMonth());
            }
        } else if (request.getFrequency() == TaskFrequency.EVERY_N_DAYS) {
            recur.setNDays(request.getNDays());
        }
        return recur;
    }

    public List<TaskOccurrenceDTO> getCalendarOccurrences(ZonedDateTime userFrom, ZonedDateTime userTo) {
        User currentUser = getCurrentUser();
        
        // Convert user's local range to UTC for database query
        Instant startUtc = userFrom.toInstant();
        Instant endUtc = userTo.toInstant();

//        return taskOccurrenceRepo.findByCollaboratorOrCreatorInRange(currentUser, startUtc, endUtc, Pageable.unpaged())
        return taskOccurrenceRepo.findByCollaboratorOrCreatorInRangeNoPage(currentUser, startUtc, endUtc)
                .stream()
                .map(this::toOccurrenceDTO)
                .toList();
    }

    public Page<TaskOccurrenceDTO> getPaginatedOccurrences(ZonedDateTime userFrom, ZonedDateTime userTo, org.springframework.data.domain.Pageable pageable) {
        User currentUser = getCurrentUser();
        
        // Convert user's local range to UTC for database query
        Instant startUtc = userFrom.toInstant();
        Instant endUtc = userTo.toInstant();

        return taskOccurrenceRepo.findByCollaboratorOrCreatorInRange(currentUser, startUtc, endUtc, pageable)
                .map(this::toOccurrenceDTO);
    }

    @Transactional
//    public TaskOccurrenceDTO rescheduleOccurrence(Long occurrenceId, LocalDateTime newStart, LocalDateTime newEnd){
    public TaskOccurrenceDTO rescheduleOccurrence(Long occurrenceId, Instant newStart, Instant newEnd){
        TaskOccurrence occurrence = taskOccurrenceRepo.findById(occurrenceId)
                .orElseThrow(()->{
            log.warn("Occurrence not found");
            return new EntityNotFoundException("Occurrence not found");
        });
        if(!occurrence.getTask().getCreator().equals(getCurrentUser())){
            log.warn("Unauthorized user tried to modify the occurrence with id {}", occurrenceId);
            throw new RuntimeException("Unauthorized to modify this task");
        }
//        occurrence.setStartDateTime(newStart);
//        occurrence.setEndDateTime(newEnd);
        occurrence.setStartTimeUtc(newStart);
        occurrence.setEndTimeUtc(newEnd);
        occurrence.setLocked(true);

        return toOccurrenceDTO(taskOccurrenceRepo.save(occurrence));
    }

    @Transactional
    public @Nullable TaskOccurrenceDTO setOccurrenceComplete(Long id) {
        TaskOccurrence occurrence = taskOccurrenceRepo.findById(id)
                .orElseThrow(()->{
                    log.warn("Occurrence id not found");
                    return new EntityNotFoundException("Occurrence not found");
                });
        if(!occurrence.getTask().getCreator().equals(getCurrentUser())){
            log.warn("Unauthorized user tried to complete the occurrence with id {}", id);
            throw new RuntimeException("Unauthorized to modify this task");
        }
        occurrence.setComplete(!occurrence.isComplete());
        return toOccurrenceDTO(taskOccurrenceRepo.save(occurrence));
    }

    @Transactional
    public void deleteTask (Long taskId){
        Task task = taskRepo.findById(taskId)
                .orElseThrow(()-> new EntityNotFoundException("Task not found"));

        if(!task.getCreator().equals(getCurrentUser())){
            log.warn("Unauthorized user tried to delete the task with id {}", taskId);
            throw new RuntimeException("Unauthorized to delete this task");
        }
        taskRepo.deleteById(taskId);
    }

//    public Page<TaskDTO> getPaginatedTasks(Pageable pageable){
//        return taskRepo.findByCollaboratorOrCreator(getCurrentUser(), pageable).map(this::toTaskDTO);
//    }

//    public Page<TaskOccurrenceDTO> getPaginatedOccurrences(LocalDateTime start, LocalDateTime end, Pageable pageable) {
//        User currentUser = getCurrentUser();
//        return taskOccurrenceRepo.findByTaskCreatorAndStartDateTimeBetween(currentUser, start, end, pageable)
//                .map(this::toOccurrenceDTO);
//    }

    public TaskDTO getTaskById (Long taskId){
        Task task = taskRepo.findById(taskId).orElseThrow(()-> new EntityNotFoundException("Task not found"));
        return toTaskDTO(task);
    }

    //To DTOs

    private RecurrenceRuleResponse recurrenceRuleDto(TaskRecurrenceRule rule){
        return new RecurrenceRuleResponse(
            rule.getId(),
            rule.getFrequency(),
            rule.isWeekdaysOnly(),
            rule.getWeekdays(),
            rule.getMonthMode(),
            rule.getDayOfMonth(),
            rule.getNDays(),
            rule.getStartTime(),
            rule.getEndTime(),
            rule.getSeriesStartDateTime(),
            rule.getScheduleVersion()
        );
    }

    private CollaborationResponseDto toCollabDto(CollaborationRequest collab){
        return new CollaborationResponseDto(
            collab.getId(),
            collab.getCollaborator().getUsername(),
            collab.getStatus(),
            collab.getRejectionReason(),
            collab.getCreatedAt(),
            collab.getUpdatedAt()
        );
    }

    private TaskDTO toTaskDTO(Task task){
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreator().getUsername(),
                task.getTaskTimezone(),
                task.isAllDay(),
                task.getAllDayStartDate(),
                task.getAllDayEndDate(),
                task.getRecurrenceType(),
                task.getRecurrenceType() == RecurrenceType.RECURRING ?
                        recurrenceRuleDto(task.getRecurrenceRule()) :
                        null,
                task.getOccurrences().stream().map(this::toOccurrenceDTO).toList(),
                task.getCollabs().stream().map(this::toCollabDto).toList(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getVersion()
        );
    }

    private TaskOccurrenceDTO toOccurrenceDTO(TaskOccurrence occurrence){
        User currentUser = getCurrentUser();
        String targetTimezone = currentUser.getPreferredTimezone() != null 
                ? currentUser.getPreferredTimezone() 
                : occurrence.getTask().getTaskTimezone();
        
        ZoneId targetZone = ZoneId.of(targetTimezone);

        // Map UTC storage back to the User's preferred timezone for the UI
        ZonedDateTime startZoned = occurrence.getStartTimeUtc().atZone(targetZone);
        ZonedDateTime endZoned = occurrence.getEndTimeUtc().atZone(targetZone);
        ZonedDateTime now = ZonedDateTime.now(targetZone);

        OccurrenceStatus status = OccurrenceStatus.TO_DO;

        if(occurrence.isComplete()) status = OccurrenceStatus.COMPLETED;
        else if (now.isAfter(startZoned) && now.isBefore(endZoned)) status = OccurrenceStatus.IN_PROGRESS;
        else if (now.isAfter(endZoned)) status = OccurrenceStatus.OVERDUE;

        List<OccurrenceCollabDto> collaborators = new ArrayList<>();
        if (!occurrence.getTask().getCollabs().isEmpty()) {
            collaborators.add(
                    new OccurrenceCollabDto(
                            occurrence.getTask().getCreator().getFirstName(),
                            occurrence.getTask().getCreator().getLastName(),
                            occurrence.getTask().getCreator().getUsername(),
                            occurrence.getTask().getCreator().getPfp(),
                            CollabStatus.ORGANIZER
                    )
            );
        }
        occurrence.getTask().getCollabs()
                .forEach(collab ->
                    collaborators.add(
                            new OccurrenceCollabDto(
                                    collab.getCollaborator().getFirstName(),
                                    collab.getCollaborator().getLastName(),
                                    collab.getCollaborator().getUsername(),
                                    collab.getCollaborator().getPfp(),
                                    collab.getStatus()
                            )
                        )
                );

        return new TaskOccurrenceDTO(
                occurrence.getId(),
                occurrence.getTask().getId(),
                occurrence.getTask().getTitle(),
                occurrence.getTask().getDescription(),
                startZoned,
                endZoned,
                targetTimezone,
                occurrence.isComplete(),
                occurrence.isLocked(),
                occurrence.getSequenceNumber(),
                status,
                occurrence.getTask().isAllDay(),
                collaborators
        );
    }

    //Scheduling Assistant

    public SchedulingAssistantResponse getSchedulingAssistantData(List<String> usernames, ZonedDateTime start, ZonedDateTime end) {
        List<User> participants = new ArrayList<>();
        
        // Always include the current user (creator)
        participants.add(getCurrentUser());
        
        // Add requested potential collaborators
        if (usernames != null) {
            for (String username : usernames) {
                userRepo.findByUsername(username).ifPresent(participants::add);
            }
        }

        Instant startUtc = start.toInstant();
        Instant endUtc = end.toInstant();
        User viewer = getCurrentUser();
        String tz = viewer.getPreferredTimezone() != null ? viewer.getPreferredTimezone() : "UTC";
        ZoneId viewerZone = ZoneId.of(tz);

        List<SchedulingAssistantResponse.UserBusySchedule> schedules = participants.stream()
                .distinct() // Remove duplicates if creator is in list
                .map(user -> {
                    List<TaskOccurrence> busyOccurrences = taskOccurrenceRepo.findByCollaboratorOrCreatorInRangeNoPage(user, startUtc, endUtc);
                    
                    List<SchedulingAssistantResponse.BusySlot> busySlots = busyOccurrences.stream().map(occ -> 
                        SchedulingAssistantResponse.BusySlot.builder()
                                .start(occ.getStartTimeUtc().atZone(viewerZone))
                                .end(occ.getEndTimeUtc().atZone(viewerZone))
                                .taskTitle(user.equals(viewer) ? occ.getTask().getTitle() : "Busy")
                                .build()
                    ).toList();

                    return SchedulingAssistantResponse.UserBusySchedule.builder()
                            .username(user.getUsername())
                            .busySlots(busySlots)
                            .build();
                }).toList();
        return new SchedulingAssistantResponse(schedules);
    }


}
