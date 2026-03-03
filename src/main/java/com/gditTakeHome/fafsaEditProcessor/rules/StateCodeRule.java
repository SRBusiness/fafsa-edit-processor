package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Validates that the applicant's state of residence is a recognized US state or territory abbreviation.
 *
 * <p>Input is stripped of leading/trailing whitespace and normalized to uppercase before the check,
 * so {@code " ca "} is treated the same as {@code "CA"}. The valid set contains all 50 states,
 * DC, and the US territories (PR, VI, GU, AS, MP).
 */
@Component
public class StateCodeRule implements EditRule {

    private static final String RULE_ID = "STATE_CODE";

    private static final Set<String> VALID_STATE_CODES = Set.of(
            // 50 states
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
            "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY",
            // territories
            "DC", "PR", "VI", "GU", "AS", "MP"
            // military postal codes (AA, AE, AP) excluded for now — add if needed in the future
    );

    private static final String MSG_REQUIRED = "State of residence is required.";
    private static final String MSG_VALID = "State code '%s' is valid.";
    private static final String MSG_INVALID = "'%s' is not a valid US state abbreviation.";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public RuleResult apply(ApplicationRequest application) {
        String state = application.getStateOfResidence();

        RuleResult.RuleResultBuilder ruleResult = RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName(getRuleName())
                .severity(getSeverity());

        // State is missing or blank
        if (state == null || state.isBlank()) {
            return ruleResult.passed(false).message(MSG_REQUIRED).build();
        }

        // Normalize whitespace and case, then check against the valid set
        if (!VALID_STATE_CODES.contains(state.strip().toUpperCase())) {
            return ruleResult.passed(false).message(String.format(MSG_INVALID, state)).build();
        }

        // State code is recognized
        return ruleResult.passed(true).message(String.format(MSG_VALID, state)).build();
    }
}