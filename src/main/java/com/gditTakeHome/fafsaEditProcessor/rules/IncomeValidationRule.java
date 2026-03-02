package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.Income;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class IncomeValidationRule implements EditRule {

    @Override
    public String getRuleId() {
        return "INCOME_VALIDATION";
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public RuleResult apply(ApplicationRequest application) {
        Income income = application.getIncome();

        if (income == null || income.getStudentIncome() == null) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("Income Validation")
                    .passed(false)
                    .severity(getSeverity())
                    .message("Student income is required.")
                    .build();
        }

        if (income.getStudentIncome().compareTo(BigDecimal.ZERO) < 0) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("Income Validation")
                    .passed(false)
                    .severity(getSeverity())
                    .message("Student income cannot be negative.")
                    .build();
        }

        if (income.getParentIncome() != null && income.getParentIncome().compareTo(BigDecimal.ZERO) < 0) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("Income Validation")
                    .passed(false)
                    .severity(getSeverity())
                    .message("Parent income cannot be negative.")
                    .build();
        }

        return RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName("Income Validation")
                .passed(true)
                .severity(getSeverity())
                .message("Income values are valid.")
                .build();
    }
}
