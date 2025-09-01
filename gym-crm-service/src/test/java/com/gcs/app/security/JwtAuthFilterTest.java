package com.gcs.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtAuthFilter filter;

    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor;

    @BeforeEach
    void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doFilterInternal_noAuthorizationHeader_callsFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService, securityContext);
    }

    @Test
    void doFilterInternal_authHeaderNotBearer_callsFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic some-token");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService, securityContext);
    }

    @Test
    void doFilterInternal_tokenNotAccessType_callsFilterChain() throws Exception {
        String token = "token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("user");
        when(jwtService.getTokenType(token)).thenReturn("refresh");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService, securityContext);
    }

    @Test
    void doFilterInternal_validToken_authenticatesUser() throws Exception {
        String token = "valid.jwt.token";
        String username = "test.user";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.getTokenType(token)).thenReturn("access");
        when(securityContext.getAuthentication()).thenReturn(null);

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(true);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        filter.doFilterInternal(request, response, filterChain);

        verify(securityContext).setAuthentication(authCaptor.capture());
        UsernamePasswordAuthenticationToken auth = authCaptor.getValue();

        assertEquals(userDetails, auth.getPrincipal());
        assertNull(auth.getCredentials());
        assertEquals(userDetails.getAuthorities(), auth.getAuthorities());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tokenInvalid_noAuthenticationSet() throws Exception {
        String token = "invalid.jwt.token";
        String username = "test.user";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.getTokenType(token)).thenReturn("access");
        when(securityContext.getAuthentication()).thenReturn(null);

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(userDetails.isEnabled()).thenReturn(true);
        when(jwtService.isTokenValid(token)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_alreadyAuthenticated_callsFilterChainWithoutChanges() throws Exception {
        String token = "token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("user");
        when(jwtService.getTokenType(token)).thenReturn("access");
        when(securityContext.getAuthentication()).thenReturn(mock(UsernamePasswordAuthenticationToken.class));

        filter.doFilterInternal(request, response, filterChain);

        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }
}