package com.example.task.helpers.task.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityRequest {
    @NotNull(message = "Proposed slots are required")
    private List<TimeSlot> proposedSlots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private ZonedDateTime start;
        private ZonedDateTime end;
    }
}