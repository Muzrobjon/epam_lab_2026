package com.epam.gym.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void contextLoads_andAllBeansCreated() throws JsonProcessingException {
        // Create Spring context with your AppConfig
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {

            // =========================
            // Test DataSource (PostgreSQL)
            // =========================
            DataSource dataSource = context.getBean(DataSource.class);
            assertNotNull(dataSource);

            // =========================
            // Test ObjectMapper
            // =========================
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
            assertNotNull(objectMapper);

            // Check JavaTimeModule works
            String json = objectMapper.writeValueAsString(LocalDate.of(2026, 3, 2));
            assertEquals("\"2026-03-02\"", json);

            // =========================
            // Test EntityManagerFactoryBean
            // =========================
            LocalContainerEntityManagerFactoryBean emfBean =
                    context.getBean(LocalContainerEntityManagerFactoryBean.class);
            assertNotNull(emfBean);

            // =========================
            // Test EntityManagerFactory
            // =========================
            EntityManagerFactory emf = context.getBean(EntityManagerFactory.class);
            assertNotNull(emf);

            // =========================
            // Test TransactionManager
            // =========================
            PlatformTransactionManager txManager = context.getBean(PlatformTransactionManager.class);
            assertNotNull(txManager);

            // =========================
            // Test Validator
            // =========================
            Validator validator = context.getBean(Validator.class);
            assertNotNull(validator);
        }
    }
}