package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validates that the student's Social Security Number (SSN) contains exactly 9 digits.
 *
 * <p>Dashes and spaces are stripped from the input before validation to accommodate
 * common formatting, so both {@code "123456789"} and {@code "123-45-6789"} are accepted.
 * Other characters (e.g. letters) are intentionally preserved so that inputs like
 * {@code "1A23456789"} correctly fail validation.
 */
@Component
public class SsnFormatRule implements EditRule {

    private static final String RULE_ID = "SSN_FORMAT";
    private static final Pattern SSN_PATTERN = Pattern.compile("^\\d{9}$");

    private static final String MSG_REQUIRED = "Student SSN is required.";
    private static final String MSG_INVALID = "SSN must contain exactly 9 digits.";
    private static final String MSG_VALID = "SSN format is valid.";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    /**
     * Applies the SSN format validation rule to the given application.
     * Non-digit characters are stripped from the SSN before the format check is performed.
     *
     * @param application the FAFSA application to validate
     * @return a {@link RuleResult} indicating whether the SSN format is valid
     */
    @Override
    public RuleResult apply(ApplicationRequest application) {
        String ssn = application.getStudentInfo() == null ? null : application.getStudentInfo().getSsn();

        RuleResult.RuleResultBuilder ruleResult = RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName(getRuleName())
                .severity(getSeverity());

        // SSN is missing or blank
        if (ssn == null || ssn.isBlank()) {
            return ruleResult.passed(false).message(MSG_REQUIRED).build();
        }

        // Strip dashes and spaces to accommodate common formatting (e.g. "123-45-6789")
        String digitsOnly = ssn.replaceAll("[\\s-]", "");

        // SSN does not contain exactly 9 digits after stripping
        if (!SSN_PATTERN.matcher(digitsOnly).matches()) {
            return ruleResult.passed(false).message(MSG_INVALID).build();
        }

        // SSN is valid
        return ruleResult.passed(true).message(MSG_VALID).build();
    }
}