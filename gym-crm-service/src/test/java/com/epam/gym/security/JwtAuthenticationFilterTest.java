package com.epam.gym.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtProvider jwtProvider;
    @Mock private UserDetailsServiceImpl userDetailsService;
    @Mock private TokenBlacklistService tokenBlacklistService;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UserDetails buildUserDetails(String username) {
        return new User(username, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("doFilterInternal: valid token → sets authentication")
    void doFilterInternal_validToken_setsAuthentication() throws Exception {
        UserDetails userDetails = buildUserDetails("john.doe");

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(tokenBlacklistService.isBlacklisted("valid.jwt.token")).thenReturn(false);
        when(jwtProvider.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtProvider.getUsernameFromToken("valid.jwt.token")).thenReturn("john.doe");
        when(userDetailsService.loadUserByUsername("john.doe")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("john.doe");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal: blacklisted token → skips authentication")
    void doFilterInternal_blacklistedToken_skipsAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer blacklisted.token");
        when(tokenBlacklistService.isBlacklisted("blacklisted.token")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtProvider, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal: invalid token → does not set authentication")
    void doFilterInternal_invalidToken_doesNotSetAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        when(tokenBlacklistService.isBlacklisted("invalid.token")).thenReturn(false);
        when(jwtProvider.validateToken("invalid.token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal: no Authorization header → continues chain")
    void doFilterInternal_noAuthHeader_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
        verify(jwtProvider, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal: no Bearer prefix → continues chain")
    void doFilterInternal_noBearerPrefix_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenBlacklistService, never()).isBlacklisted(anyString());
        verify(jwtProvider, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal: empty Authorization header → continues chain")
    void doFilterInternal_emptyAuthHeader_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal: exception during validation → continues chain without authentication")
    void doFilterInternal_exceptionDuringValidation_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenBlacklistService.isBlacklisted("token")).thenReturn(false);
        when(jwtProvider.validateToken("token")).thenThrow(new RuntimeException("Unexpected error"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal: exception when loading user → continues chain without authentication")
    void doFilterInternal_exceptionLoadingUser_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token");
        when(tokenBlacklistService.isBlacklisted("valid.token")).thenReturn(false);
        when(jwtProvider.validateToken("valid.token")).thenReturn(true);
        when(jwtProvider.getUsernameFromToken("valid.token")).thenReturn("ghost.user");
        when(userDetailsService.loadUserByUsername("ghost.user"))
                .thenThrow(new RuntimeException("User not found"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}