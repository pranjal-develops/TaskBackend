package com.example.task.helpers.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SearchResponseDto {
    private Long id;
    private String username;
    private String pfp;
    private String banner;
    private String firstName;
    private String lastName;
}
