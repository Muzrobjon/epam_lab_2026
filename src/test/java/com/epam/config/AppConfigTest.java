package com.epam.gym.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    // Test the ObjectMapper bean from AppConfig
    @Test
    void objectMapper_ShouldRegisterJavaTimeModule() {
        // Create a Spring Application Context using the AppConfig class
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // Retrieve the ObjectMapper bean
        ObjectMapper mapper = context.getBean(ObjectMapper.class);

        // Ensure the ObjectMapper is not null
        assertNotNull(mapper);

        // Verify that the JavaTimeModule is registered
        assertTrue(mapper.getRegisteredModuleIds().contains(new JavaTimeModule().getTypeId()));

        // Close the context to release resources
        context.close();
    }

    @Test
    void objectMapper_ShouldHandleJavaTimeTypes() {
        // Create a Spring Application Context using the AppConfig class
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // Retrieve the ObjectMapper bean
        ObjectMapper mapper = context.getBean(ObjectMapper.class);

        // Ensure it can serialize Java Time types
        assertTrue(mapper.canSerialize(java.time.LocalDate.class));
        assertTrue(mapper.canSerialize(java.time.LocalDateTime.class));

        // Close the context to release resources
        context.close();
    }
}
