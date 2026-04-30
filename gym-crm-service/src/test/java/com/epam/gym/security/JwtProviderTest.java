package com.epam.gym.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

    @InjectMocks
    private JwtProvider jwtProvider;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Injecting @Value fields manually
        String secret = "your-very-long-secret-key-that-must-be-at-least-32-characters";
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", secret);
        // 1 hour
        long expiration = 3600000;
        ReflectionTestUtils.setField(jwtProvider, "jwtExpiration", expiration);
    }

    @Test
    @DisplayName("Should generate a valid token based on Authentication object")
    void generateToken_Success() {
        // GIVEN
        UserPrincipal principal = UserPrincipal.builder()
                .id(1L)
                .username("test_user")
                .password("encoded_password")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(authentication.getPrincipal()).thenReturn(principal);

        // WHEN
        String token = jwtProvider.generateToken(authentication);

        // THEN
        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));
        assertEquals("test_user", jwtProvider.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("Should generate a valid token from a simple username")
    void generateTokenFromUsername_Success() {
        // WHEN
        String token = jwtProvider.generateTokenFromUsername("admin");

        // THEN
        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));
        assertEquals("admin", jwtProvider.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("Should return false when token is malformed")
    void validateToken_Malformed_ReturnsFalse() {
        // GIVEN
        String malformedToken = "invalid.token.structure";

        // WHEN
        boolean isValid = jwtProvider.validateToken(malformedToken);

        // THEN
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should return false when token is expired")
    void validateToken_Expired_ReturnsFalse() {
        // GIVEN: Set expiration to -5 seconds to force expiration
        ReflectionTestUtils.setField(jwtProvider, "jwtExpiration", -5000L);
        String expiredToken = jwtProvider.generateTokenFromUsername("expiredUser");

        // WHEN
        boolean isValid = jwtProvider.validateToken(expiredToken);

        // THEN
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should extract correct expiration date-time from token")
    void getExpirationFromToken_Success() {
        // GIVEN
        String token = jwtProvider.generateTokenFromUsername("timeCheckUser");

        // WHEN
        LocalDateTime expirationTime = jwtProvider.getExpirationFromToken(token);

        // THEN
        assertNotNull(expirationTime);
        assertTrue(expirationTime.isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should return false when token is null or empty")
    void validateToken_Empty_ReturnsFalse() {
        // WHEN & THEN
        assertFalse(jwtProvider.validateToken(""));
        assertFalse(jwtProvider.validateToken(null));
    }
}