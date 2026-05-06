package com.epam.gym.trainerworkloadservice.config.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    private static final String SECRET =
            "very-secret-key-very-secret-key-very-secret-key"; // >= 256 bits

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();

        // inject @Value field manually
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", SECRET);
    }

    @Test
    void shouldExtractUsernameFromValidToken() {
        // given
        String token = generateToken("john.doe", 3600);

        // when
        String username = jwtProvider.getUsernameFromToken(token);

        // then
        assertThat(username).isEqualTo("john.doe");
    }

    @Test
    void shouldValidateValidToken() {
        // given
        String token = generateToken("john.doe", 3600);

        // when
        boolean valid = jwtProvider.validateToken(token);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    void shouldReturnFalseForMalformedToken() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when
        boolean valid = jwtProvider.validateToken(invalidToken);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void shouldReturnFalseForExpiredToken() {
        // given
        String expiredToken = generateToken("john.doe", -3600);

        // when
        boolean valid = jwtProvider.validateToken(expiredToken);

        // then
        assertThat(valid).isFalse();
    }

    // --------------------------------------------------

    private String generateToken(String username, long secondsToLive) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(secondsToLive)))
                .signWith(key)
                .compact();
    }
}