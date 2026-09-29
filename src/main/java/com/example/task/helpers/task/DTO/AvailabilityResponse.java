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
public class AvailabilityResponse {
    private List<CollaboratorAvailability> collaboratorAvailabilities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaboratorAvailability {
        private String username;
        private List<SlotStatus> slotStatuses;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotStatus {
        private ZonedDateTime start;
        private ZonedDateTime end;
        private boolean available;
        private List<String> conflictingTaskTitles;
    }
}