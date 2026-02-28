package com.epam.storage;

import com.epam.gym.storage.DataInitializer;
import com.epam.gym.storage.Storage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private Storage storage;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void init_WithNullFile_ShouldNotThrowException() {
        assertDoesNotThrow(() -> dataInitializer.init());
    }

    @Test
    void constructor_ShouldInitialize() {
        assertNotNull(dataInitializer);
    }
}
