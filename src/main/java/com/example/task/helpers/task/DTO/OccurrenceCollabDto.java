package com.example.task.helpers.task.DTO;

import com.example.task.helpers.collaboration.enums.CollabStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OccurrenceCollabDto {
    private String firstName;
    private String lastName;
    private String username;
    private String pfp;
    private CollabStatus status;
}
