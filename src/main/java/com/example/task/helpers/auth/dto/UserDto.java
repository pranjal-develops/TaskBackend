package com.example.task.helpers.auth.dto;

import com.example.task.helpers.auth.enums.Role;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    private String pfp;
    private Role role;
    private String preferredTimezone = "UTC";
//    private List<Long> taskIDs = new ArrayList<>();
}
