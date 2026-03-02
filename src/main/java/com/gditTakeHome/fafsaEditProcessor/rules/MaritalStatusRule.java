package com.gditTakeHome.fafsaEditProcessor.rules;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.dto.SpouseInfo;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Component;

@Component
public class MaritalStatusRule implements EditRule {

    @Override
    public String getRuleId() {
        return "MARITAL_STATUS";
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public RuleResult apply(ApplicationRequest application) {
        if (!"married".equalsIgnoreCase(application.getMaritalStatus())) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("Marital Status")
                    .passed(true)
                    .severity(getSeverity())
                    .message("Applicant is not married; spouse info not required.")
                    .build();
        }

        SpouseInfo spouse = application.getSpouseInfo();
        boolean spouseInfoComplete = spouse != null
                && spouse.getSsn() != null
                && !spouse.getSsn().isBlank();

        if (spouseInfoComplete) {
            return RuleResult.builder()
                    .ruleId(getRuleId())
                    .ruleName("Marital Status")
                    .passed(true)
                    .severity(getSeverity())
                    .message("Spouse information is provided for married applicant.")
                    .build();
        }

        return RuleResult.builder()
                .ruleId(getRuleId())
                .ruleName("Marital Status")
                .passed(false)
                .severity(getSeverity())
                .message("Spouse name and SSN are required when marital status is 'married'.")
                .build();
    }
}
