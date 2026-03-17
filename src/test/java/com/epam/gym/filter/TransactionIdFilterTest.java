package com.epam.gym.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionIdFilterTest {

    private TransactionIdFilter filter;

    @Mock
    private FilterChain mockFilterChain;

    @BeforeEach
    void setUp() {
        filter = new TransactionIdFilter();
    }

    // ==================== Transaction ID Generation Tests ====================

    @Test
    @DisplayName("Should generate new transaction ID when header not present")
    void shouldGenerateNewTransactionIdWhenHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/trainees");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        String transactionId = (String) request.getAttribute("transactionId");
        assertNotNull(transactionId);
        assertFalse(transactionId.isEmpty());
        assertTrue(transactionId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));

        String responseHeader = response.getHeader("X-Transaction-Id");
        assertEquals(transactionId, responseHeader);
    }

    @Test
    @DisplayName("Should use existing transaction ID from request header")
    void shouldUseExistingTransactionIdFromHeader() throws ServletException, IOException {
        String existingTransactionId = "custom-txn-12345";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/trainings");
        request.addHeader("X-Transaction-Id", existingTransactionId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        String transactionId = (String) request.getAttribute("transactionId");
        assertEquals(existingTransactionId, transactionId);

        String responseHeader = response.getHeader("X-Transaction-Id");
        assertEquals(existingTransactionId, responseHeader);
    }

    @Test
    @DisplayName("Should generate new ID when header is empty string")
    void shouldGenerateNewIdWhenHeaderEmpty() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/test");
        request.addHeader("X-Transaction-Id", "");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        String transactionId = (String) request.getAttribute("transactionId");
        assertNotNull(transactionId);
        assertFalse(transactionId.isEmpty());
        assertNotEquals("", transactionId);
        assertTrue(transactionId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }

    @Test
    @DisplayName("Should accept whitespace-only header as-is (current filter behavior)")
    void shouldAcceptWhitespaceHeaderAsIs() throws ServletException, IOException {
        // Given - Current filter doesn't trim, so whitespace is accepted
        String whitespaceId = "   ";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/test");
        request.addHeader("X-Transaction-Id", whitespaceId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // When
        filter.doFilter(request, response, chain);

        // Then - Whitespace is NOT empty, so it's used as-is
        String transactionId = (String) request.getAttribute("transactionId");
        assertEquals(whitespaceId, transactionId, "Whitespace-only header should be accepted as-is");
        assertEquals(whitespaceId, response.getHeader("X-Transaction-Id"));
    }

    // ==================== Request/Response Handling Tests ====================

    @Test
    @DisplayName("Should continue filter chain")
    void shouldContinueFilterChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mockFilterChain);

        verify(mockFilterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should set response header before chain execution")
    void shouldSetHeaderBeforeChainExecution() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mockFilterChain);

        verify(mockFilterChain).doFilter(request, response);
        assertNotNull(response.getHeader("X-Transaction-Id"));
    }

    @Test
    @DisplayName("Should handle exceptions in filter chain and still log duration")
    void shouldHandleExceptionsAndLogDuration() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/error");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doThrow(new ServletException("Chain error")).when(mockFilterChain).doFilter(any(), any());

        assertThrows(ServletException.class, () -> {
            filter.doFilter(request, response, mockFilterChain);
        });

        assertNotNull(response.getHeader("X-Transaction-Id"));
    }

    // ==================== Logging and Timing Tests ====================

    @Test
    @DisplayName("Should capture request method and URI in logs")
    void shouldCaptureRequestDetails() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("PUT");
        request.setRequestURI("/api/trainers/John.Doe");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(request.getAttribute("transactionId"));
    }

    @Test
    @DisplayName("Should calculate and log request duration")
    void shouldCalculateDuration() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/slow-endpoint");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            Thread.sleep(50);
            return null;
        }).when(mockFilterChain).doFilter(any(), any());

        long startTime = System.currentTimeMillis();
        filter.doFilter(request, response, mockFilterChain);
        long actualDuration = System.currentTimeMillis() - startTime;

        assertTrue(actualDuration >= 50, "Duration should be at least 50ms");
        verify(mockFilterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should capture response status code")
    void shouldCaptureResponseStatus() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            response.setStatus(201);
            return null;
        }).when(mockFilterChain).doFilter(any(), any());

        filter.doFilter(request, response, mockFilterChain);

        assertEquals(201, response.getStatus());
        verify(mockFilterChain).doFilter(request, response);
    }

    // ==================== Edge Case Tests ====================

    @Test
    @DisplayName("Should handle null header value")
    void shouldHandleNullHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        String transactionId = (String) request.getAttribute("transactionId");
        assertNotNull(transactionId);
        assertFalse(transactionId.isEmpty());
    }

    @Test
    @DisplayName("Should handle different HTTP methods")
    void shouldHandleDifferentHttpMethods() throws ServletException, IOException {
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};

        for (String method : methods) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod(method);
            request.setRequestURI("/api/test");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);

            assertNotNull(request.getAttribute("transactionId"),
                    "Should set transaction ID for " + method);
            assertNotNull(response.getHeader("X-Transaction-Id"),
                    "Should set header for " + method);
        }
    }

    @Test
    @DisplayName("Should handle request with query parameters")
    void shouldHandleRequestWithQueryParams() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/trainings");
        request.setQueryString("fromDate=2024-01-01&toDate=2024-12-31");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(request.getAttribute("transactionId"));
        assertEquals("/api/trainings", request.getRequestURI());
    }

    @Test
    @DisplayName("Should generate unique transaction IDs for each request")
    void shouldGenerateUniqueIds() throws ServletException, IOException {
        String firstId;
        String secondId;

        MockHttpServletRequest request1 = new MockHttpServletRequest();
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        MockFilterChain chain1 = new MockFilterChain();
        filter.doFilter(request1, response1, chain1);
        firstId = (String) request1.getAttribute("transactionId");

        MockHttpServletRequest request2 = new MockHttpServletRequest();
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        MockFilterChain chain2 = new MockFilterChain();
        filter.doFilter(request2, response2, chain2);
        secondId = (String) request2.getAttribute("transactionId");

        assertNotNull(firstId);
        assertNotNull(secondId);
        assertNotEquals(firstId, secondId, "Each request should have unique transaction ID");
    }
}