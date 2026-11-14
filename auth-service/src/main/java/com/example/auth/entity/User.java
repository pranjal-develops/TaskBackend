package com.example.task.helpers.auth.entity;

import com.example.task.helpers.auth.enums.Role;
import com.example.task.helpers.task.entity.Task;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @JsonIgnore
    private String password;
    private String email;
    private String firstName;
    private String lastName;
//    @Column(name = "default_timezone", nullable = false, length = 80)
//    private String defaultTimeZone = "UTC";
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    @Column(columnDefinition = "TEXT")
    private String pfp;
    @Column(columnDefinition = "TEXT")
    private String banner;
    @Enumerated
    private Role role;
    @Column(name = "preferred_timezone")
    private String preferredTimezone = "UTC";
//    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
//    private List<Task> tasks;

    @Column(name =  "email_notification")
    private boolean emailNotification = false;

    @OneToMany(mappedBy = "creator")
    private List<Task> tasks = new ArrayList<>();

    public User(
            String username,
            String password,
            String email,
            String pfp,
            Role role
    ) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.pfp = pfp;
        this.role = role;

        this.active = true;
//        this.defaultTimeZone = "UTC";
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

}
