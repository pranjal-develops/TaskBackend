package com.example.task.helpers.auth.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RefreshJwtResponse {
    private String token;
}
