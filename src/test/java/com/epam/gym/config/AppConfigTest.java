package com.epam.gym.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(TestJpaConfig.class)
class AppConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded");
    }

    @Test
    void dataSourceBeanExists() {
        assertNotNull(dataSource, "DataSource bean should exist");
    }

    @Test
    void dataSourceConnectionWorks() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Should be able to get connection");
            assertFalse(connection.isClosed(), "Connection should be open");
        }
    }

    @Test
    void entityManagerFactoryBeanExists() {
        assertNotNull(entityManagerFactory, "EntityManagerFactory bean should exist");
        assertTrue(entityManagerFactory.isOpen(), "EntityManagerFactory should be open");
    }

    @Test
    void entityManagerExists() {
        assertNotNull(entityManager, "EntityManager should be injected");
    }

    @Test
    void transactionManagerBeanExists() {
        assertNotNull(transactionManager, "TransactionManager bean should exist");
    }

    @Test
    void allRequiredBeansExist() {
        assertTrue(applicationContext.containsBean("dataSource"),
                "DataSource bean should be registered");
        assertTrue(applicationContext.containsBean("entityManagerFactory"),
                "EntityManagerFactory bean should be registered");
        assertTrue(applicationContext.containsBean("transactionManager"),
                "TransactionManager bean should be registered");
    }
}