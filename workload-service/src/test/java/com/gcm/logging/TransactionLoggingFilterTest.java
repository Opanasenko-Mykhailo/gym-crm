package com.gcm.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionLoggingFilterTest {

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String TRANSACTION_ID_MDC = "transactionId";

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TransactionLoggingFilter filter;

    @Test
    void givenRequestWithoutTransactionId_whenFilter_thenGeneratesTransactionIdAndAddsHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doAnswer(invocation -> null).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);

        String transactionIdInHeader = response.getHeader(TRANSACTION_ID_HEADER);
        assertThat(transactionIdInHeader).isNotNull().isNotEmpty();

        String transactionIdInMDC = MDC.get(TRANSACTION_ID_MDC);
        assertThat(transactionIdInMDC).isNull();
    }

    @Test
    void givenRequestWithTransactionId_whenFilter_thenUsesExistingTransactionId() throws ServletException, IOException {
        String existingId = "12345";
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/submit");
        request.addHeader(TRANSACTION_ID_HEADER, existingId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        doAnswer(invocation -> null).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);

        String transactionIdInHeader = response.getHeader(TRANSACTION_ID_HEADER);
        assertThat(transactionIdInHeader).isEqualTo(existingId);

        String transactionIdInMDC = MDC.get(TRANSACTION_ID_MDC);
        assertThat(transactionIdInMDC).isNull();
    }
}