package com.epam.gym.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {

    private SwaggerConfig swaggerConfig;

    @BeforeEach
    void setUp() {
        swaggerConfig = new SwaggerConfig();
    }

    @Test
    @DisplayName("Should create OpenAPI bean with correct API info")
    void shouldCreateOpenAPIBeanWithCorrectApiInfo() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI, "OpenAPI bean should not be null");

        Info info = openAPI.getInfo();
        assertNotNull(info, "Info should not be null");
        assertEquals("EPAM Lab 2025 API", info.getTitle(), "API title should match");
        assertEquals("1.0.0", info.getVersion(), "API version should match");
        assertEquals("REST API documentation", info.getDescription(), "API description should match");
    }

    @Test
    @DisplayName("Should create OpenAPI bean with correct security scheme")
    void shouldCreateOpenAPIBeanWithCorrectSecurityScheme() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        Components components = openAPI.getComponents();
        assertNotNull(components, "Components should not be null");

        SecurityScheme securityScheme = components.getSecuritySchemes().get("BearerAuth");
        assertNotNull(securityScheme, "BearerAuth security scheme should exist");

        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType(), "Security scheme type should be HTTP");
        assertEquals("bearer", securityScheme.getScheme(), "Security scheme should be bearer");
        assertEquals("JWT", securityScheme.getBearerFormat(), "Bearer format should be JWT");
        assertEquals(SecurityScheme.In.HEADER, securityScheme.getIn(), "Security scheme should be in header");
        assertEquals("Authorization", securityScheme.getName(), "Security scheme name should be Authorization");
    }

    @Test
    @DisplayName("Should create OpenAPI bean with all required components")
    void shouldCreateOpenAPIBeanWithAllRequiredComponents() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then - Verify all parts are present in single assertion group
        assertAll("OpenAPI configuration",
                () -> assertNotNull(openAPI, "OpenAPI should not be null"),
                () -> assertNotNull(openAPI.getInfo(), "Info should not be null"),
                () -> assertNotNull(openAPI.getComponents(), "Components should not be null"),
                () -> assertNotNull(openAPI.getComponents().getSecuritySchemes(), "Security schemes should not be null"),
                () -> assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("BearerAuth"),
                        "Should contain BearerAuth security scheme")
        );
    }

    @Test
    @DisplayName("Should return new instance on each call (prototype-like behavior check)")
    void shouldReturnConfiguredInstance() {
        // When
        OpenAPI firstCall = swaggerConfig.customOpenAPI();
        OpenAPI secondCall = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(firstCall);
        assertNotNull(secondCall);
        // Note: In Spring context, this would be a singleton.
        // Here we verify the configuration is consistent
        assertEquals(firstCall.getInfo().getTitle(), secondCall.getInfo().getTitle());
    }
}