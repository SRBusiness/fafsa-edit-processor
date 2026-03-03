package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.DependencyStatus;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

/**
 * Validates that a dependent student's application includes parent income.
 *
 * <p>This rule only applies when {@link com.gditTakeHome.fafsaEditProcessor.model.DependencyStatus}
 * is {@code DEPENDENT}. Independent students may optionally include parent income without penalty.
 * The presence of parent income is validated here; its value is validated by
 * {@link IncomeValidationRule}.
 */
@Component
public class DependentParentIncomeRule implements EditRule {

    private static final String RULE_ID = "DEPENDENT_PARENT_INCOME";

    private static final String MSG_NOT_DEPENDENT = "Applicant is not dependent; parent income not required.";
    private static final String MSG_INCOME_PRESENT = "Parent income is provided for dependent applicant.";
    private static final String MSG_INCOME_REQUIRED = "Parent income is required when dependency status is 'dependent'.";

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
        RuleResult.RuleResultBuilder ruleResult = RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName(getRuleName())
                .severity(getSeverity());

        // Rule only applies to dependent students
        if (application.getDependencyStatus() != DependencyStatus.DEPENDENT) {
            return ruleResult.passed(true).message(MSG_NOT_DEPENDENT).build();
        }

        // Check if parent income is present (value is validated by IncomeValidationRule)
        boolean parentIncomePresent = application.getIncome() != null
                && application.getIncome().getParentIncome() != null;

        // Dependent student is missing required parent income
        if (!parentIncomePresent) {
            return ruleResult.passed(false).message(MSG_INCOME_REQUIRED).build();
        }

        // Parent income is present for dependent student
        return ruleResult.passed(true).message(MSG_INCOME_PRESENT).build();
    }
}
