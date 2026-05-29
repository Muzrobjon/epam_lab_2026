package com.epam.gym.trainerworkloadservice.component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
@Getter
@Setter
@Slf4j
public class WorkloadTestContext {

    private HttpResponse<String> lastHttpResponse;
    private String currentTrainerUsername;
    private String authToken;
    private String transactionId;
    private int serverPort;

    @Value("${test.auth.base-url:http://localhost:8080}")
    private String authBaseUrl;

    @Value("${test.auth.username:Administrator.Gym}")
    private String testUsername;

    @Value("${test.auth.password:0EBqqVTuh2}")
    private String testPassword;

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationWhichShouldBeLongEnough123456789}")
    private String jwtSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public void reset() {
        lastHttpResponse = null;
        currentTrainerUsername = null;
        authToken = null;
        transactionId = null;
        serverPort = 0;
    }

    public String getOrFetchToken() {
        if (authToken == null || authToken.isBlank()) {
            try {
                authToken = fetchTokenFromAuthService();
                log.info("JWT token obtained from auth service");
            } catch (ResourceAccessException e) {
                log.warn("Auth service unavailable at {}, generating local test token", authBaseUrl);
                authToken = generateLocalTestToken();
            } catch (Exception e) {
                log.error("Failed to fetch JWT token: {}, generating local test token", e.getMessage());
                authToken = generateLocalTestToken();
            }
        }
        return authToken;
    }

    private String fetchTokenFromAuthService() {
        String loginUrl = authBaseUrl + "/api/auth/login";
        log.info("Fetching JWT token from: {}", loginUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, String> body = Map.of(
                "username", testUsername,
                "password", testPassword
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);

        if (response.getBody() == null) {
            throw new RuntimeException("Login response body is null");
        }

        Object tokenObj = response.getBody().get("accessToken");
        if (tokenObj == null) {
            tokenObj = response.getBody().get("access_token");
        }
        if (tokenObj == null) {
            tokenObj = response.getBody().get("token");
        }

        if (tokenObj == null) {
            log.error("Response body: {}", response.getBody());
            throw new RuntimeException("No accessToken found in login response");
        }

        String token = tokenObj.toString();
        if (token.isBlank()) {
            throw new RuntimeException("Access token is empty");
        }

        return token;
    }

    private String generateLocalTestToken() {
        log.info("Generating local test JWT token with secret");

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(testUsername)
                .claim("role", "ADMIN")
                .claim("authorities", "ROLE_ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(key)
                .compact();
    }
}