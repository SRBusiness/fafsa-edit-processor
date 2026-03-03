package com.gditTakeHome.fafsaEditProcessor.service;

import com.gditTakeHome.fafsaEditProcessor.dto.*;
import com.gditTakeHome.fafsaEditProcessor.model.ApplicationStatus;
import com.gditTakeHome.fafsaEditProcessor.rules.DependentParentIncomeRule;
import com.gditTakeHome.fafsaEditProcessor.rules.HouseholdLogicRule;
import com.gditTakeHome.fafsaEditProcessor.rules.IncomeValidationRule;
import com.gditTakeHome.fafsaEditProcessor.rules.MaritalStatusRule;
import com.gditTakeHome.fafsaEditProcessor.rules.SsnFormatRule;
import com.gditTakeHome.fafsaEditProcessor.rules.StateCodeRule;
import com.gditTakeHome.fafsaEditProcessor.rules.StudentAgeRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import com.gditTakeHome.fafsaEditProcessor.model.DependencyStatus;
import com.gditTakeHome.fafsaEditProcessor.model.MaritalStatus;

import static org.assertj.core.api.Assertions.assertThat;

class EditProcessorServiceTest {

    private EditProcessorService service;

    @BeforeEach
    void setUp() {
        service = new EditProcessorService(List.of(
                new StudentAgeRule(Clock.systemDefaultZone()),
                new SsnFormatRule(),
                new DependentParentIncomeRule(),
                new IncomeValidationRule(),
                new HouseholdLogicRule(),
                new StateCodeRule(),
                new MaritalStatusRule()
        ));
    }

    @Test
    void returnsValid_whenAllRulesPass() {
        ApplicationRequest request = validRequest();
        ValidationResponse response = service.process(request);

        assertThat(response.getApplicationStatus()).isEqualTo(ApplicationStatus.VALID);
        assertThat(response.getRuleResults()).hasSize(7);
        assertThat(response.getRuleResults()).allMatch(RuleResult::isPassed);
    }

    @Test
    void returnsInvalid_whenStudentIsTooYoung() {
        ApplicationRequest request = validRequest();
        request.getStudentInfo().setDateOfBirth(LocalDate.now().minusYears(10));

        ValidationResponse response = service.process(request);

        assertThat(response.getApplicationStatus()).isEqualTo(ApplicationStatus.INVALID);
        assertThat(response.getErrorCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void returnsInvalid_whenSsnIsInvalid() {
        ApplicationRequest request = validRequest();
        request.getStudentInfo().setSsn("bad-ssn");

        ValidationResponse response = service.process(request);

        assertThat(response.getApplicationStatus()).isEqualTo(ApplicationStatus.INVALID);
    }

    @Test
    void allSevenRulesAreEvaluated_evenWhenMultipleFail() {
        // Application with multiple failures
        ApplicationRequest request = ApplicationRequest.builder()
                .studentInfo(StudentInfo.builder()
                        .ssn("INVALID")
                        .dateOfBirth(LocalDate.now().minusYears(10))
                        .build())
                .dependencyStatus(DependencyStatus.DEPENDENT)
                .maritalStatus(MaritalStatus.MARRIED)
                .household(Household.builder().numberInHousehold(2).numberInCollege(5).build())
                .income(Income.builder().studentIncome(BigDecimal.valueOf(-100)).build())
                .stateOfResidence("XX")
                .build();

        ValidationResponse response = service.process(request);

        assertThat(response.getRuleResults()).hasSize(7);
        assertThat(response.getApplicationStatus()).isEqualTo(ApplicationStatus.INVALID);
        assertThat(response.getErrorCount()).isGreaterThan(1);
    }

    private ApplicationRequest validRequest() {
        return ApplicationRequest.builder()
                .studentInfo(StudentInfo.builder()
                        .firstName("Jane")
                        .lastName("Smith")
                        .ssn("123456789")
                        .dateOfBirth(LocalDate.of(2003, 5, 15))
                        .build())
                .dependencyStatus(DependencyStatus.DEPENDENT)
                .maritalStatus(MaritalStatus.SINGLE)
                .household(Household.builder().numberInHousehold(4).numberInCollege(1).build())
                .income(Income.builder()
                        .studentIncome(BigDecimal.valueOf(5000))
                        .parentIncome(BigDecimal.valueOf(65000))
                        .build())
                .stateOfResidence("CA")
                .build();
    }
}
