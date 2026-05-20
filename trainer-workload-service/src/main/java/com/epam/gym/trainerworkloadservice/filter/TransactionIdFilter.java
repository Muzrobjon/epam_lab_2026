package com.epam.gym.trainerworkloadservice.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionIdFilter implements Filter {

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String MDC_KEY = "X-Transaction-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String transactionId = httpRequest.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        httpRequest.setAttribute("transactionId", transactionId);
        httpResponse.setHeader(TRANSACTION_ID_HEADER, transactionId);
        MDC.put(MDC_KEY, transactionId);

        log.info("[TransactionId: {}] Request: {} {}",
                transactionId,
                httpRequest.getMethod(),
                httpRequest.getRequestURI());

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("[TransactionId: {}] Response: {} - Status: {} - Duration: {}ms",
                    transactionId,
                    httpRequest.getRequestURI(),
                    httpResponse.getStatus(),
                    duration);
            MDC.remove(MDC_KEY);
        }
    }
}