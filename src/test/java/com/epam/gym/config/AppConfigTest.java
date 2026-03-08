package com.epam.gym.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppConfigTest {

    @Mock
    private Environment env;

    @InjectMocks
    private AppConfig appConfig;

    @Test
    void dataSource_ShouldCreateDriverManagerDataSourceWithCorrectProperties() {
        // Given
        when(env.getProperty("jdbc.driver")).thenReturn("org.postgresql.Driver");
        when(env.getProperty("jdbc.url")).thenReturn("jdbc:postgresql://localhost:5432/gym");
        when(env.getProperty("jdbc.username")).thenReturn("admin");
        when(env.getProperty("jdbc.password")).thenReturn("secret");

        // When
        DataSource dataSource = appConfig.dataSource();

        // Then
        assertNotNull(dataSource);
        assertInstanceOf(DriverManagerDataSource.class, dataSource);
        DriverManagerDataSource driverManagerDataSource = (DriverManagerDataSource) dataSource;
        assertEquals("jdbc:postgresql://localhost:5432/gym", driverManagerDataSource.getUrl());
        assertEquals("admin", driverManagerDataSource.getUsername());

        verify(env).getProperty("jdbc.driver");
        verify(env).getProperty("jdbc.url");
        verify(env).getProperty("jdbc.username");
        verify(env).getProperty("jdbc.password");
    }

    @Test
    void entityManagerFactory_ShouldCreateLocalContainerEntityManagerFactoryBean() {
        // Given
        when(env.getProperty("hibernate.dialect")).thenReturn("org.hibernate.dialect.PostgreSQLDialect");
        when(env.getProperty("hibernate.hbm2ddl.auto")).thenReturn("update");
        when(env.getProperty("hibernate.show_sql")).thenReturn("true");
        when(env.getProperty("hibernate.format_sql")).thenReturn("true");
        when(env.getProperty("hibernate.use_sql_comments")).thenReturn("true");

        // Need to mock data source properties for the internal call
        when(env.getProperty("jdbc.driver")).thenReturn("org.postgresql.Driver");
        when(env.getProperty("jdbc.url")).thenReturn("jdbc:postgresql://localhost:5432/gym");
        when(env.getProperty("jdbc.username")).thenReturn("admin");
        when(env.getProperty("jdbc.password")).thenReturn("secret");

        // When
        LocalContainerEntityManagerFactoryBean emf = appConfig.entityManagerFactory();

        // Then
        assertNotNull(emf);
        assertNotNull(emf.getDataSource());
        assertNotNull(emf.getJpaVendorAdapter());
        assertTrue(emf.getJpaPropertyMap().containsKey("hibernate.dialect"));
        assertTrue(emf.getJpaPropertyMap().containsKey("hibernate.query.mutation_strategy"));
        // Cannot verify packagesToScan - no getter available, but we can verify the bean was created successfully
    }

    @Test
    void transactionManager_ShouldCreateJpaTransactionManagerWithEntityManagerFactory() {
        // Given
        EntityManagerFactory emf = mock(EntityManagerFactory.class);

        // When
        JpaTransactionManager transactionManager =
                (JpaTransactionManager) appConfig.transactionManager(emf);

        // Then
        assertNotNull(transactionManager);
        assertEquals(emf, transactionManager.getEntityManagerFactory());
    }

    @Test
    void validator_ShouldCreateDefaultValidator() {
        // When
        Validator validator = appConfig.validator();

        // Then
        assertNotNull(validator);
    }

    @Test
    void hibernateProperties_ShouldContainAllRequiredProperties() {
        // Given
        when(env.getProperty("hibernate.dialect")).thenReturn("org.hibernate.dialect.PostgreSQLDialect");
        when(env.getProperty("hibernate.hbm2ddl.auto")).thenReturn("validate");
        when(env.getProperty("hibernate.show_sql")).thenReturn("false");
        when(env.getProperty("hibernate.format_sql")).thenReturn("false");
        when(env.getProperty("hibernate.use_sql_comments")).thenReturn("false");

        // Need to mock data source properties for the internal call
        when(env.getProperty("jdbc.driver")).thenReturn("org.postgresql.Driver");
        when(env.getProperty("jdbc.url")).thenReturn("jdbc:postgresql://localhost:5432/gym");
        when(env.getProperty("jdbc.username")).thenReturn("admin");
        when(env.getProperty("jdbc.password")).thenReturn("secret");

        // When
        LocalContainerEntityManagerFactoryBean emf = appConfig.entityManagerFactory();
        Properties jpaProperties = new Properties();
        jpaProperties.putAll(emf.getJpaPropertyMap());

        // Then
        assertEquals("org.hibernate.dialect.PostgreSQLDialect",
                jpaProperties.getProperty("hibernate.dialect"));
        assertEquals("validate", jpaProperties.getProperty("hibernate.hbm2ddl.auto"));
        assertEquals("false", jpaProperties.getProperty("hibernate.show_sql"));
        assertEquals("false", jpaProperties.getProperty("hibernate.format_sql"));
        assertEquals("false", jpaProperties.getProperty("hibernate.use_sql_comments"));
        assertEquals("org.hibernate.query.sqm.mutation.internal.inline.InlineMutationStrategy",
                jpaProperties.getProperty("hibernate.query.mutation_strategy"));
    }

    @Test
    void hibernateProperties_ShouldIncludeMutationStrategyForCTEIssue() {
        // Given - mock all required properties
        when(env.getProperty("jdbc.driver")).thenReturn("org.postgresql.Driver");
        when(env.getProperty("jdbc.url")).thenReturn("jdbc:postgresql://localhost:5432/gym");
        when(env.getProperty("jdbc.username")).thenReturn("admin");
        when(env.getProperty("jdbc.password")).thenReturn("secret");
        when(env.getProperty("hibernate.dialect")).thenReturn("org.hibernate.dialect.PostgreSQLDialect");
        when(env.getProperty("hibernate.hbm2ddl.auto")).thenReturn("update");
        when(env.getProperty("hibernate.show_sql")).thenReturn("true");
        when(env.getProperty("hibernate.format_sql")).thenReturn("true");
        when(env.getProperty("hibernate.use_sql_comments")).thenReturn("true");

        // When
        LocalContainerEntityManagerFactoryBean emf = appConfig.entityManagerFactory();

        // Then
        assertTrue(emf.getJpaPropertyMap().containsKey("hibernate.query.mutation_strategy"));
        assertEquals("org.hibernate.query.sqm.mutation.internal.inline.InlineMutationStrategy",
                emf.getJpaPropertyMap().get("hibernate.query.mutation_strategy"));
    }
}