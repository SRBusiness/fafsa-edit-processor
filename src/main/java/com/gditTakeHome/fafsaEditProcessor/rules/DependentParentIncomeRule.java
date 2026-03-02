package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

@Component
public class DependentParentIncomeRule implements EditRule {

    @Override
    public String getRuleId() {
        return "DEPENDENT_PARENT_INCOME";
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public RuleResult apply(ApplicationRequest application) {
        if (!"dependent".equalsIgnoreCase(application.getDependencyStatus())) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("Dependent Parent Income")
                    .passed(true)
                    .severity(getSeverity())
                    .message("Applicant is not dependent; parent income not required.")
                    .build();
        }

        boolean parentIncomePresent = application.getIncome() != null
                && application.getIncome().getParentIncome() != null;

        if (parentIncomePresent) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("Dependent Parent Income")
                    .passed(true)
                    .severity(getSeverity())
                    .message("Parent income is provided for dependent applicant.")
                    .build();
        }

        return RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName("Dependent Parent Income")
                .passed(false)
                .severity(getSeverity())
                .message("Parent income is required when dependency status is 'dependent'.")
                .build();
    }
}
