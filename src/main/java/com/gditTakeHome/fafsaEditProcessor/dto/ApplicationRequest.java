package com.gditTakeHome.fafsaEditProcessor.dto;

import com.gditTakeHome.fafsaEditProcessor.model.DependencyStatus;
import com.gditTakeHome.fafsaEditProcessor.model.MaritalStatus;
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
    private DependencyStatus dependencyStatus;
    private MaritalStatus maritalStatus;
    private Household household;
    private Income income;
    private String stateOfResidence;
    private SpouseInfo spouseInfo;
}
