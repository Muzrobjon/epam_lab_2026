package com.epam.gym.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(AppConfig.class)
class AppConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private Validator validator;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded");
    }

    @Test
    void dataSourceBeanExists() {
        assertNotNull(dataSource, "DataSource bean should exist");
        assertInstanceOf(DriverManagerDataSource.class, dataSource, "DataSource should be instance of DriverManagerDataSource");
    }

    @Test
    void entityManagerFactoryBeanExists() {
        assertNotNull(entityManagerFactory, "EntityManagerFactory bean should exist");
    }

    @Test
    void transactionManagerBeanExists() {
        assertNotNull(transactionManager, "TransactionManager bean should exist");
        assertInstanceOf(JpaTransactionManager.class, transactionManager, "TransactionManager should be instance of JpaTransactionManager");
    }

    @Test
    void validatorBeanExists() {
        assertNotNull(validator, "Validator bean should exist");
    }

    @Test
    void dataSourceConfiguration() {
        DriverManagerDataSource ds = (DriverManagerDataSource) dataSource;

        assertNotNull(ds.getUrl(), "Database URL should be configured");
        assertNotNull(ds.getUsername(), "Database username should be configured");
        assertNotNull(ds.getPassword(), "Database password should be configured");
    }

    @Test
    void entityManagerFactoryConfiguration() {
        LocalContainerEntityManagerFactoryBean emfBean = applicationContext
                .getBean(LocalContainerEntityManagerFactoryBean.class);

        assertNotNull(emfBean.getDataSource(), "EntityManagerFactory should have DataSource");
        assertNotNull(emfBean.getJpaVendorAdapter(), "JPA vendor adapter should be configured");
    }

    @Test
    void transactionManagerConfiguration() {
        JpaTransactionManager txManager = (JpaTransactionManager) transactionManager;

        assertNotNull(txManager.getEntityManagerFactory(),
                "TransactionManager should have EntityManagerFactory");
    }
}