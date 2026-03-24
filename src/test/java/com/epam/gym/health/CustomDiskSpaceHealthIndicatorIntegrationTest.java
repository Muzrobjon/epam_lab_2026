package com.epam.gym.health;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "management.endpoints.web.exposure.include=health",
        "management.endpoint.health.show-details=always"
})
@DisplayName("CustomDiskSpaceHealthIndicator Integration Tests")
class CustomDiskSpaceHealthIndicatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should include customDiskSpace in health endpoint")
    void shouldIncludeCustomDiskSpaceInHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.customDiskSpace").exists())
                .andExpect(jsonPath("$.components.customDiskSpace.status").exists())
                .andExpect(jsonPath("$.components.customDiskSpace.details.free").exists());
    }
}