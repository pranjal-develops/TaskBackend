package com.example.task.helpers.task.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingAssistantResponse {
    private List<UserBusySchedule> schedules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBusySchedule {
        private String username;
        private List<BusySlot> busySlots;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusySlot {
        private ZonedDateTime start;
        private ZonedDateTime end;
        private String taskTitle; // Optional: only if permission allows
    }
}