package com.gditTakeHome.fafsaEditProcessor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Income {
    private BigDecimal studentIncome;
    private BigDecimal parentIncome;
}
