package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StateCodeRuleTest {

    private StateCodeRule rule;

    @BeforeEach
    void setUp() {
        rule = new StateCodeRule();
    }

    @Test
    void passes_withValidStateCode() {
        RuleResult result = rule.apply(requestWithState("CA"));
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_withDcCode() {
        RuleResult result = rule.apply(requestWithState("DC"));
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_withTerritoryCodes() {
        for (String code : new String[]{"PR", "VI", "GU", "AS", "MP"}) {
            RuleResult result = rule.apply(requestWithState(code));
            assertThat(result.isPassed()).as("Expected territory code %s to pass", code).isTrue();
        }
    }

    @Test
    void passes_withLeadingAndTrailingWhitespace() {
        RuleResult result = rule.apply(requestWithState("  CA  "));
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void fails_withInvalidStateCode() {
        RuleResult result = rule.apply(requestWithState("XX"));
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("not a valid");
    }

    @Test
    void passes_withLowercaseStateCode() {
        // Rule normalizes to uppercase before checking
        RuleResult result = rule.apply(requestWithState("ca"));
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void fails_withNullState() {
        RuleResult result = rule.apply(ApplicationRequest.builder().build());
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("required");
    }

    @Test
    void fails_withBlankState() {
        RuleResult result = rule.apply(requestWithState("  "));
        assertThat(result.isPassed()).isFalse();
    }

    private ApplicationRequest requestWithState(String state) {
        return ApplicationRequest.builder()
                .stateOfResidence(state)
                .build();
    }
}
