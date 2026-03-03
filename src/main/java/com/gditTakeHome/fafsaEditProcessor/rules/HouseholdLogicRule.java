package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.Household;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

/**
 * Validates that household size and number of college students are logically consistent.
 *
 * <p>Both fields are required and must be non-negative. The number of students in college
 * cannot exceed the total number of people in the household.
 */
@Component
public class HouseholdLogicRule implements EditRule {

    private static final String RULE_ID = "HOUSEHOLD_LOGIC";

    private static final String MSG_REQUIRED = "Household size and number in college are required.";
    private static final String MSG_NEGATIVE = "Household size and number in college must not be negative.";
    private static final String MSG_INVALID = "Number in college (%d) cannot exceed number in household (%d).";
    private static final String MSG_VALID = "Household figures are valid.";

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
        Household household = application.getHousehold();

        RuleResult.RuleResultBuilder ruleResult = RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName(getRuleName())
                .severity(getSeverity());

        // Required household fields are missing
        if (household == null || household.getNumberInHousehold() == null || household.getNumberInCollege() == null) {
            return ruleResult.passed(false).message(MSG_REQUIRED).build();
        }

        // Reject negative values — household counts cannot be negative
        if (household.getNumberInHousehold() < 0 || household.getNumberInCollege() < 0) {
            return ruleResult.passed(false).message(MSG_NEGATIVE).build();
        }

        // Number in college cannot exceed total household size
        if (household.getNumberInCollege() > household.getNumberInHousehold()) {
            return ruleResult.passed(false)
                    .message(String.format(MSG_INVALID, household.getNumberInCollege(), household.getNumberInHousehold()))
                    .build();
        }

        // Household figures are logically consistent
        return ruleResult.passed(true).message(MSG_VALID).build();
    }
}
