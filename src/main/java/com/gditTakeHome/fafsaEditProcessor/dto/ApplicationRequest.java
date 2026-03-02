package com.gditTakeHome.fafsaEditProcessor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {
    private StudentInfo studentInfo;
    private String dependencyStatus;
    private String maritalStatus;
    private Household household;
    private Income income;
    private String stateOfResidence;
    private SpouseInfo spouseInfo;
}
