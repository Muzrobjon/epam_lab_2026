package com.epam.gym.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint entryPoint;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        entryPoint = new JwtAuthenticationEntryPoint();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("commence: writes 401 JSON response with error details")
    void commence_writesUnauthorizedJsonResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthenticationException ex = mock(AuthenticationException.class);
        when(ex.getMessage()).thenReturn("Full authentication is required");

        entryPoint.commence(request, response, ex);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = objectMapper.readValue(response.getContentAsByteArray(), Map.class);

        assertThat(body.get("status")).isEqualTo(401);
        assertThat(body.get("error")).isEqualTo("Unauthorized");
        assertThat(body.get("message")).isEqualTo("Full authentication is required");
        assertThat(body.get("path")).isEqualTo("/api/protected");
        assertThat(body.get("timestamp")).isNotNull();
    }

    @Test
    @DisplayName("commence: handles null exception message")
    void commence_handlesNullMessage() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthenticationException ex = mock(AuthenticationException.class);
        when(ex.getMessage()).thenReturn(null);

        entryPoint.commence(request, response, ex);

        assertThat(response.getStatus()).isEqualTo(401);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = objectMapper.readValue(response.getContentAsByteArray(), Map.class);

        assertThat(body.get("status")).isEqualTo(401);
        assertThat(body.get("error")).isEqualTo("Unauthorized");
        assertThat(body.get("message")).isNull();
        assertThat(body.get("path")).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("commence: writes correct servlet path")
    void commence_writesCorrectPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/trainers/workload");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthenticationException ex = mock(AuthenticationException.class);
        when(ex.getMessage()).thenReturn("JWT expired");

        entryPoint.commence(request, response, ex);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = objectMapper.readValue(response.getContentAsByteArray(), Map.class);

        assertThat(body.get("path")).isEqualTo("/api/trainers/workload");
        assertThat(body.get("message")).isEqualTo("JWT expired");
    }
}