package com.epam.gym.trainerworkloadservice.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class TransactionIdFilterTest {

    private final TransactionIdFilter filter = new TransactionIdFilter();

    @Test
    void doFilter_withHeader_usesProvidedTransactionId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Transaction-Id", "custom-id-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(request.getAttribute("transactionId")).isEqualTo("custom-id-123");
        assertThat(response.getHeader("X-Transaction-Id")).isEqualTo("custom-id-123");
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_withoutHeader_generatesUuid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        String txId = (String) request.getAttribute("transactionId");
        assertThat(txId).isNotNull().isNotEmpty();
        assertThat(response.getHeader("X-Transaction-Id")).isEqualTo(txId);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_emptyHeader_generatesUuid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Transaction-Id", "");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(request.getAttribute("transactionId")).isNotNull();
        verify(chain).doFilter(request, response);
    }
}