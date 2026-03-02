package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SsnFormatRule implements EditRule {

    private static final Pattern SSN_PATTERN = Pattern.compile("^\\d{9}$");

    @Override
    public String getRuleId() {
        return "SSN_FORMAT";
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public RuleResult apply(ApplicationRequest application) {
        String ssn = application.getStudentInfo() == null ? null : application.getStudentInfo().getSsn();

        if (ssn == null || ssn.isBlank()) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("SSN Format")
                    .passed(false)
                    .severity(getSeverity())
                    .message("Student SSN is required.")
                    .build();
        }

        if (SSN_PATTERN.matcher(ssn).matches()) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("SSN Format")
                    .passed(true)
                    .severity(getSeverity())
                    .message("SSN format is valid.")
                    .build();
        }

        return RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName("SSN Format")
                .passed(false)
                .severity(getSeverity())
                .message("SSN must be exactly 9 digits (no dashes or spaces).")
                .build();
    }
}
