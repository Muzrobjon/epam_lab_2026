package com.epam.gym.controller;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.dto.request.ToggleActiveRequest;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "password123";
    private static final String NEW_PASSWORD = "newPassword456";

    // Test exception handler for standalone MockMvc setup
    @RestControllerAdvice
    static class TestExceptionHandler {

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<String> handleAuthenticationException(AuthenticationException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<String> handleNotFoundException(NotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new TestExceptionHandler()) // Add exception handler
                .build();
        objectMapper = new ObjectMapper();
    }

    // ==================== LOGIN TESTS ====================

    @Nested
    @DisplayName("Login Endpoint Tests")
    class LoginTests {

        @Test
        @DisplayName("Should return 200 OK when login is successful")
        void login_WithValidCredentials_ReturnsOk() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);

            doNothing().when(userService).authenticate(USERNAME, PASSWORD);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(userService).authenticate(USERNAME, PASSWORD);
        }

        @Test
        @DisplayName("Should return 401 when authentication fails")
        void login_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername(USERNAME);
            request.setPassword("wrongPassword");

            doThrow(new AuthenticationException("Invalid username or password"))
                    .when(userService).authenticate(USERNAME, "wrongPassword");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(userService).authenticate(USERNAME, "wrongPassword");
        }
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Nested
    @DisplayName("Change Password Endpoint Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should return 200 OK when password change is successful")
        void changePassword_WithValidRequest_ReturnsOk() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setUsername(USERNAME);
            request.setOldPassword(PASSWORD);
            request.setNewPassword(NEW_PASSWORD);

            doNothing().when(userService).changePassword(USERNAME, PASSWORD, NEW_PASSWORD);

            mockMvc.perform(put("/api/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(userService).changePassword(USERNAME, PASSWORD, NEW_PASSWORD);
        }

        @Test
        @DisplayName("Should return 401 when old password is incorrect")
        void changePassword_WithWrongOldPassword_ReturnsUnauthorized() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setUsername(USERNAME);
            request.setOldPassword("wrongOldPassword");
            request.setNewPassword(NEW_PASSWORD);

            doThrow(new AuthenticationException("Invalid username or password"))
                    .when(userService).changePassword(USERNAME, "wrongOldPassword", NEW_PASSWORD);

            mockMvc.perform(put("/api/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void changePassword_UserNotFound_ReturnsNotFound() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setUsername("nonexistent");
            request.setOldPassword(PASSWORD);
            request.setNewPassword(NEW_PASSWORD);

            doThrow(new NotFoundException("User not found"))
                    .when(userService).changePassword("nonexistent", PASSWORD, NEW_PASSWORD);

            mockMvc.perform(put("/api/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== TOGGLE STATUS TESTS ====================

    @Nested
    @DisplayName("Toggle User Status Endpoint Tests")
    class ToggleUserStatusTests {

        @Test
        @DisplayName("Should return 200 OK when activating user")
        void toggleUserStatus_ActivateUser_ReturnsOk() throws Exception {
            ToggleActiveRequest request = new ToggleActiveRequest();
            request.setUsername(USERNAME);  // <-- FIX: Set username field
            request.setIsActive(true);

            doNothing().when(userService).isAuthenticated(USERNAME);
            doNothing().when(userService).setActiveStatus(USERNAME, true);

            mockMvc.perform(patch("/api/auth/users/{username}/status", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(userService).isAuthenticated(USERNAME);
            verify(userService).setActiveStatus(USERNAME, true);
        }

        @Test
        @DisplayName("Should return 200 OK when deactivating user")
        void toggleUserStatus_DeactivateUser_ReturnsOk() throws Exception {
            ToggleActiveRequest request = new ToggleActiveRequest();
            request.setUsername(USERNAME);  // <-- FIX: Set username field
            request.setIsActive(false);

            doNothing().when(userService).isAuthenticated(USERNAME);
            doNothing().when(userService).setActiveStatus(USERNAME, false);

            mockMvc.perform(patch("/api/auth/users/{username}/status", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(userService).isAuthenticated(USERNAME);
            verify(userService).setActiveStatus(USERNAME, false);
        }

        @Test
        @DisplayName("Should return 401 when user not authenticated")
        void toggleUserStatus_UserNotAuthenticated_ReturnsUnauthorized() throws Exception {
            ToggleActiveRequest request = new ToggleActiveRequest();
            request.setUsername(USERNAME);  // <-- FIX: Set username field
            request.setIsActive(true);

            doThrow(new AuthenticationException("User is not authenticated"))
                    .when(userService).isAuthenticated(USERNAME);

            mockMvc.perform(patch("/api/auth/users/{username}/status", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(userService).isAuthenticated(USERNAME);
            verify(userService, never()).setActiveStatus(anyString(), anyBoolean());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void toggleUserStatus_UserNotFound_ReturnsNotFound() throws Exception {
            ToggleActiveRequest request = new ToggleActiveRequest();
            request.setUsername(USERNAME);  // <-- FIX: Set username field
            request.setIsActive(true);

            doNothing().when(userService).isAuthenticated(USERNAME);
            doThrow(new NotFoundException("User not found"))
                    .when(userService).setActiveStatus(USERNAME, true);

            mockMvc.perform(patch("/api/auth/users/{username}/status", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}