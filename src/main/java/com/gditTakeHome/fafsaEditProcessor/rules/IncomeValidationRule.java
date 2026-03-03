package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.Income;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Validates that student and parent income values are non-negative.
 *
 * <p>Student income is required; parent income is optional and only validated if present.
 * This rule does not enforce whether parent income must be provided — that is the
 * responsibility of {@link DependentParentIncomeRule}.
 */
@Component
public class IncomeValidationRule implements EditRule {

    private static final String RULE_ID = "INCOME_VALIDATION";

    private static final String MSG_STUDENT_INCOME_REQUIRED = "Student income is required.";
    private static final String MSG_STUDENT_INCOME_NEGATIVE = "Student income cannot be negative.";
    private static final String MSG_PARENT_INCOME_NEGATIVE = "Parent income cannot be negative.";
    private static final String MSG_VALID = "Income values are valid.";

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
        Income income = application.getIncome();

        RuleResult.RuleResultBuilder ruleResult = RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName(getRuleName())
                .severity(getSeverity());

        // Check if application or student income is missing
        if (income == null || income.getStudentIncome() == null) {
            return ruleResult.passed(false).message(MSG_STUDENT_INCOME_REQUIRED).build();
        }

        // Student income must be non-negative
        if (income.getStudentIncome().compareTo(BigDecimal.ZERO) < 0) {
            return ruleResult.passed(false).message(MSG_STUDENT_INCOME_NEGATIVE).build();
        }

        // Parent income is optional, but cannot be negative if provided
        if (income.getParentIncome() != null && income.getParentIncome().compareTo(BigDecimal.ZERO) < 0) {
            return ruleResult.passed(false).message(MSG_PARENT_INCOME_NEGATIVE).build();
        }

        // Income values are valid
        return ruleResult.passed(true).message(MSG_VALID).build();
    }
}
