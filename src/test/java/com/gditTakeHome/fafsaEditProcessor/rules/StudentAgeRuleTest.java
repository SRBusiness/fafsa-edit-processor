package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.dto.StudentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class StudentAgeRuleTest {

    private StudentAgeRule rule;

    @BeforeEach
    void setUp() {
        rule = new StudentAgeRule();
    }

    @Test
    void passes_whenStudentIsOlderThan14() {
        ApplicationRequest request = requestWithDob(LocalDate.now().minusYears(20));
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void passes_whenStudentIsExactly14() {
        ApplicationRequest request = requestWithDob(LocalDate.now().minusYears(14));
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void fails_whenStudentIsYoungerThan14() {
        ApplicationRequest request = requestWithDob(LocalDate.now().minusYears(13));
        RuleResult result = rule.apply(request);
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMessage()).contains("at least 14");
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
