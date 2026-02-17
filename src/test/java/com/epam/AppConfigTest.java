package com.epam;

import com.epam.gym.config.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    private final AppConfig appConfig = new AppConfig();

    @Test
    void propertyConfigurer_ShouldReturnNonNullConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = AppConfig.propertyConfigurer();
        assertNotNull(configurer);
    }

    @Test
    void objectMapper_ShouldRegisterJavaTimeModule() {
        ObjectMapper mapper = appConfig.objectMapper();

        assertNotNull(mapper);
        assertTrue(mapper.getRegisteredModuleIds().contains(new JavaTimeModule().getTypeId()));
    }

    @Test
    void objectMapper_ShouldHandleJavaTimeTypes() {
        ObjectMapper mapper = appConfig.objectMapper();

        assertTrue(mapper.canSerialize(java.time.LocalDate.class));
        assertTrue(mapper.canSerialize(java.time.LocalDateTime.class));
    }
}
