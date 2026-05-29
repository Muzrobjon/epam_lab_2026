package com.epam.gym.trainerworkloadservice.controller;

import com.epam.gym.trainerworkloadservice.config.service.JwtProvider;
import com.epam.gym.trainerworkloadservice.dto.response.TrainerWorkloadResponse;
import com.epam.gym.trainerworkloadservice.exception.GlobalExceptionHandler;
import com.epam.gym.trainerworkloadservice.service.TrainerWorkloadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({TrainerWorkloadController.class, GlobalExceptionHandler.class})
@WithMockUser
class TrainerWorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainerWorkloadService workloadService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void getTrainerWorkload_shouldReturn200_whenFound() throws Exception {
        TrainerWorkloadResponse.MonthSummary month = TrainerWorkloadResponse.MonthSummary.builder()
                .month(6)
                .trainingSummaryDuration(90L)
                .build();
        TrainerWorkloadResponse.YearSummary year = TrainerWorkloadResponse.YearSummary.builder()
                .year(2024)
                .months(List.of(month))
                .build();
        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .trainerStatus(true)
                .years(List.of(year))
                .build();

        when(workloadService.getTrainerWorkload(eq("john.doe"), eq(2024), eq(6), anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/trainer-workload/john.doe")
                        .param("year", "2024")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("john.doe"))
                .andExpect(jsonPath("$.years[0].year").value(2024))
                .andExpect(jsonPath("$.years[0].months[0].month").value(6))
                .andExpect(jsonPath("$.years[0].months[0].trainingSummaryDuration").value(90));

        verify(workloadService).getTrainerWorkload(eq("john.doe"), eq(2024), eq(6), anyString());
    }

    @Test
    void getTrainerWorkload_shouldReturn200_withoutFilters() throws Exception {
        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername("john.doe")
                .years(List.of())
                .build();

        when(workloadService.getTrainerWorkload(eq("john.doe"), isNull(), isNull(), anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/trainer-workload/john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("john.doe"));
    }
}