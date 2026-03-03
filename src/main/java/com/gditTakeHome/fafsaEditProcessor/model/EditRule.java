package com.gditTakeHome.fafsaEditProcessor.model;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.RuleResult;

public interface EditRule {
    RuleResult apply(ApplicationRequest application);
    String getRuleId();
    Severity getSeverity();

    default String getRuleName() {
        return getClass().getSimpleName();
    }
}
