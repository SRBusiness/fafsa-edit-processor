package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class StudentAgeRule implements EditRule {

    private static final int MINIMUM_AGE = 14;

    @Override
    public String getRuleId() {
        return "STUDENT_AGE";
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public RuleResult apply(ApplicationRequest application) {
        if (application.getStudentInfo() == null || application.getStudentInfo().getDateOfBirth() == null) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("Student Age")
                    .passed(false)
                    .severity(getSeverity())
                    .message("Date of birth is required to verify student age.")
                    .build();
        }

        LocalDate dob = application.getStudentInfo().getDateOfBirth();
        int age = Period.between(dob, LocalDate.now()).getYears();

        if (age >= MINIMUM_AGE) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("Student Age")
                    .passed(true)
                    .severity(getSeverity())
                    .message("Student age is valid (age: " + age + ").")
                    .build();
        }

        return RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName("Student Age")
                .passed(false)
                .severity(getSeverity())
                .message("Student must be at least " + MINIMUM_AGE + " years old. Current age: " + age + ".")
                .build();
    }
}
