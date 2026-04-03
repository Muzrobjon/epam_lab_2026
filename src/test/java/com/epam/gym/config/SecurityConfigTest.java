package com.epam.gym.config;

import com.epam.gym.security.JwtAuthenticationEntryPoint;
import com.epam.gym.security.JwtAuthenticationFilter;
import com.epam.gym.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private CorsConfigurationSource corsConfigurationSource;

    @InjectMocks
    private SecurityConfig securityConfig;

    // ==================== PASSWORD ENCODER TESTS ====================

    @Nested
    @DisplayName("Password Encoder Tests")
    class PasswordEncoderTests {

        private PasswordEncoder passwordEncoder;

        @BeforeEach
        void setUp() {
            passwordEncoder = securityConfig.passwordEncoder();
        }

        @Test
        @DisplayName("Should create PasswordEncoder bean")
        void passwordEncoder_ShouldNotBeNull() {
            assertThat(passwordEncoder).isNotNull();
        }

        @Test
        @DisplayName("Should return BCryptPasswordEncoder instance")
        void passwordEncoder_ShouldBeBCryptInstance() {
            assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
        }

        @Test
        @DisplayName("Should encode password")
        void passwordEncoder_ShouldEncodePassword() {
            String rawPassword = "TestPassword@123";
            String encoded = passwordEncoder.encode(rawPassword);

            assertThat(encoded).isNotNull();
            assertThat(encoded).isNotEqualTo(rawPassword);
            assertThat(encoded).startsWith("$2a$12$");
        }

        @Test
        @DisplayName("Should match raw password with encoded password")
        void passwordEncoder_ShouldMatchPassword() {
            String rawPassword = "TestPassword@123";
            String encoded = passwordEncoder.encode(rawPassword);

            assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
        }

        @Test
        @DisplayName("Should not match wrong password")
        void passwordEncoder_ShouldNotMatchWrongPassword() {
            String rawPassword = "TestPassword@123";
            String wrongPassword = "WrongPassword@456";
            String encoded = passwordEncoder.encode(rawPassword);

            assertThat(passwordEncoder.matches(wrongPassword, encoded)).isFalse();
        }

        @Test
        @DisplayName("Should use strength 12")
        void passwordEncoder_ShouldUseStrength12() {
            String rawPassword = "Test";
            String encoded = passwordEncoder.encode(rawPassword);

            // BCrypt hash format: $2a$STRENGTH$...
            // $2a$12$ — 12 rounds
            assertThat(encoded).startsWith("$2a$12$");
        }

        @Test
        @DisplayName("Should produce different hashes for same password")
        void passwordEncoder_ShouldProduceDifferentHashes() {
            String rawPassword = "TestPassword@123";
            String encoded1 = passwordEncoder.encode(rawPassword);
            String encoded2 = passwordEncoder.encode(rawPassword);

            assertThat(encoded1).isNotEqualTo(encoded2);
            assertThat(passwordEncoder.matches(rawPassword, encoded1)).isTrue();
            assertThat(passwordEncoder.matches(rawPassword, encoded2)).isTrue();
        }
    }

    // ==================== AUTHENTICATION MANAGER TESTS ====================

    @Nested
    @DisplayName("Authentication Manager Tests")
    class AuthenticationManagerTests {

        @Test
        @DisplayName("Should create AuthenticationManager bean")
        void authenticationManager_ShouldNotBeNull() {
            AuthenticationManager authManager = securityConfig.authenticationManager();

            assertThat(authManager).isNotNull();
        }

        @Test
        @DisplayName("Should return ProviderManager instance")
        void authenticationManager_ShouldBeProviderManager() {
            AuthenticationManager authManager = securityConfig.authenticationManager();

            assertThat(authManager).isInstanceOf(ProviderManager.class);
        }

        @Test
        @DisplayName("Should contain DaoAuthenticationProvider")
        void authenticationManager_ShouldContainDaoProvider() {
            AuthenticationManager authManager = securityConfig.authenticationManager();
            ProviderManager providerManager = (ProviderManager) authManager;

            assertThat(providerManager.getProviders()).hasSize(1);
            assertThat(providerManager.getProviders().getFirst())
                    .isInstanceOf(DaoAuthenticationProvider.class);
        }
    }
}