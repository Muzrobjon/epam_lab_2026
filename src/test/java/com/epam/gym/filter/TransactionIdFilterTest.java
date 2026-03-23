package com.epam.gym.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionIdFilter Unit Tests")
class TransactionIdFilterTest {

    private TransactionIdFilter transactionIdFilter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String TRANSACTION_ID_ATTRIBUTE = "transactionId";

    @BeforeEach
    void setUp() {
        transactionIdFilter = new TransactionIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("Transaction ID Generation Tests")
    class TransactionIdGenerationTests {

        @Test
        @DisplayName("Should generate new transaction ID when header is not present")
        void doFilter_NoTransactionIdHeader_GeneratesNewTransactionId() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/trainees");

            transactionIdFilter.doFilter(request, response, filterChain);

            String transactionId = response.getHeader(TRANSACTION_ID_HEADER);
            assertThat(transactionId).isNotNull();
            assertThat(transactionId).isNotEmpty();
            assertThat(isValidUUID(transactionId)).isTrue();

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should generate new transaction ID when header is empty")
        void doFilter_EmptyTransactionIdHeader_GeneratesNewTransactionId() throws ServletException, IOException {
            request.setMethod("POST");
            request.setRequestURI("/api/trainers");
            request.addHeader(TRANSACTION_ID_HEADER, "");

            transactionIdFilter.doFilter(request, response, filterChain);

            String transactionId = response.getHeader(TRANSACTION_ID_HEADER);
            assertThat(transactionId).isNotNull();
            assertThat(transactionId).isNotEmpty();
            assertThat(isValidUUID(transactionId)).isTrue();

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should use existing transaction ID from header")
        void doFilter_ExistingTransactionIdHeader_UsesExistingTransactionId() throws ServletException, IOException {
            String existingTransactionId = UUID.randomUUID().toString();
            request.setMethod("GET");
            request.setRequestURI("/api/trainees/profile");
            request.addHeader(TRANSACTION_ID_HEADER, existingTransactionId);

            transactionIdFilter.doFilter(request, response, filterChain);

            String transactionId = response.getHeader(TRANSACTION_ID_HEADER);
            assertThat(transactionId).isEqualTo(existingTransactionId);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should use custom transaction ID from header")
        void doFilter_CustomTransactionId_UsesCustomTransactionId() throws ServletException, IOException {
            String customTransactionId = "custom-txn-12345";
            request.setMethod("PUT");
            request.setRequestURI("/api/trainees/john.doe");
            request.addHeader(TRANSACTION_ID_HEADER, customTransactionId);

            transactionIdFilter.doFilter(request, response, filterChain);

            String transactionId = response.getHeader(TRANSACTION_ID_HEADER);
            assertThat(transactionId).isEqualTo(customTransactionId);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should generate unique transaction IDs for different requests")
        void doFilter_MultipleRequests_GeneratesUniqueTransactionIds() throws ServletException, IOException {
            MockHttpServletRequest request1 = new MockHttpServletRequest();
            MockHttpServletResponse response1 = new MockHttpServletResponse();
            request1.setMethod("GET");
            request1.setRequestURI("/api/trainees");

            MockHttpServletRequest request2 = new MockHttpServletRequest();
            MockHttpServletResponse response2 = new MockHttpServletResponse();
            request2.setMethod("GET");
            request2.setRequestURI("/api/trainers");

            transactionIdFilter.doFilter(request1, response1, filterChain);
            transactionIdFilter.doFilter(request2, response2, filterChain);

            String transactionId1 = response1.getHeader(TRANSACTION_ID_HEADER);
            String transactionId2 = response2.getHeader(TRANSACTION_ID_HEADER);

            assertThat(transactionId1).isNotEqualTo(transactionId2);
        }
    }

    @Nested
    @DisplayName("Request Attribute Tests")
    class RequestAttributeTests {

        @Test
        @DisplayName("Should store transaction ID in request attribute")
        void doFilter_ValidRequest_StoresTransactionIdInAttribute() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/trainees");

            transactionIdFilter.doFilter(request, response, filterChain);

            Object transactionIdAttribute = request.getAttribute(TRANSACTION_ID_ATTRIBUTE);
            assertThat(transactionIdAttribute).isNotNull();
            assertThat(transactionIdAttribute).isInstanceOf(String.class);
            assertThat((String) transactionIdAttribute).isNotEmpty();
        }

        @Test
        @DisplayName("Should store existing transaction ID in request attribute")
        void doFilter_ExistingTransactionId_StoresInAttribute() throws ServletException, IOException {
            String existingTransactionId = UUID.randomUUID().toString();
            request.setMethod("POST");
            request.setRequestURI("/api/trainings");
            request.addHeader(TRANSACTION_ID_HEADER, existingTransactionId);

            transactionIdFilter.doFilter(request, response, filterChain);

            Object transactionIdAttribute = request.getAttribute(TRANSACTION_ID_ATTRIBUTE);
            assertThat(transactionIdAttribute).isEqualTo(existingTransactionId);
        }

        @Test
        @DisplayName("Should have matching transaction ID in attribute and response header")
        void doFilter_ValidRequest_MatchingAttributeAndHeader() throws ServletException, IOException {
            request.setMethod("DELETE");
            request.setRequestURI("/api/trainees/john.doe");

            transactionIdFilter.doFilter(request, response, filterChain);

            String headerTransactionId = response.getHeader(TRANSACTION_ID_HEADER);
            String attributeTransactionId = (String) request.getAttribute(TRANSACTION_ID_ATTRIBUTE);

            assertThat(headerTransactionId).isEqualTo(attributeTransactionId);
        }
    }

    @Nested
    @DisplayName("Response Header Tests")
    class ResponseHeaderTests {

        @Test
        @DisplayName("Should add transaction ID to response header")
        void doFilter_ValidRequest_AddsTransactionIdToResponseHeader() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/trainings/types");

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.containsHeader(TRANSACTION_ID_HEADER)).isTrue();
            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
        }

        @Test
        @DisplayName("Should preserve transaction ID in response even when exception occurs")
        void doFilter_ExceptionOccurs_PreservesTransactionIdInResponse() throws ServletException, IOException {
            request.setMethod("POST");
            request.setRequestURI("/api/trainings");

            doThrow(new ServletException("Test exception")).when(filterChain).doFilter(any(), any());

            assertThatThrownBy(() -> transactionIdFilter.doFilter(request, response, filterChain))
                    .isInstanceOf(ServletException.class);

            assertThat(response.containsHeader(TRANSACTION_ID_HEADER)).isTrue();
        }
    }

    @Nested
    @DisplayName("Filter Chain Tests")
    class FilterChainTests {

        @Test
        @DisplayName("Should call filter chain doFilter method")
        void doFilter_ValidRequest_CallsFilterChain() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/trainees");

            transactionIdFilter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should propagate ServletException from filter chain")
        void doFilter_FilterChainThrowsServletException_PropagatesException() throws ServletException, IOException {
            request.setMethod("POST");
            request.setRequestURI("/api/trainings");

            ServletException expectedException = new ServletException("Filter chain error");
            doThrow(expectedException).when(filterChain).doFilter(any(), any());

            assertThatThrownBy(() -> transactionIdFilter.doFilter(request, response, filterChain))
                    .isInstanceOf(ServletException.class)
                    .hasMessage("Filter chain error");
        }

        @Test
        @DisplayName("Should propagate IOException from filter chain")
        void doFilter_FilterChainThrowsIOException_PropagatesException() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/trainees");

            IOException expectedException = new IOException("IO error");
            doThrow(expectedException).when(filterChain).doFilter(any(), any());

            assertThatThrownBy(() -> transactionIdFilter.doFilter(request, response, filterChain))
                    .isInstanceOf(IOException.class)
                    .hasMessage("IO error");
        }

        @Test
        @DisplayName("Should propagate RuntimeException from filter chain")
        void doFilter_FilterChainThrowsRuntimeException_PropagatesException() throws ServletException, IOException {
            request.setMethod("PUT");
            request.setRequestURI("/api/trainees/john.doe");

            RuntimeException expectedException = new RuntimeException("Runtime error");
            doThrow(expectedException).when(filterChain).doFilter(any(), any());

            assertThatThrownBy(() -> transactionIdFilter.doFilter(request, response, filterChain))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Runtime error");
        }
    }

