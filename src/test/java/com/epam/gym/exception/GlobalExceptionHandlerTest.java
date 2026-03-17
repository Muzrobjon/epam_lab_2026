package com.epam.gym.exception;

import com.epam.gym.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    // Interceptor to add transactionId to requests
    static class TransactionIdInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                                 jakarta.servlet.http.HttpServletResponse response,
                                 Object handler) {
            request.setAttribute("transactionId", UUID.randomUUID().toString());
            return true;
        }
    }

    // Test controller that throws various exceptions
    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/not-found")
        public String throwNotFound() {
            throw new NotFoundException("Resource not found");
        }

        @GetMapping("/auth")
        public String throwAuthentication() {
            throw new AuthenticationException("Invalid credentials");
        }

        @GetMapping("/validation")
        public String throwValidation() {
            throw new ValidationException("Invalid data provided");
        }

        @GetMapping("/generic")
        public String throwGeneric() {
            throw new RuntimeException("Unexpected error");
        }
    }

    @BeforeEach
    void setUp() {
        // Build MockMvc with both controller, exception handler, AND transactionId interceptor
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(exceptionHandler)
                .addInterceptors(new TransactionIdInterceptor())
                .build();
    }

    // ==================== NotFoundException Tests ====================

    @Test
    @DisplayName("Should handle NotFoundException and return 404")
    void shouldHandleNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle NotFoundException directly via handler method")
    void shouldHandleNotFoundExceptionDirectly() {
        // Given
        when(request.getAttribute("transactionId")).thenReturn("txn-123");
        when(request.getRequestURI()).thenReturn("/api/resource/123");

        NotFoundException ex = new NotFoundException("User not found");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFoundException(ex, request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("txn-123", response.getBody().getTransactionId());
        assertEquals("/api/resource/123", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    // ==================== AuthenticationException Tests ====================

    @Test
    @DisplayName("Should handle AuthenticationException and return 401")
    void shouldHandleAuthenticationException() throws Exception {
        mockMvc.perform(get("/test/auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/test/auth"))
                .andExpect(jsonPath("$.transactionId").exists());
    }

    @Test
    @DisplayName("Should handle AuthenticationException directly via handler method")
    void shouldHandleAuthenticationExceptionDirectly() {
        // Given
        when(request.getAttribute("transactionId")).thenReturn("txn-456");
        when(request.getRequestURI()).thenReturn("/api/login");

        AuthenticationException ex = new AuthenticationException("Bad credentials");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad credentials", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
    }

    // ==================== ValidationException Tests ====================

    @Test
    @DisplayName("Should handle ValidationException and return 400")
    void shouldHandleValidationException() throws Exception {
        mockMvc.perform(get("/test/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid data provided"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/test/validation"))
                .andExpect(jsonPath("$.transactionId").exists());
    }

    @Test
    @DisplayName("Should handle ValidationException directly via handler method")
    void shouldHandleValidationExceptionDirectly() {
        // Given
        when(request.getAttribute("transactionId")).thenReturn("txn-789");
        when(request.getRequestURI()).thenReturn("/api/validate");

        ValidationException ex = new ValidationException("Field cannot be null");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Field cannot be null", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
    }

    // ==================== MethodArgumentNotValidException Tests ====================

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with field errors")
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        when(request.getAttribute("transactionId")).thenReturn("txn-abc");
        when(request.getRequestURI()).thenReturn("/api/users");

        // Create a mock binding result with field errors
        Object target = new Object();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "username", "Username is required"));
        bindingResult.addError(new FieldError("target", "email", "Email format is invalid"));

        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValid(ex, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Username is required"));
        assertTrue(response.getBody().getMessage().contains("Email format is invalid"));
        assertTrue(response.getBody().getMessage().startsWith("Validation failed:"));
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with single field error")
    void shouldHandleSingleFieldError() {
        // Given
        when(request.getAttribute("transactionId")).thenReturn("txn-single");
        when(request.getRequestURI()).thenReturn("/api/resource");

        Object target = new Object();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "name", "Name cannot be blank"));

        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValid(ex, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed: Name cannot be blank", response.getBody().getMessage());
    }

    // ==================== Generic Exception Tests ====================

    @Test
    @DisplayName("Should handle generic Exception and return 500")
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred: Unexpected error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.path").value("/test/generic"))
                .andExpect(jsonPath("$.transactionId").exists());
    }

    @Test
    @DisplayName("Should handle generic Exception directly via handler method")
    void shouldHandleGenericExceptionDirectly() {
        // Given
        when(request.getAttribute("transactionId")).thenReturn("txn-999");
        when(request.getRequestURI()).thenReturn("/api/error");

        Exception ex = new RuntimeException("Database connection failed");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Database connection failed"));
        assertTrue(response.getBody().getMessage().startsWith("An unexpected error occurred:"));
        assertEquals(500, response.getBody().getStatus());
    }

    // ==================== ErrorResponse Structure Tests ====================

    @Test
    @DisplayName("ErrorResponse should contain all required fields")
    void errorResponseShouldContainAllFields() {
        // Given
        when(request.getAttribute("transactionId")).thenReturn("txn-test");
        when(request.getRequestURI()).thenReturn("/api/test");

        NotFoundException ex = new NotFoundException("Test message");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFoundException(ex, request);
        ErrorResponse body = response.getBody();

        // Then
        assertNotNull(body);
        assertAll("ErrorResponse fields",
                () -> assertNotNull(body.getTransactionId()),
                () -> assertNotNull(body.getMessage()),
                () -> assertNotNull(body.getStatus()),
                () -> assertNotNull(body.getTimestamp()),
                () -> assertNotNull(body.getPath()),
                () -> assertEquals("txn-test", body.getTransactionId()),
                () -> assertEquals("Test message", body.getMessage()),
                () -> assertEquals(404, body.getStatus()),
                () -> assertEquals("/api/test", body.getPath()),
                () -> assertTrue(body.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)))
        );
    }

    @Test
    @DisplayName("Should handle null transactionId gracefully")
    void shouldHandleNullTransactionId() {
        // Given
        when(request.getAttribute("transactionId")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");

        ValidationException ex = new ValidationException("Validation error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex, request);

        // Then
        assertNotNull(response.getBody());
        assertNull(response.getBody().getTransactionId());
    }
}