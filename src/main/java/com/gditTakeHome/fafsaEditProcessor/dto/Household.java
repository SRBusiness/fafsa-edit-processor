package com.gditTakeHome.fafsaEditProcessor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Household {
    private Integer numberInHousehold;
    private Integer numberInCollege;
}
