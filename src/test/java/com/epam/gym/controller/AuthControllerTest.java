package com.epam.gym.controller;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.dto.request.ToggleActiveRequest;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String CHANGE_PASSWORD_URL = "/api/auth/change-password";
    private static final String TOGGLE_STATUS_URL = "/api/auth/users/{username}/status";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== LOGIN TESTS ====================

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() throws Exception {
            // given
            var request = createLoginRequest("john.doe", "password123");
            doNothing().when(userService).authenticate("john.doe", "password123");

            // when & then
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(userService, times(1)).authenticate("john.doe", "password123");
        }

        @Test
        @DisplayName("Should return 401 when credentials are invalid")
        void shouldReturn401_WhenCredentialsInvalid() throws Exception {
            // given
            var request = createLoginRequest("john.doe", "wrongPassword");
            doThrow(new AuthenticationException("Invalid username or password"))
                    .when(userService).authenticate("john.doe", "wrongPassword");

            // when & then
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(userService, times(1)).authenticate("john.doe", "wrongPassword");
        }

        @Test
        @DisplayName("Should return 401 when user not found")
        void shouldReturn401_WhenUserNotFound() throws Exception {
            // given
            var request = createLoginRequest("nonexistent", "password123");
            doThrow(new AuthenticationException("Invalid username or password"))
                    .when(userService).authenticate("nonexistent", "password123");

            // when & then
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when username is null")
        void shouldReturn400_WhenUsernameIsNull() throws Exception {
            // given
            var request = createLoginRequest(null, "password123");

            // when & then
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).authenticate(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when username is empty")
        void shouldReturn400_WhenUsernameIsEmpty() throws Exception {
            // given
            var request = createLoginRequest("", "password123");

            // when & then
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).authenticate(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when password is null")
        void shouldReturn400_WhenPasswordIsNull() throws Exception {
            // given
            var request = createLoginRequest("john.doe", null);

            // when & then
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).authenticate(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when password is empty")
        void shouldReturn400_WhenPasswordIsEmpty() throws Exception {
            // given
            var request = createLoginRequest("john.doe", "");

            // when & then
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).authenticate(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void shouldReturn400_WhenRequestBodyIsEmpty() throws Exception {
            // when & then
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).authenticate(anyString(), anyString());
        }

        private LoginRequest createLoginRequest(String username, String password) {
            var request = new LoginRequest();
            request.setUsername(username);
            request.setPassword(password);
            return request;
        }
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Nested
    @DisplayName("PUT /api/auth/change-password")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() throws Exception {
            // given
            var request = createChangePasswordRequest("john.doe", "oldPass123", "newPass456");
            doNothing().when(userService).changePassword("john.doe", "oldPass123", "newPass456");

            // when & then
            mockMvc.perform(put(CHANGE_PASSWORD_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(userService, times(1)).changePassword("john.doe", "oldPass123", "newPass456");
        }

        @Test
        @DisplayName("Should return 401 when old password is incorrect")
        void shouldReturn401_WhenOldPasswordIncorrect() throws Exception {
            // given
            var request = createChangePasswordRequest("john.doe", "wrongOldPass", "newPass456");
            doThrow(new AuthenticationException("Invalid username or password"))
                    .when(userService).changePassword("john.doe", "wrongOldPass", "newPass456");

            // when & then
            mockMvc.perform(put(CHANGE_PASSWORD_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404_WhenUserNotFound() throws Exception {
            // given
            var request = createChangePasswordRequest("nonexistent", "oldPass123", "newPass456");
            doThrow(new NotFoundException("User not found: nonexistent"))
                    .when(userService).changePassword("nonexistent", "oldPass123", "newPass456");

            // when & then
            mockMvc.perform(put(CHANGE_PASSWORD_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when username is null")
        void shouldReturn400_WhenUsernameIsNull() throws Exception {
            // given
            var request = createChangePasswordRequest(null, "oldPass123", "newPass456");

            // when & then
            mockMvc.perform(put(CHANGE_PASSWORD_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).changePassword(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when old password is null")
        void shouldReturn400_WhenOldPasswordIsNull() throws Exception {
            // given
            var request = createChangePasswordRequest("john.doe", null, "newPass456");

            // when & then
            mockMvc.perform(put(CHANGE_PASSWORD_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).changePassword(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when new password is null")
        void shouldReturn400_WhenNewPasswordIsNull() throws Exception {
            // given
            var request = createChangePasswordRequest("john.doe", "oldPass123", null);

            // when & then
            mockMvc.perform(put(CHANGE_PASSWORD_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).changePassword(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when all fields are empty")
        void shouldReturn400_WhenAllFieldsAreEmpty() throws Exception {
            // given
            var request = createChangePasswordRequest("", "", "");

            // when & then
            mockMvc.perform(put(CHANGE_PASSWORD_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).changePassword(anyString(), anyString(), anyString());
        }

        private ChangePasswordRequest createChangePasswordRequest(String username, String oldPassword, String newPassword) {
            var request = new ChangePasswordRequest();
            request.setUsername(username);
            request.setOldPassword(oldPassword);
            request.setNewPassword(newPassword);
            return request;
        }
    }

    // ==================== TOGGLE STATUS TESTS ====================

    @Nested
    @DisplayName("PATCH /api/auth/users/{username}/status")
    class ToggleUserStatusTests {

        @Test
        @DisplayName("Should activate user successfully")
        void shouldActivateUserSuccessfully() throws Exception {
            // given
            var username = "john.doe";
            var request = createToggleActiveRequest(username, "password123", true);
            doNothing().when(userService).setActiveStatus(username, "password123", true);

            // when & then
            mockMvc.perform(patch(TOGGLE_STATUS_URL, username)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(userService, times(1)).setActiveStatus(username, "password123", true);
        }

        @Test
        @DisplayName("Should deactivate user successfully")
        void shouldDeactivateUserSuccessfully() throws Exception {
            // given
            var username = "john.doe";
            var request = createToggleActiveRequest(username, "password123", false);
            doNothing().when(userService).setActiveStatus(username, "password123", false);

            // when & then
            mockMvc.perform(patch(TOGGLE_STATUS_URL, username)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(userService, times(1)).setActiveStatus(username, "password123", false);
        }

        @Test
        @DisplayName("Should return 400 when username in path does not match request body")
        void shouldReturn400_WhenUsernameMismatch() throws Exception {
            // given
            var pathUsername = "john.doe";
            var request = createToggleActiveRequest("jane.doe", "password123", true);

            // when & then
            mockMvc.perform(patch(TOGGLE_STATUS_URL, pathUsername)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).setActiveStatus(anyString(), anyString(), anyBoolean());
        }

        @Test
        @DisplayName("Should return 401 when password is incorrect")
        void shouldReturn401_WhenPasswordIncorrect() throws Exception {
            // given
            var username = "john.doe";
            var request = createToggleActiveRequest(username, "wrongPassword", true);
            doThrow(new AuthenticationException("Invalid username or password"))
                    .when(userService).setActiveStatus(username, "wrongPassword", true);

            // when & then
            mockMvc.perform(patch(TOGGLE_STATUS_URL, username)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404_WhenUserNotFound() throws Exception {
            // given
            var username = "nonexistent";
            var request = createToggleActiveRequest(username, "password123", true);
            doThrow(new NotFoundException("User not found: " + username))
                    .when(userService).setActiveStatus(username, "password123", true);

            // when & then
            mockMvc.perform(patch(TOGGLE_STATUS_URL, username)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when username in request body is null")
        void shouldReturn400_WhenUsernameInBodyIsNull() throws Exception {
            // given
            var pathUsername = "john.doe";
            var request = createToggleActiveRequest(null, "password123", true);

            // when & then
            mockMvc.perform(patch(TOGGLE_STATUS_URL, pathUsername)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).setActiveStatus(anyString(), anyString(), anyBoolean());
        }

        @Test
        @DisplayName("Should return 400 when password is null")
        void shouldReturn400_WhenPasswordIsNull() throws Exception {
            // given
            var username = "john.doe";
            var request = createToggleActiveRequest(username, null, true);

            // when & then
            mockMvc.perform(patch(TOGGLE_STATUS_URL, username)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).setActiveStatus(anyString(), anyString(), anyBoolean());
        }

        @Test
        @DisplayName("Should return 400 when isActive is null")
        void shouldReturn400_WhenIsActiveIsNull() throws Exception {
            // given
            var username = "john.doe";
            var request = createToggleActiveRequest(username, "password123", null);

            // when & then
            mockMvc.perform(patch(TOGGLE_STATUS_URL, username)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).setActiveStatus(anyString(), anyString(), anyBoolean());
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void shouldReturn400_WhenRequestBodyIsEmpty() throws Exception {
            // given
            var username = "john.doe";

            // when & then
            mockMvc.perform(patch(TOGGLE_STATUS_URL, username)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userService, never()).setActiveStatus(anyString(), anyString(), anyBoolean());
        }

        private ToggleActiveRequest createToggleActiveRequest(String username, String password, Boolean isActive) {
            var request = new ToggleActiveRequest();
            request.setUsername(username);
            request.setPassword(password);
            request.setIsActive(isActive);
            return request;
        }
    }
}