package com.epam.gym.controller;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.security.*;
import com.epam.gym.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private LoginAttemptService loginAttemptService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private LoginRequest loginRequest;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        userPrincipal = new UserPrincipal(
                1L, "testuser", "password123", "John", "Doe", true, Collections.emptyList()
        );
    }

    @Test
    void login_Success_ShouldReturnToken() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(loginAttemptService.isBlocked(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(jwtProvider.generateToken(authentication)).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(loginAttemptService).loginSucceeded("testuser");
    }

    @Test
    void login_WhenBlocked_ShouldReturnLockedStatus() throws Exception {
        when(loginAttemptService.isBlocked("testuser")).thenReturn(true);
        when(loginAttemptService.getBlockDurationMinutes()).thenReturn(30);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.error").value("Account is locked"))
                .andExpect(jsonPath("$.blockDurationMinutes").value(30));
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        when(loginAttemptService.isBlocked(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
        when(loginAttemptService.getRemainingAttempts("testuser")).thenReturn(2);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.remainingAttempts").value(2));

        verify(loginAttemptService).loginFailed("testuser");
    }

    @Test
    void logout_Success_ShouldBlacklistToken() throws Exception {
        String token = "valid-token";
        LocalDateTime now = LocalDateTime.now();
        when(jwtProvider.getExpirationFromToken(token)).thenReturn(now);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(tokenBlacklistService).blacklistToken(eq(token), any());
    }

    @Test
    void changePassword_Success_ShouldReturnOk() throws Exception {
        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setUsername("testuser");
        changeRequest.setOldPassword("oldPass");
        changeRequest.setNewPassword("newPass");

        doNothing().when(userService).changePassword(anyString(), anyString(), anyString());

        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    void changePassword_Failure_ShouldReturnBadRequest() throws Exception {
        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setUsername("testuser");
        changeRequest.setOldPassword("wrongOldPass");
        changeRequest.setNewPassword("newPass123");

        doThrow(new RuntimeException("Old password incorrect"))
                .when(userService).changePassword(any(), any(), any());

        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password change failed"))
                .andExpect(jsonPath("$.message").value("Old password incorrect"));
    }

    @Test
    void changePassword_MissingFields_ShouldReturnValidationError() throws Exception {
        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setUsername("testuser");


        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }
}