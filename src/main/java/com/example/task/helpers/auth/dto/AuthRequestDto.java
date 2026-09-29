package com.example.task.helpers.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthRequestDto {
    private String username;
    private String password;
    private String email;
    private String pfp;
}
