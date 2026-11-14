package com.example.task.helpers.auth.dto;

import com.example.task.helpers.auth.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponseDto {
    private Long id;
    private String username;
    private String email;
    private String pfp;
    private String banner;
    private String firstName;
    private String lastName;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    private Role role;
    private String preferredTimezone;
}
