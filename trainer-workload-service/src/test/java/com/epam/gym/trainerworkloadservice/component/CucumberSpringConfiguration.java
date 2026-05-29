package com.epam.gym.trainerworkloadservice.component;

import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true"
        }
)
@ActiveProfiles("stg")
@Import(TestJmsConfig.class)
@ComponentScan(
        basePackages = "com.epam.gym.trainerworkloadservice",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.epam\\.gym\\.config\\..*"
        )
)
@TestPropertySource(properties = {
        "test.auth.base-url=http://localhost.151:8080",
        "test.auth.username=Administrator.Gym",
        "test.auth.password=0EBqqVTuh2",

        "spring.data.mongodb.uri=mongodb://admin:admin123@localhost:27017/test-workload-db?authSource=admin",
        "spring.data.mongodb.database=test-workload-db",

        "spring.activemq.broker-url=vm://embedded-broker?create=false",
        "spring.activemq.user=",
        "spring.activemq.password=",
        "spring.activemq.packages.trust-all=true",
        "spring.jms.listener.auto-startup=true",

        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",

        "jwt.secret=mySecretKeyForJWTTokenGenerationWhichShouldBeLongEnough123456789",
        "jwt.expiration=7200000",

        "app.jms.queue.trainer-workload=trainer.workload.queue",
        "app.jms.queue.trainer-workload-dlq=trainer.workload.dlq"
})
public class CucumberSpringConfiguration {

    @LocalServerPort
    private int port;

    @Autowired
    private WorkloadTestContext context;

    @Before
    public void setUp() {
        context.reset();
        context.setServerPort(port);
        System.out.println("=== Test server started on port: " + port + " ===");
    }
}