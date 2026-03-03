package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

/**
 * Validates that a student's age falls within the acceptable range for FAFSA eligibility.
 *
 * <p>The following conditions result in a failed rule:
 * <ul>
 *   <li>Date of birth is missing</li>
 *   <li>Date of birth is a future date</li>
 *   <li>Calculated age exceeds {@value MAXIMUM_AGE} years (implausible data entry)</li>
 *   <li>Calculated age is below {@value MINIMUM_AGE} years</li>
 * </ul>
 *
 * <p>A {@link Clock} is injected rather than using {@code LocalDate.now()} directly,
 * allowing tests to freeze time for deterministic age boundary assertions.
 */
@Component
public class StudentAgeRule implements EditRule {
    private static final String RULE_ID = "STUDENT_AGE";
    private static final int MINIMUM_AGE = 14;
    private static final int MAXIMUM_AGE = 120;

    private static final String MSG_DOB_REQUIRED = "Date of birth is required to verify student age.";
    private static final String MSG_FUTURE_DATE = "Date of birth cannot be a future date.";
    private static final String MSG_EXCEEDS_MAX = "Date of birth indicates an age over " + MAXIMUM_AGE + " years, which is not valid.";
    private static final String MSG_TOO_YOUNG = "Student must be at least " + MINIMUM_AGE + " years old. Current age: ";
    private static final String MSG_VALID = "Student age is valid (age: ";

    private final Clock clock;

    /**
     * Constructs the rule with the provided clock.
     * In production, Spring injects {@code Clock.systemDefaultZone()} via {@code AppConfig}.
     *
     * @param clock the clock used to determine the current date
     */
    public StudentAgeRule(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    /**
     * Applies the student age validation rule to the given application.
     *
     * @param application the FAFSA application to validate
     * @return a {@link RuleResult} indicating whether the student's age is valid,
     *         with a descriptive message explaining the outcome
     */
    @Override
    public RuleResult apply(ApplicationRequest application) {
        RuleResult.RuleResultBuilder ruleResult = RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName(getRuleName())
                .severity(getSeverity());

        // DOB or student info missing — cannot determine age
        if (application.getStudentInfo() == null || application.getStudentInfo().getDateOfBirth() == null) {
            return ruleResult.passed(false).message(MSG_DOB_REQUIRED).build();
        }

        LocalDate dob = application.getStudentInfo().getDateOfBirth();
        LocalDate today = LocalDate.now(clock);

        // Reject future dates before calculating age
        if (dob.isAfter(today)) {
            return ruleResult.passed(false).message(MSG_FUTURE_DATE).build();
        }

        // Calculate age in whole years
        int age = Period.between(dob, today).getYears();

        // Reject implausibly old ages (likely a data entry error)
        if (age > MAXIMUM_AGE) {
            return ruleResult.passed(false).message(MSG_EXCEEDS_MAX).build();
        }

        // Reject ages below the minimum threshold
        if (age < MINIMUM_AGE) {
            return ruleResult.passed(false).message(MSG_TOO_YOUNG + age + ".").build();
        }

        // Age is within the valid range
        return ruleResult.passed(true).message(MSG_VALID + age + ").").build();
    }
}
