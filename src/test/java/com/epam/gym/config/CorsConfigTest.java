package com.epam.gym.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    private CorsConfigurationSource corsConfigurationSource;
    private CorsConfiguration corsConfiguration;

    @BeforeEach
    void setUp() {
        CorsConfig corsConfig = new CorsConfig();
        corsConfigurationSource = corsConfig.corsConfigurationSource();

        corsConfiguration = ((UrlBasedCorsConfigurationSource) corsConfigurationSource)
                .getCorsConfigurations().get("/**");
    }

    @Test
    @DisplayName("Should create CorsConfigurationSource bean")
    void corsConfigurationSource_ShouldNotBeNull() {
        assertThat(corsConfigurationSource).isNotNull();
        assertThat(corsConfigurationSource).isInstanceOf(UrlBasedCorsConfigurationSource.class);
    }

    @Test
    @DisplayName("Should register configuration for all paths")
    void corsConfigurationSource_ShouldRegisterForAllPaths() {
        assertThat(corsConfiguration).isNotNull();
    }

    // ==================== ALLOWED ORIGINS TESTS ====================

    @Nested
    @DisplayName("Allowed Origins Tests")
    class AllowedOriginsTests {

        @Test
        @DisplayName("Should contain all allowed origins")
        void shouldContainAllAllowedOrigins() {
            assertThat(corsConfiguration.getAllowedOrigins())
                    .containsExactlyInAnyOrder(
                            "http://localhost:3000",
                            "http://localhost:4200",
                            "http://localhost:8080"
                    );
        }

        @Test
        @DisplayName("Should have exactly 3 allowed origins")
        void shouldHaveExactlyThreeOrigins() {
            assertThat(corsConfiguration.getAllowedOrigins()).hasSize(3);
        }

        @Test
        @DisplayName("Should allow React default port")
        void shouldAllowReactPort() {
            assertThat(corsConfiguration.getAllowedOrigins())
                    .contains("http://localhost:3000");
        }

        @Test
        @DisplayName("Should allow Angular default port")
        void shouldAllowAngularPort() {
            assertThat(corsConfiguration.getAllowedOrigins())
                    .contains("http://localhost:4200");
        }

        @Test
        @DisplayName("Should allow Spring Boot default port")
        void shouldAllowSpringBootPort() {
            assertThat(corsConfiguration.getAllowedOrigins())
                    .contains("http://localhost:8080");
        }
    }

    // ==================== ALLOWED METHODS TESTS ====================

    @Nested
    @DisplayName("Allowed Methods Tests")
    class AllowedMethodsTests {

        @Test
        @DisplayName("Should contain all allowed HTTP methods")
        void shouldContainAllAllowedMethods() {
            assertThat(corsConfiguration.getAllowedMethods())
                    .containsExactlyInAnyOrder(
                            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
                    );
        }

        @Test
        @DisplayName("Should have exactly 6 allowed methods")
        void shouldHaveExactlySixMethods() {
            assertThat(corsConfiguration.getAllowedMethods()).hasSize(6);
        }

        @Test
        @DisplayName("Should allow OPTIONS for preflight requests")
        void shouldAllowOptionsMethod() {
            assertThat(corsConfiguration.getAllowedMethods())
                    .contains("OPTIONS");
        }
    }

    // ==================== ALLOWED HEADERS TESTS ====================

    @Nested
    @DisplayName("Allowed Headers Tests")
    class AllowedHeadersTests {

        @Test
        @DisplayName("Should allow all headers with wildcard")
        void shouldAllowAllHeaders() {
            assertThat(corsConfiguration.getAllowedHeaders())
                    .containsExactly("*");
        }
    }

    // ==================== CREDENTIALS TESTS ====================

    @Nested
    @DisplayName("Credentials Tests")
    class CredentialsTests {

        @Test
        @DisplayName("Should allow credentials")
        void shouldAllowCredentials() {
            assertThat(corsConfiguration.getAllowCredentials()).isTrue();
        }
    }

    // ==================== EXPOSED HEADERS TESTS ====================

    @Nested
    @DisplayName("Exposed Headers Tests")
    class ExposedHeadersTests {

        @Test
        @DisplayName("Should contain all exposed headers")
        void shouldContainAllExposedHeaders() {
            assertThat(corsConfiguration.getExposedHeaders())
                    .containsExactlyInAnyOrder(
                            "Authorization",
                            "Content-Type"
                    );
        }

        @Test
        @DisplayName("Should have exactly 2 exposed headers")
        void shouldHaveExactlyTwoExposedHeaders() {
            assertThat(corsConfiguration.getExposedHeaders()).hasSize(2);
        }

        @Test
        @DisplayName("Should expose Authorization header")
        void shouldExposeAuthorizationHeader() {
            assertThat(corsConfiguration.getExposedHeaders())
                    .contains("Authorization");
        }
    }

    // ==================== MAX AGE TESTS ====================

    @Nested
    @DisplayName("Max Age Tests")
    class MaxAgeTests {

        @Test
        @DisplayName("Should set max age to 3600 seconds")
        void shouldSetMaxAgeTo3600() {
            assertThat(corsConfiguration.getMaxAge()).isEqualTo(3600L);
        }
    }
}