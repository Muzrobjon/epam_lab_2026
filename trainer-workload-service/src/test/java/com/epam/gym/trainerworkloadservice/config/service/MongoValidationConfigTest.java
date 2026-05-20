package com.epam.gym.trainerworkloadservice.config;

import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;

import static org.assertj.core.api.Assertions.assertThat;

class MongoValidationConfigTest {

    private MongoValidationConfig config;

    @BeforeEach
    void setUp() {
        config = new MongoValidationConfig();
    }

    @Test
    void validator_ShouldReturnNonNullInstance() {
        Validator validator = config.validator();
        assertThat(validator).isNotNull();
    }

    @Test
    void validatingMongoEventListener_ShouldReturnNonNullInstance() {
        ValidatingMongoEventListener listener = config.validatingMongoEventListener();
        assertThat(listener).isNotNull();
    }
}