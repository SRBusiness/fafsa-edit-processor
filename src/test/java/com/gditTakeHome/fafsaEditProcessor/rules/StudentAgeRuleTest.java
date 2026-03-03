package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.dto.StudentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class StudentAgeRuleTest {

    private static final LocalDate FIXED_TODAY = LocalDate.of(2024, 6, 15);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);

    private StudentAgeRule rule;

    @BeforeEach
    void setUp() {
        rule = new StudentAgeRule(FIXED_CLOCK);
    }

    @Test
    void passes_whenStudentIsOlderThan14() {
        ApplicationRequest request = requestWithDob(FIXED_TODAY.minusYears(20));
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_whenStudentIsExactly14() {
        ApplicationRequest request = requestWithDob(FIXED_TODAY.minusYears(14));
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void fails_whenStudentIsYoungerThan14() {
        ApplicationRequest request = requestWithDob(FIXED_TODAY.minusYears(13));
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("at least 14");
    }

    @Test
    void fails_whenDateOfBirthIsInTheFuture() {
        ApplicationRequest request = requestWithDob(FIXED_TODAY.plusDays(1));
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("future");
    }

    @Test
    void fails_whenAgeExceedsMaximum() {
        ApplicationRequest request = requestWithDob(FIXED_TODAY.minusYears(121));
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("120");
    }

    @Test
    void fails_whenDateOfBirthIsNull() {
        ApplicationRequest request = ApplicationRequest.builder()
                .studentInfo(StudentInfo.builder().build())
                .build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("required");
    }

    @Test
    void fails_whenStudentInfoIsNull() {
        ApplicationRequest request = ApplicationRequest.builder().build();
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
    }

    private ApplicationRequest requestWithDob(LocalDate dob) {
        return ApplicationRequest.builder()
                .studentInfo(StudentInfo.builder().dateOfBirth(dob).build())
                .build();
    }
}