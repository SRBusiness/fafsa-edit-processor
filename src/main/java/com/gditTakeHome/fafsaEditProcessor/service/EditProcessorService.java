package com.gditTakeHome.fafsaEditProcessor.service;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;
import com.gditTakeHome.fafsaEditProcessor.dto.ValidationResponse;
import com.gditTakeHome.fafsaEditProcessor.model.ApplicationStatus;
import com.gditTakeHome.fafsaEditProcessor.model.EditRule;
import com.gditTakeHome.fafsaEditProcessor.model.Severity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EditProcessorService {

    private final List<EditRule> rules;

    public EditProcessorService(List<EditRule> rules) {
        this.rules = rules;
    }

    public ValidationResponse process(ApplicationRequest request) {
        List<RuleResult> results = rules.stream()
                .map(rule -> rule.apply(request))
                .collect(Collectors.toList());

        boolean hasErrors = results.stream()
                .anyMatch(r -> !r.isPassed() && r.getSeverity() == Severity.ERROR);

        ApplicationStatus status = hasErrors ? ApplicationStatus.INVALID : ApplicationStatus.VALID;

        return ValidationResponse.builder()
                .applicationStatus(status)
                .ruleResults(results)
                .build();
    }
}
