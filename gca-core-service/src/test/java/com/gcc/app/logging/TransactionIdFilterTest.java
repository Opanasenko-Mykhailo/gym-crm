package com.gcc.app.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TransactionIdFilterTest {

    private final TransactionIdFilter filter = new TransactionIdFilter();

    @Test
    void shouldPutAndRemoveTransactionIdInMdcDuringFilter() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNull(MDC.get("transactionId"), "transactionId should be removed from MDC after filter");
        verify(chain, times(1)).doFilter(request, response);
        String txId = response.getHeader("X-Transaction-Id");
        assertNotNull(txId, "X-Transaction-Id header should be set");
    }

    @Test
    void shouldHaveTransactionIdSetDuringChainExecution() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doAnswer(invocation -> {
            String idInMdc = MDC.get("transactionId");
            assertNotNull(idInMdc, "transactionId should be set in MDC during filter chain execution");
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldUseExistingTransactionIdIfPresent() throws IOException, ServletException {
        String existingId = "12345";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Transaction-Id", existingId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        String txId = response.getHeader("X-Transaction-Id");
        assertNotNull(txId);
        assert (txId.equals(existingId));
    }
}