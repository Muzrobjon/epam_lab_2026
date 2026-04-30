package com.epam.gym.trainerworkloadservice.controller;

import com.epam.gym.trainerworkloadservice.dto.response.TrainerWorkloadResponse;
import com.epam.gym.trainerworkloadservice.filter.JwtAuthenticationFilter;
import com.epam.gym.trainerworkloadservice.service.TrainerWorkloadService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerWorkloadController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainerWorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainerWorkloadService workloadService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnTrainerWorkloadWithYearAndMonth() throws Exception {
        // given
        String trainerUsername = "john.doe";
        Integer year = 2024;
        Integer month = 3;
        String transactionId = "tx-123";

        TrainerWorkloadResponse.MonthSummary monthSummary =
                TrainerWorkloadResponse.MonthSummary.builder()
                        .month(month)
                        .trainingSummaryDuration(120L)
                        .build();

        TrainerWorkloadResponse.YearSummary yearSummary =
                TrainerWorkloadResponse.YearSummary.builder()
                        .year(year)
                        .months(List.of(monthSummary))
                        .build();

        TrainerWorkloadResponse response =
                TrainerWorkloadResponse.builder()
                        .trainerUsername(trainerUsername)
                        .trainerFirstName("John")
                        .trainerLastName("Doe")
                        .trainerStatus(true)
                        .years(List.of(yearSummary))
                        .build();

        Mockito.when(workloadService.getTrainerWorkload(
                        trainerUsername, year, month, transactionId))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/trainers/workload/{trainerUsername}", trainerUsername)
                        .param("year", year.toString())
                        .param("month", month.toString())
                        .header("X-Transaction-Id", transactionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value(trainerUsername))
                .andExpect(jsonPath("$.trainerFirstName").value("John"))
                .andExpect(jsonPath("$.trainerLastName").value("Doe"))
                .andExpect(jsonPath("$.trainerStatus").value(true))
                .andExpect(jsonPath("$.years[0].year").value(year))
                .andExpect(jsonPath("$.years[0].months[0].month").value(month))
                .andExpect(jsonPath("$.years[0].months[0].trainingSummaryDuration").value(120));
    }

    @Test
    void shouldReturnTrainerWorkloadWithoutOptionalParams() throws Exception {
        // given
        String trainerUsername = "john.doe";
        String transactionId = "tx-456";

        TrainerWorkloadResponse response =
                TrainerWorkloadResponse.builder()
                        .trainerUsername(trainerUsername)
                        .trainerFirstName("John")
                        .trainerLastName("Doe")
                        .trainerStatus(true)
                        .years(List.of())
                        .build();

        Mockito.when(workloadService.getTrainerWorkload(
                        trainerUsername, null, null, transactionId))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/trainers/workload/{trainerUsername}", trainerUsername)
                        .header("X-Transaction-Id", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value(trainerUsername))
                .andExpect(jsonPath("$.years").isArray());
    }

    @Test
    void shouldUseUnknownTransactionIdWhenHeaderIsMissing() throws Exception {
        // given
        String trainerUsername = "john.doe";

        TrainerWorkloadResponse response =
                TrainerWorkloadResponse.builder()
                        .trainerUsername(trainerUsername)
                        .years(List.of())
                        .build();

        Mockito.when(workloadService.getTrainerWorkload(
                        trainerUsername, null, null, "UNKNOWN"))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/trainers/workload/{trainerUsername}", trainerUsername))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value(trainerUsername));
    }
}