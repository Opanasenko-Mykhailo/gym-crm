package com.gcm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private static final String TEST_VALID_TOKEN = "valid.jwt.token";
    private static final String TEST_INVALID_TOKEN = "invalid.jwt.token";
    private static final String TEST_USERNAME = "test.user";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthFilter filter;

    @Test
    void doFilterInternal_validToken_authenticatesUser() throws ServletException, IOException {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_VALID_TOKEN);
        when(jwtService.isTokenValid(TEST_VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(TEST_VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(jwtService.getAuthorities(TEST_VALID_TOKEN)).thenReturn(authorities);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getSession(false)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).isTokenValid(TEST_VALID_TOKEN);
        verify(jwtService).extractUsername(TEST_VALID_TOKEN);
        verify(jwtService).getAuthorities(TEST_VALID_TOKEN);

        assertEquals(TEST_USERNAME, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertNull(SecurityContextHolder.getContext().getAuthentication().getCredentials());
        assertEquals(authorities, SecurityContextHolder.getContext().getAuthentication().getAuthorities());
    }

    @Test
    void doFilterInternal_noToken_proceedsWithoutAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService, never()).isTokenValid(any());
        verify(jwtService, never()).extractUsername(any());
        verify(jwtService, never()).getAuthorities(any());
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_invalidToken_clearsContextAndProceeds() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_INVALID_TOKEN);
        when(jwtService.isTokenValid(TEST_INVALID_TOKEN)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).isTokenValid(TEST_INVALID_TOKEN);
        verify(jwtService, never()).extractUsername(TEST_INVALID_TOKEN);
        verify(jwtService, never()).getAuthorities(TEST_INVALID_TOKEN);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_nullUsername_clearsContextAndProceeds() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_VALID_TOKEN);
        when(jwtService.isTokenValid(TEST_VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(TEST_VALID_TOKEN)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).isTokenValid(TEST_VALID_TOKEN);
        verify(jwtService).extractUsername(TEST_VALID_TOKEN);
        verify(jwtService, never()).getAuthorities(TEST_VALID_TOKEN);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_nullAuthorities_clearsContextAndProceeds() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_VALID_TOKEN);
        when(jwtService.isTokenValid(TEST_VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(TEST_VALID_TOKEN)).thenReturn(TEST_USERNAME);
        when(jwtService.getAuthorities(TEST_VALID_TOKEN)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).isTokenValid(TEST_VALID_TOKEN);
        verify(jwtService).extractUsername(TEST_VALID_TOKEN);
        verify(jwtService).getAuthorities(TEST_VALID_TOKEN);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_exceptionDuringProcessing_clearsContextAndProceeds() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_VALID_TOKEN);
        when(jwtService.isTokenValid(TEST_VALID_TOKEN)).thenThrow(new RuntimeException("Token validation failed"));

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).isTokenValid(TEST_VALID_TOKEN);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}