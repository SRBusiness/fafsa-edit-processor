package com.gditTakeHome.fafsaEditProcessor.dto;

import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleResult {
    private String ruleId;
    private String ruleName;
    private boolean passed;
    private Severity severity;
    private String message;
}
