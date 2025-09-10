package com.gcc.app.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TransactionIdFilterTest {

    private final TransactionIdFilter filter = new TransactionIdFilter();

    @Test
    void shouldCallChainDirectlyForNonHttpRequests() throws IOException, ServletException {
        ServletRequest request = mock(ServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertThat(MDC.get("transactionId")).isNull();
    }

    @Test
    void shouldGenerateTransactionIdIfMissing() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        String txId = response.getHeader("X-Transaction-Id");
        assertThat(txId).isNotNull().isNotEmpty();
        assertThat(MDC.get("transactionId")).isNull();
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldUseExistingTransactionId() throws IOException, ServletException {
        String existingId = "12345";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Transaction-Id", existingId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        String txId = response.getHeader("X-Transaction-Id");
        assertThat(txId).isEqualTo(existingId);
        assertThat(MDC.get("transactionId")).isNull();
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldHaveTransactionIdSetDuringChainExecution() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doAnswer(invocation -> {
            String idInMdc = MDC.get("transactionId");
            assertThat(idInMdc).isNotNull().isNotEmpty();
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldClearMdcEvenIfChainThrows() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doThrow(new RuntimeException("Chain error")).when(chain).doFilter(request, response);

        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, chain));

        assertThat(MDC.get("transactionId")).isNull();
    }
}