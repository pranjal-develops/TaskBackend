package com.example.task.helpers.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminResDto {
    private Long id;
    private String username;
    private String email;
    private String pfp;
    private String role;
}
