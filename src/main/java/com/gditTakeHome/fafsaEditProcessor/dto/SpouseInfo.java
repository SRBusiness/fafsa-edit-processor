package com.gditTakeHome.fafsaEditProcessor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpouseInfo {
    private String firstName;
    private String lastName;
    private String ssn;
}
