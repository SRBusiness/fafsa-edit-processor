package com.gditTakeHome.fafsaEditProcessor.dto;

import com.gditTakeHome.fafsaEditProcessor.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {
    private ApplicationStatus applicationStatus;
    private List<RuleResult> ruleResults;

    public long getErrorCount() {
        return ruleResults == null ? 0 :
                ruleResults.stream().filter(r -> !r.isPassed()).count();
    }

    public long getPassCount() {
        return ruleResults == null ? 0 :
                ruleResults.stream().filter(RuleResult::isPassed).count();
    }
}
