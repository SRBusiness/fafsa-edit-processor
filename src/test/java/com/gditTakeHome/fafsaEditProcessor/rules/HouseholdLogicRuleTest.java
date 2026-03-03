package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.Household;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HouseholdLogicRuleTest {

    private HouseholdLogicRule rule;

    @BeforeEach
    void setUp() {
        rule = new HouseholdLogicRule();
    }

    @Test
    void fails_whenNumberInCollegeExceedsHousehold() {
        RuleResult result = rule.apply(requestWithHousehold(3, 5));
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("cannot exceed");
    }

    @Test
    void passes_whenNumberInCollegeEqualsHousehold() {
        RuleResult result = rule.apply(requestWithHousehold(4, 4));
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_whenNumberInCollegeIsLessThanHousehold() {
        RuleResult result = rule.apply(requestWithHousehold(4, 1));
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void fails_whenHouseholdIsNull() {
        ApplicationRequest request = ApplicationRequest.builder().build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("required");
    }

    @Test
    void fails_whenNumberInHouseholdIsNull() {
        ApplicationRequest request = ApplicationRequest.builder()
                .household(Household.builder().numberInCollege(1).build())
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("required");
    }

    @Test
    void fails_whenNumberInCollegeIsNull() {
        ApplicationRequest request = ApplicationRequest.builder()
                .household(Household.builder().numberInHousehold(4).build())
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("required");
    }

    @Test
    void fails_whenNumberInHouseholdIsNegative() {
        RuleResult result = rule.apply(requestWithHousehold(-1, 0));
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("negative");
    }

    @Test
    void fails_whenNumberInCollegeIsNegative() {
        RuleResult result = rule.apply(requestWithHousehold(4, -1));
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("negative");
    }

    @Test
    void passes_whenBothAreZero() {
        RuleResult result = rule.apply(requestWithHousehold(0, 0));
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_whenNumberInCollegeIsZero() {
        RuleResult result = rule.apply(requestWithHousehold(4, 0));
        assertThat(result.isPassed()).isTrue();
    }

    private ApplicationRequest requestWithHousehold(int inHousehold, int inCollege) {
        return ApplicationRequest.builder()
                .household(Household.builder()
                        .numberInHousehold(inHousehold)
                        .numberInCollege(inCollege)
                        .build())
                .build();
    }
}
