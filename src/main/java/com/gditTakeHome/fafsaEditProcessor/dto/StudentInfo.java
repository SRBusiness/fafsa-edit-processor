package com.gditTakeHome.fafsaEditProcessor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentInfo {
    private String firstName;
    private String lastName;
    private String ssn;
    private LocalDate dateOfBirth;
}
