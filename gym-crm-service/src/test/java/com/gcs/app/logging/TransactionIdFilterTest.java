package com.gcs.app.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TransactionIdFilterTest {

    private final TransactionIdFilter filter = new TransactionIdFilter();

    @Test
    void shouldPutAndRemoveTransactionIdInMdcDuringFilter() throws Exception {
        ServletRequest request = mock(ServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNull(MDC.get("transactionId"), "transactionId should be removed from MDC after filter");
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldHaveTransactionIdSetDuringChainExecution() throws Exception {
        ServletRequest request = mock(ServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        doAnswer(invocation -> {
            String idInMdc = MDC.get("transactionId");
            assertNotNull(idInMdc, "transactionId should be set in MDC during filter chain execution");
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);
    }
}