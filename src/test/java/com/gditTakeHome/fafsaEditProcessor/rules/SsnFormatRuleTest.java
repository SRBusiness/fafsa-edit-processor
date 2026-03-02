package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.dto.StudentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SsnFormatRuleTest {

    private SsnFormatRule rule;

    @BeforeEach
    void setUp() {
        rule = new SsnFormatRule();
    }

    @Test
    void passes_withValid9DigitSsn() {
        RuleResult result = rule.apply(requestWithSsn("123456789"));
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void fails_withLettersInSsn() {
        RuleResult result = rule.apply(requestWithSsn("12345678A"));
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("9 digits");
    }

    @Test
    void fails_withTooShortSsn() {
        RuleResult result = rule.apply(requestWithSsn("12345678"));
        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void fails_withTooLongSsn() {
        RuleResult result = rule.apply(requestWithSsn("1234567890"));
        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void fails_withNullSsn() {
        ApplicationRequest request = ApplicationRequest.builder()
                .studentInfo(StudentInfo.builder().build())
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("required");
    }

    @Test
    void fails_withNullStudentInfo() {
        RuleResult result = rule.apply(ApplicationRequest.builder().build());
        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void fails_withDashesInSsn() {
        RuleResult result = rule.apply(requestWithSsn("123-45-6789"));
        assertThat(result.isPassed()).isFalse();
    }

    private ApplicationRequest requestWithSsn(String ssn) {
        return ApplicationRequest.builder()
                .studentInfo(StudentInfo.builder().ssn(ssn).build())
                .build();
    }
}
