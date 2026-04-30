package com.epam.gym.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false"
})
class SecurityConfigTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void rootEndpointIsAccessibleWithoutAuthentication() {
        ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);
        assertThat(response.getStatusCode().value()).isNotEqualTo(401);
    }

    @Test
    void eurekaAppsEndpointIsAccessibleWithoutAuthentication() {
        ResponseEntity<String> response = restTemplate.getForEntity("/eureka/apps", String.class);
        assertThat(response.getStatusCode().value()).isNotEqualTo(401);
    }
}