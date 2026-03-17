package com.epam.gym.controller;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.facade.GymFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GymFacade gymFacade;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Login should succeed when trainee authentication succeeds")
    void loginShouldSucceedWhenTraineeAuthenticationSucceeds() throws Exception {
        doNothing().when(gymFacade).authenticateTrainee("john.doe", "password123");

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "john.doe")
                        .param("password", "password123"))
                .andExpect(status().isOk());

        verify(gymFacade).authenticateTrainee("john.doe", "password123");
        verify(gymFacade, never()).authenticateTrainer(anyString(), anyString());
    }

    @Test
    @DisplayName("Login should succeed when trainer authentication succeeds after trainee fails")
    void loginShouldSucceedWhenTrainerAuthenticationSucceeds() throws Exception {
        doThrow(new RuntimeException("Trainee not found"))
                .when(gymFacade).authenticateTrainee("jane.smith", "trainerPass");
        doNothing().when(gymFacade).authenticateTrainer("jane.smith", "trainerPass");

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "jane.smith")
                        .param("password", "trainerPass"))
                .andExpect(status().isOk());

        verify(gymFacade).authenticateTrainee("jane.smith", "trainerPass");
        verify(gymFacade).authenticateTrainer("jane.smith", "trainerPass");
    }

    @Test
    @DisplayName("Login should fail when both trainee and trainer authentication fail")
    void loginShouldFailWhenBothAuthenticationsFail() {
        String username = "unknown.user";
        String password = "wrongPass";

        doThrow(new RuntimeException("Trainee not found"))
                .when(gymFacade).authenticateTrainee(username, password);
        doThrow(new RuntimeException("Trainer not found"))
                .when(gymFacade).authenticateTrainer(username, password);

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/api/auth/login")
                    .param("username", username)
                    .param("password", password));
        });

        Throwable rootCause = findRootCause(exception);
        assertNotNull(rootCause, "Expected RuntimeException in exception chain");
        assertEquals("Trainer not found", rootCause.getMessage());

        verify(gymFacade).authenticateTrainee(username, password);
        verify(gymFacade).authenticateTrainer(username, password);
    }

    @Test
    @DisplayName("Change password should succeed for trainee")
    void changePasswordShouldSucceedForTrainee() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("john.doe");
        request.setOldPassword("oldPass123");
        request.setNewPassword("newPass456");

        doNothing().when(gymFacade).changeTraineePassword("john.doe", "oldPass123", "newPass456");

        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).changeTraineePassword("john.doe", "oldPass123", "newPass456");
        verify(gymFacade, never()).changeTrainerPassword(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Change password should succeed for trainer when trainee fails")
    void changePasswordShouldSucceedForTrainerWhenTraineeFails() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername("jane.smith");
        request.setOldPassword("oldTrainerPass");
        request.setNewPassword("newTrainerPass");

        doThrow(new RuntimeException("Trainee not found"))
                .when(gymFacade).changeTraineePassword("jane.smith", "oldTrainerPass", "newTrainerPass");
        doNothing().when(gymFacade).changeTrainerPassword("jane.smith", "oldTrainerPass", "newTrainerPass");

        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).changeTraineePassword("jane.smith", "oldTrainerPass", "newTrainerPass");
        verify(gymFacade).changeTrainerPassword("jane.smith", "oldTrainerPass", "newTrainerPass");
    }

    @Test
    @DisplayName("Change password should fail when both trainee and trainer password change fails")
    void changePasswordShouldFailWhenBothFail() {
        String username = "unknown.user";
        String oldPassword = "wrongOld";
        String newPassword = "newPass";

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(username);
        request.setOldPassword(oldPassword);
        request.setNewPassword(newPassword);

        doThrow(new RuntimeException("Trainee not found"))
                .when(gymFacade).changeTraineePassword(username, oldPassword, newPassword);
        doThrow(new RuntimeException("Trainer not found"))
                .when(gymFacade).changeTrainerPassword(username, oldPassword, newPassword);

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(put("/api/auth/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        });

        Throwable rootCause = findRootCause(exception);
        assertNotNull(rootCause, "Expected RuntimeException in exception chain");
        assertEquals("Trainer not found", rootCause.getMessage());

        verify(gymFacade).changeTraineePassword(username, oldPassword, newPassword);
        verify(gymFacade).changeTrainerPassword(username, oldPassword, newPassword);
    }

    @Test
    @DisplayName("Change password should return bad request when request body is invalid")
    void changePasswordShouldReturnBadRequestWhenInvalid() throws Exception {
        ChangePasswordRequest invalidRequest = new ChangePasswordRequest();

        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login should return bad request when parameters are missing")
    void loginShouldReturnBadRequestWhenParametersMissing() throws Exception {
        mockMvc.perform(get("/api/auth/login")
                        .param("username", "john.doe"))
                .andExpect(status().isBadRequest());
    }

    private Throwable findRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (RuntimeException.class.isInstance(current)) {
                return current;
            }
            current = current.getCause();
        }
        return null;
    }
}