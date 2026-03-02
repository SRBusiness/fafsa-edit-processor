package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.Household;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

@Component
public class HouseholdLogicRule implements EditRule {

    @Override
    public String getRuleId() {
        return "HOUSEHOLD_LOGIC";
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public RuleResult apply(ApplicationRequest application) {
        Household household = application.getHousehold();

        if (household == null || household.getNumberInHousehold() == null || household.getNumberInCollege() == null) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("Household Logic")
                    .passed(false)
                    .severity(getSeverity())
                    .message("Household size and number in college are required.")
                    .build();
        }

        if (household.getNumberInCollege() <= household.getNumberInHousehold()) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("Household Logic")
                    .passed(true)
                    .severity(getSeverity())
                    .message("Household figures are valid.")
                    .build();
        }

        return RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName("Household Logic")
                .passed(false)
                .severity(getSeverity())
                .message("Number in college (" + household.getNumberInCollege()
                        + ") cannot exceed number in household (" + household.getNumberInHousehold() + ").")
                .build();
    }
}
