package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class StateCodeRule implements EditRule {

    private static final Set<String> VALID_STATE_CODES = Set.of(
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
            "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY",
            "DC"
    );

    @Override
    public String getRuleId() {
        return "STATE_CODE";
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public RuleResult apply(ApplicationRequest application) {
        String state = application.getStateOfResidence();

        if (state == null || state.isBlank()) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("State Code")
                    .passed(false)
                    .severity(getSeverity())
                    .message("State of residence is required.")
                    .build();
        }

        if (VALID_STATE_CODES.contains(state.toUpperCase())) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("State Code")
                    .passed(true)
                    .severity(getSeverity())
                    .message("State code '" + state + "' is valid.")
                    .build();
        }

        return RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName("State Code")
                .passed(false)
                .severity(getSeverity())
                .message("'" + state + "' is not a valid US state abbreviation.")
                .build();
    }
}
