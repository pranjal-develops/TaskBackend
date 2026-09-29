package com.example.task.helpers.collaboration.controller;

import com.example.task.helpers.collaboration.service.CollaborationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collaborations")
@RequiredArgsConstructor
public class CollaborationController {

    private final CollaborationService collabService;

    @PostMapping("/tasks/{taskId}/invite")
    public ResponseEntity<Void> inviteCollaborators(
            @PathVariable Long taskId,
            @RequestBody List<String> usernames) {
        collabService.inviteCollaborators(taskId, usernames);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<Void> acceptInvitation(@PathVariable Long requestId) {
        collabService.acceptInvitation(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<Void> rejectInvitation(
            @PathVariable Long requestId,
            @RequestParam(required = false) String reason) {
        collabService.rejectInvitation(requestId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<Void> approveCollaboration(@PathVariable Long requestId) {
        collabService.approveCollaboration(requestId);
        return ResponseEntity.ok().build();
    }
}