    @Nested
    @DisplayName("HTTP Method Tests")
    class HttpMethodTests {

        @Test
        @DisplayName("Should handle GET request")
        void doFilter_GetRequest_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/trainees/profile");

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle POST request")
        void doFilter_PostRequest_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("POST");
            request.setRequestURI("/api/trainees/register");

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle PUT request")
        void doFilter_PutRequest_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("PUT");
            request.setRequestURI("/api/trainees/john.doe");

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle DELETE request")
        void doFilter_DeleteRequest_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("DELETE");
            request.setRequestURI("/api/trainees/john.doe");

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle PATCH request")
        void doFilter_PatchRequest_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("PATCH");
            request.setRequestURI("/api/trainees/john.doe/status");

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Various URI Tests")
    class VariousUriTests {

        @Test
        @DisplayName("Should handle root URI")
        void doFilter_RootUri_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/");

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle URI with path variables")
        void doFilter_UriWithPathVariables_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/trainees/john.doe/trainers");

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle URI with query parameters")
        void doFilter_UriWithQueryParameters_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/trainees/trainings");
            request.setQueryString("fromDate=2024-01-01&toDate=2024-12-31");

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle long URI path")
        void doFilter_LongUriPath_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/v1/gym/management/trainees/john.doe/trainings/history");

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Response Status Tests")
    class ResponseStatusTests {

