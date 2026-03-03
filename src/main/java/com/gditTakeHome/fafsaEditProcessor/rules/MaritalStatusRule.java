package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.dto.SpouseInfo;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.MaritalStatus;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

/**
 * Validates that spouse information is provided when the applicant is married.
 *
 * <p>This rule only applies when {@link com.gditTakeHome.fafsaEditProcessor.model.MaritalStatus}
 * is {@code MARRIED}. Non-married applicants pass automatically. When married, the spouse's
 * SSN must be present and non-blank.
 */
@Component
public class MaritalStatusRule implements EditRule {

    private static final String RULE_ID = "MARITAL_STATUS";

    private static final String MSG_NOT_MARRIED = "Applicant is not married; spouse info not required.";
    private static final String MSG_SPOUSE_PRESENT = "Spouse information is provided for married applicant.";
    private static final String MSG_SPOUSE_REQUIRED = "Spouse name and SSN are required when marital status is 'married'.";

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public RuleResult apply(ApplicationRequest application) {
        RuleResult.RuleResultBuilder ruleResult = RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName(getRuleName())
                .severity(getSeverity());

        // Rule only applies to married applicants
        if (application.getMaritalStatus() != MaritalStatus.MARRIED) {
            return ruleResult.passed(true).message(MSG_NOT_MARRIED).build();
        }

        // Spouse SSN must be present and non-blank
        SpouseInfo spouse = application.getSpouseInfo();
        boolean spouseInfoComplete = spouse != null
                && spouse.getSsn() != null
                && !spouse.getSsn().isBlank();

        // Married applicant is missing required spouse information
        if (!spouseInfoComplete) {
            return ruleResult.passed(false).message(MSG_SPOUSE_REQUIRED).build();
        }

        // Spouse information is complete
        return ruleResult.passed(true).message(MSG_SPOUSE_PRESENT).build();
    }
}
