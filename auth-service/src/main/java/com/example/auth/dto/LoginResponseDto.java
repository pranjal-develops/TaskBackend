package com.example.task.helpers.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDto {
    private String token;
//    private String refreshToken;
    private AuthResponseDto user;
}