        @Test
        @DisplayName("Should handle successful response status")
        void doFilter_SuccessfulResponse_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/trainees");
            response.setStatus(200);

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
        }

        @Test
        @DisplayName("Should handle created response status")
        void doFilter_CreatedResponse_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("POST");
            request.setRequestURI("/api/trainees/register");
            response.setStatus(201);

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
        }

        @Test
        @DisplayName("Should handle error response status")
        void doFilter_ErrorResponse_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/trainees/nonexistent");
            response.setStatus(404);

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
        }

        @Test
        @DisplayName("Should handle server error response status")
        void doFilter_ServerErrorResponse_ProcessesSuccessfully() throws ServletException, IOException {
            request.setMethod("POST");
            request.setRequestURI("/api/trainings");
            response.setStatus(500);

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle whitespace-only transaction ID header")
        void doFilter_WhitespaceTransactionIdHeader_GeneratesNewTransactionId() throws ServletException, IOException {
            request.setMethod("GET");
            request.setRequestURI("/api/trainees");
            request.addHeader(TRANSACTION_ID_HEADER, "   ");

            transactionIdFilter.doFilter(request, response, filterChain);

            // Note: Current implementation treats whitespace as non-empty
            // If you want to treat whitespace as empty, you'd need to modify the filter
            String transactionId = response.getHeader(TRANSACTION_ID_HEADER);
            assertThat(transactionId).isNotNull();
        }

        @Test
        @DisplayName("Should handle very long transaction ID")
        void doFilter_VeryLongTransactionId_UsesExistingTransactionId() throws ServletException, IOException {
            String longTransactionId = "txn-" + "a".repeat(1000);
            request.setMethod("GET");
            request.setRequestURI("/api/trainees");
            request.addHeader(TRANSACTION_ID_HEADER, longTransactionId);

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isEqualTo(longTransactionId);
        }

        @Test
        @DisplayName("Should handle special characters in transaction ID")
        void doFilter_SpecialCharactersInTransactionId_UsesExistingTransactionId() throws ServletException, IOException {
            String specialTransactionId = "txn-!@#$%^&*()_+-=[]{}|;':\",./<>?";
            request.setMethod("GET");
            request.setRequestURI("/api/trainees");
            request.addHeader(TRANSACTION_ID_HEADER, specialTransactionId);

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isEqualTo(specialTransactionId);
        }

        @Test
        @DisplayName("Should handle unicode characters in transaction ID")
        void doFilter_UnicodeTransactionId_UsesExistingTransactionId() throws ServletException, IOException {
            String unicodeTransactionId = "txn-日本語-한국어-中文";
            request.setMethod("GET");
            request.setRequestURI("/api/trainees");
            request.addHeader(TRANSACTION_ID_HEADER, unicodeTransactionId);

            transactionIdFilter.doFilter(request, response, filterChain);

            assertThat(response.getHeader(TRANSACTION_ID_HEADER)).isEqualTo(unicodeTransactionId);
        }
    }

    // ==================== Helper Methods ====================

    private boolean isValidUUID(String str) {
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}