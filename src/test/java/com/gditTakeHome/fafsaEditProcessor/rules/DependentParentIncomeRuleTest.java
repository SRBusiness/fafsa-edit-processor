package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.Income;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.DependencyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DependentParentIncomeRuleTest {

    private DependentParentIncomeRule rule;

    @BeforeEach
    void setUp() {
        rule = new DependentParentIncomeRule();
    }

    @Test
    void fails_whenDependentAndNoParentIncome() {
        ApplicationRequest request = ApplicationRequest.builder()
                .dependencyStatus(DependencyStatus.DEPENDENT)
                .income(Income.builder().studentIncome(BigDecimal.valueOf(5000)).build())
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).containsIgnoringCase("parent income");
    }

    @Test
    void passes_whenDependentAndParentIncomeProvided() {
        ApplicationRequest request = ApplicationRequest.builder()
                .dependencyStatus(DependencyStatus.DEPENDENT)
                .income(Income.builder()
                        .studentIncome(BigDecimal.valueOf(5000))
                        .parentIncome(BigDecimal.valueOf(65000))
                        .build())
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_whenDependentAndParentIncomeIsZero() {
        ApplicationRequest request = ApplicationRequest.builder()
                .dependencyStatus(DependencyStatus.DEPENDENT)
                .income(Income.builder()
                        .studentIncome(BigDecimal.valueOf(5000))
                        .parentIncome(BigDecimal.ZERO)
                        .build())
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_whenIndependentAndNoParentIncome() {
        ApplicationRequest request = ApplicationRequest.builder()
                .dependencyStatus(DependencyStatus.INDEPENDENT)
                .income(Income.builder().studentIncome(BigDecimal.valueOf(5000)).build())
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_whenIndependentAndParentIncomeProvided() {
        // Parent income is only required for dependent students — independent students
        // may voluntarily include it without triggering a failure.
        ApplicationRequest request = ApplicationRequest.builder()
                .dependencyStatus(DependencyStatus.INDEPENDENT)
                .income(Income.builder()
                        .studentIncome(BigDecimal.valueOf(5000))
                        .parentIncome(BigDecimal.valueOf(45000))
                        .build())
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_whenNullDependencyStatusAndNoParentIncome() {
        ApplicationRequest request = ApplicationRequest.builder()
                .income(Income.builder().studentIncome(BigDecimal.valueOf(5000)).build())
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void fails_whenDependentAndIncomeObjectIsNull() {
        ApplicationRequest request = ApplicationRequest.builder()
                .dependencyStatus(DependencyStatus.DEPENDENT)
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
    }
}
