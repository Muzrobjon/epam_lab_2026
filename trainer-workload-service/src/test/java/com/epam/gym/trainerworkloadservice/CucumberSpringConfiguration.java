package com.epam.gym.trainerworkloadservice;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MongoDBContainer;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("stg")
@TestPropertySource(properties = {
        "test.auth.base-url=http://localhost:8080",
        "test.auth.username=Administrator.Gym",
        "test.auth.password=0EBqqVTuh2",
        "spring.data.mongodb.uri=mongodb://admin:admin123@localhost:27017/test-workload-db?authSource=admin",
        "spring.activemq.broker-url=tcp://localhost:61616",
        "spring.activemq.user=admin",
        "spring.activemq.password=admin",
        "spring.activemq.packages.trust-all=true",
        "eureka.client.enabled=false"
})
public class CucumberSpringConfiguration {

    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    static {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
}