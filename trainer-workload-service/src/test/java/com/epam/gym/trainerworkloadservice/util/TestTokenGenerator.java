package com.epam.gym.trainerworkloadservice.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TestTokenGenerator {

    @Value("${test.auth.base-url:http://localhost:8080}")
    private String authBaseUrl;

    @Value("${test.auth.username:Admin.User}")
    private String testUsername;

    @Value("${test.auth.password:Admin123}")
    private String testPassword;

    private final RestTemplate restTemplate = new RestTemplate();

    // Cache - har safar login qilmaslik uchun
    private String cachedToken;

    // ========== PUBLIC METHODS ==========

    public String getToken() {
        if (cachedToken == null || cachedToken.isBlank()) {
            cachedToken = fetchToken();
        }
        return cachedToken;
    }

    public void invalidateToken() {
        cachedToken = null;
        log.info("🗑️ Token cache tozalandi");
    }

    // ========== PRIVATE METHODS ==========

    private String fetchToken() {
        // 1. Config qiymatlarini tekshirish
        validateConfig();

        String loginUrl = authBaseUrl + "/api/auth/login";

        log.info("🔑 Login urinish:");
        log.info("   URL      = [{}]", loginUrl);
        log.info("   Username = [{}]", testUsername);
        log.info("   Password = [{}]", "*".repeat(testPassword.length()));

        // 2. Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // 3. Request body
        Map<String, String> loginBody = Map.of(
                "username", testUsername,
                "password", testPassword
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(loginBody, headers);

        // 4. Login qilish
        try {
            ResponseEntity<LoginResponseDto> response = restTemplate.exchange(
                    loginUrl,
                    HttpMethod.POST,
                    request,
                    LoginResponseDto.class
            );

            return handleLoginResponse(response);

        } catch (HttpClientErrorException e) {
            handleHttpError(e);
            throw new RuntimeException("Unreachable");

        } catch (Exception e) {
            log.error("❌ Kutilmagan xatolik: {}", e.getMessage());
            throw new RuntimeException(
                    "Main gym-service bilan bog'lanib bo'lmadi!"
                            + " URL: " + authBaseUrl
                            + " | Xato: " + e.getMessage(), e
            );
        }
    }

    private void validateConfig() {
        log.debug("🔧 Config tekshirilmoqda...");
        log.debug("   authBaseUrl  = [{}]", authBaseUrl);
        log.debug("   testUsername = [{}]", testUsername);
        log.debug("   testPassword = [{}]", testPassword == null ? "null" : "***");

        if (authBaseUrl == null || authBaseUrl.isBlank()) {
            throw new RuntimeException(
                    "❌ test.auth.base-url bo'sh! "
                            + "application-stg.yml da sozlang."
            );
        }

        if (testUsername == null || testUsername.isBlank()) {
            throw new RuntimeException(
                    "❌ test.auth.username bo'sh! "
                            + "application-stg.yml da sozlang. "
                            + "Masalan: test.auth.username=Admin.User"
            );
        }

        if (testPassword == null || testPassword.isBlank()) {
            throw new RuntimeException(
                    "❌ test.auth.password bo'sh! "
                            + "application-stg.yml da sozlang. "
                            + "Masalan: test.auth.password=Admin123"
            );
        }
    }

    private String handleLoginResponse(ResponseEntity<LoginResponseDto> response) {
        if (response == null) {
            throw new RuntimeException("❌ Response null qaytdi!");
        }

        log.debug("📥 Response status: {}", response.getStatusCode());

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException(
                    "❌ Login muvaffaqiyatsiz! Status: " + response.getStatusCode()
            );
        }

        LoginResponseDto body = response.getBody();

        if (body == null) {
            throw new RuntimeException("❌ Response body null!");
        }

        if (body.getAccessToken() == null || body.getAccessToken().isBlank()) {
            throw new RuntimeException("❌ accessToken bo'sh qaytdi!");
        }

        log.info("✅ Token muvaffaqiyatli olindi!");
        log.info("   Username : {}", body.getUsername());
        log.info("   Token    : {}...{}",
                body.getAccessToken().substring(0, Math.min(20, body.getAccessToken().length())),
                body.getAccessToken().substring(Math.max(0, body.getAccessToken().length() - 10))
        );

        return body.getAccessToken();
    }

    private void handleHttpError(HttpClientErrorException e) {
        String status = e.getStatusCode().toString();
        String responseBody = e.getResponseBodyAsString();

        log.error("❌ HTTP xatolik: {}", status);
        log.error("❌ Response body: {}", responseBody);

        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            throw new RuntimeException(
                    "❌ 401 Unauthorized! "
                            + "Username yoki password noto'g'ri. "
                            + "Username: [" + testUsername + "] "
                            + "| Response: " + responseBody, e
            );
        }

        if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
            throw new RuntimeException(
                    "❌ 400 Bad Request! "
                            + "Login so'rovi noto'g'ri formatda. "
                            + "Username: [" + testUsername + "] "
                            + "Password: [" + (testPassword.isBlank() ? "BO'SH!" : "***") + "] "
                            + "| Response: " + responseBody, e
            );
        }

        if (e.getStatusCode() == HttpStatus.LOCKED) {
            throw new RuntimeException(
                    "❌ 423 Locked! "
                            + "Account bloklangan (ko'p marta noto'g'ri urinish). "
                            + "Biroz kuting va qayta urining. "
                            + "| Response: " + responseBody, e
            );
        }

        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new RuntimeException(
                    "❌ 404 Not Found! "
                            + "Login endpoint topilmadi: " + authBaseUrl + "/api/auth/login "
                            + "| Main service ishlab turibdimi?", e
            );
        }

        throw new RuntimeException(
                "❌ HTTP xatolik: " + status
                        + " | " + responseBody, e
        );
    }

    // ========== INNER DTO ==========

    @Data
    public static class LoginResponseDto {
        private String accessToken;
        private String tokenType;
        private String username;
    }
}