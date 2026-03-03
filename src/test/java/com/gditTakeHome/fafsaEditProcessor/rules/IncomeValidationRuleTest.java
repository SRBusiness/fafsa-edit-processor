package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.Income;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class IncomeValidationRuleTest {

    private IncomeValidationRule rule;

    @BeforeEach
    void setUp() {
        rule = new IncomeValidationRule();
    }

    @Test
    void fails_whenStudentIncomeIsNegative() {
        RuleResult result = rule.apply(requestWithIncome(BigDecimal.valueOf(-1), null));
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("Student income cannot be negative");
    }

    @Test
    void fails_whenParentIncomeIsNegative() {
        RuleResult result = rule.apply(requestWithIncome(BigDecimal.valueOf(5000), BigDecimal.valueOf(-1)));
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("Parent income cannot be negative");
    }

    @Test
    void passes_whenBothIncomesAreZero() {
        RuleResult result = rule.apply(requestWithIncome(BigDecimal.ZERO, BigDecimal.ZERO));
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_whenBothIncomesArePositive() {
        RuleResult result = rule.apply(requestWithIncome(BigDecimal.valueOf(5000), BigDecimal.valueOf(65000)));
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_whenParentIncomeIsAbsent() {
        RuleResult result = rule.apply(requestWithIncome(BigDecimal.valueOf(5000), null));
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void fails_whenStudentIncomeIsNull() {
        RuleResult result = rule.apply(requestWithIncome(null, null));
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("required");
    }

    @Test
    void fails_whenIncomeObjectIsNull() {
        ApplicationRequest request = ApplicationRequest.builder().build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("required");
    }

    @Test
    void passes_withVeryLargeIncomeValues() {
        RuleResult result = rule.apply(requestWithIncome(
                new BigDecimal("9999999999999.99"),
                new BigDecimal("9999999999999.99")));
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_withDecimalIncomeValues() {
        RuleResult result = rule.apply(requestWithIncome(
                new BigDecimal("1234.56"),
                new BigDecimal("78901.23")));
        assertThat(result.isPassed()).isTrue();
    }

    private ApplicationRequest requestWithIncome(BigDecimal studentIncome, BigDecimal parentIncome) {
        return ApplicationRequest.builder()
                .income(Income.builder()
                        .studentIncome(studentIncome)
                        .parentIncome(parentIncome)
                        .build())
                .build();
    }
}
