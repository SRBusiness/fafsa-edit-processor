package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.dto.SpouseInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaritalStatusRuleTest {

    private MaritalStatusRule rule;

    @BeforeEach
    void setUp() {
        rule = new MaritalStatusRule();
    }

    @Test
    void fails_whenMarriedAndNoSpouseInfo() {
        ApplicationRequest request = ApplicationRequest.builder()
                .maritalStatus("married")
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("Spouse name and SSN");
    }

    @Test
    void fails_whenMarriedAndSpouseInfoMissingSsn() {
        ApplicationRequest request = ApplicationRequest.builder()
                .maritalStatus("married")
                .spouseInfo(SpouseInfo.builder().firstName("John").lastName("Smith").build())
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void passes_whenMarriedAndSpouseInfoComplete() {
        ApplicationRequest request = ApplicationRequest.builder()
                .maritalStatus("married")
                .spouseInfo(SpouseInfo.builder()
                        .firstName("John")
                        .lastName("Smith")
                        .ssn("987654321")
                        .build())
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_whenSingleAndNoSpouseInfo() {
        ApplicationRequest request = ApplicationRequest.builder()
                .maritalStatus("single")
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_whenNullMaritalStatusAndNoSpouseInfo() {
        ApplicationRequest request = ApplicationRequest.builder().build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isTrue();
    }
}
