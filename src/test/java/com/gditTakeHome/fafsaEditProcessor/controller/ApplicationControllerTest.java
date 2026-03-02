package com.gditTakeHome.fafsaEditProcessor.controller;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.Household;
import com.gditTakeHome.fafsaEditProcessor.dto.Income;
import com.gditTakeHome.fafsaEditProcessor.dto.StudentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ApplicationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void returns200WithValidStatus_forValidApplication() throws Exception {
        ApplicationRequest request = ApplicationRequest.builder()
                .studentInfo(StudentInfo.builder()
                        .firstName("Jane")
                        .lastName("Smith")
                        .ssn("123456789")
                        .dateOfBirth(LocalDate.of(2003, 5, 15))
                        .build())
                .dependencyStatus("dependent")
                .maritalStatus("single")
                .household(Household.builder().numberInHousehold(4).numberInCollege(1).build())
                .income(Income.builder()
                        .studentIncome(BigDecimal.valueOf(5000))
                        .parentIncome(BigDecimal.valueOf(65000))
                        .build())
                .stateOfResidence("CA")
                .build();

        mockMvc.perform(post("/api/applications/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationStatus").value("VALID"))
                .andExpect(jsonPath("$.ruleResults").isArray())
                .andExpect(jsonPath("$.ruleResults.length()").value(7));
    }

    @Test
    void returns200WithInvalidStatus_forInvalidApplication() throws Exception {
        ApplicationRequest request = ApplicationRequest.builder()
                .studentInfo(StudentInfo.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .ssn("BADSSN")
                        .dateOfBirth(LocalDate.now().minusYears(10))
                        .build())
                .dependencyStatus("dependent")
                .maritalStatus("single")
                .household(Household.builder().numberInHousehold(2).numberInCollege(1).build())
                .income(Income.builder()
                        .studentIncome(BigDecimal.valueOf(5000))
                        .build())
                .stateOfResidence("CA")
                .build();

        mockMvc.perform(post("/api/applications/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationStatus").value("INVALID"))
                .andExpect(jsonPath("$.ruleResults").isArray())
                .andExpect(jsonPath("$.ruleResults.length()").value(7));
    }
}
