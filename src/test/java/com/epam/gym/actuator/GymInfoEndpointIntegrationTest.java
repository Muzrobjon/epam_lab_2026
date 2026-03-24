package com.epam.gym.actuator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "management.endpoints.web.exposure.include=health,info,gym",
        "management.endpoint.gym.enabled=true"
})
@DisplayName("GymInfoEndpoint Integration Tests")
class GymInfoEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return gym info from actuator endpoint")
    void shouldReturnGymInfo() throws Exception {
        mockMvc.perform(get("/actuator/gym"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("Gym CRM System"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.statistics.trainees").isNumber())
                .andExpect(jsonPath("$.statistics.trainers").isNumber())
                .andExpect(jsonPath("$.statistics.trainings").isNumber());
    }

    @Test
    @DisplayName("Should return trainee details from actuator endpoint")
    void shouldReturnTraineeDetails() throws Exception {
        mockMvc.perform(get("/actuator/gym/trainees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entity").value("Trainee"))
                .andExpect(jsonPath("$.count").isNumber());
    }

    @Test
    @DisplayName("Should return trainer details from actuator endpoint")
    void shouldReturnTrainerDetails() throws Exception {
        mockMvc.perform(get("/actuator/gym/trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entity").value("Trainer"))
                .andExpect(jsonPath("$.count").isNumber());
    }

    @Test
    @DisplayName("Should return training details from actuator endpoint")
    void shouldReturnTrainingDetails() throws Exception {
        mockMvc.perform(get("/actuator/gym/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entity").value("Training"))
                .andExpect(jsonPath("$.count").isNumber());
    }

    @Test
    @DisplayName("Should return error for unknown entity")
    void shouldReturnErrorForUnknownEntity() throws Exception {
        mockMvc.perform(get("/actuator/gym/unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Unknown: unknown"));
    }
}