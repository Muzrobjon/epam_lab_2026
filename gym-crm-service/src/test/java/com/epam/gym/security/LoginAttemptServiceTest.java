package com.epam.gym.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private final String USERNAME = "test_user";
    private final int MAX_ATTEMPTS = 3;

    @BeforeEach
    void setUp() {
        // Injecting @Value fields manually since we aren't using a full Spring Context
        ReflectionTestUtils.setField(loginAttemptService, "maxAttempts", MAX_ATTEMPTS);
        ReflectionTestUtils.setField(loginAttemptService, "blockDurationMinutes", 5);
    }

    @Test
    @DisplayName("Should increment attempts and block user after max attempts reached")
    void loginFailed_ShouldBlockUser_WhenMaxAttemptsReached() {
        // Initially not blocked
        assertFalse(loginAttemptService.isBlocked(USERNAME));
        assertEquals(MAX_ATTEMPTS, loginAttemptService.getRemainingAttempts(USERNAME));

        // 1st failure
        loginAttemptService.loginFailed(USERNAME);
        assertEquals(2, loginAttemptService.getRemainingAttempts(USERNAME));
        assertFalse(loginAttemptService.isBlocked(USERNAME));

        // 2nd failure
        loginAttemptService.loginFailed(USERNAME);
        assertEquals(1, loginAttemptService.getRemainingAttempts(USERNAME));
        assertFalse(loginAttemptService.isBlocked(USERNAME));

        // 3rd failure (Limit reached)
        loginAttemptService.loginFailed(USERNAME);
        assertEquals(0, loginAttemptService.getRemainingAttempts(USERNAME));
        assertTrue(loginAttemptService.isBlocked(USERNAME));
    }

    @Test
    @DisplayName("Should clear attempts when login succeeds")
    void loginSucceeded_ShouldResetAttempts() {
        // GIVEN: User failed twice
        loginAttemptService.loginFailed(USERNAME);
        loginAttemptService.loginFailed(USERNAME);
        assertEquals(1, loginAttemptService.getRemainingAttempts(USERNAME));

        // WHEN: Login succeeds
        loginAttemptService.loginSucceeded(USERNAME);

        // THEN: Attempts should be reset
        assertEquals(MAX_ATTEMPTS, loginAttemptService.getRemainingAttempts(USERNAME));
        assertFalse(loginAttemptService.isBlocked(USERNAME));
    }

    @Test
    @DisplayName("Remaining attempts should not go below zero")
    void getRemainingAttempts_ShouldNotBeNegative() {
        // GIVEN: Fail 5 times (where max is 3)
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed(USERNAME);
        }

        // THEN
        assertEquals(0, loginAttemptService.getRemainingAttempts(USERNAME));
        assertTrue(loginAttemptService.isBlocked(USERNAME));
    }

    @Test
    @DisplayName("Should return the configured block duration")
    void getBlockDurationMinutes_ShouldReturnCorrectValue() {
        assertEquals(5, loginAttemptService.getBlockDurationMinutes());
    }
}