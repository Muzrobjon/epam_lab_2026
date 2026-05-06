package com.epam.gym.trainerworkloadservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidation_returnsBadRequest() throws NoSuchMethodException {
        when(request.getAttribute("transactionId")).thenReturn("tx-1");
        when(request.getRequestURI()).thenReturn("/api/test");

        Object target = new Object();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.rejectValue(null, "code", "Field is required");

        MethodParameter methodParameter = new MethodParameter(
                this.getClass().getDeclaredMethod("handleValidation_returnsBadRequest"), -1);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("transactionId")).isEqualTo("tx-1");
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("path")).isEqualTo("/api/test");
    }

    @Test
    void handleGeneric_returnsInternalServerError() {
        when(request.getAttribute("transactionId")).thenReturn("tx-2");
        when(request.getRequestURI()).thenReturn("/api/error");

        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("transactionId")).isEqualTo("tx-2");
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("message").toString()).contains("Something went wrong");
    }
}