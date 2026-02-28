package com.epam.storage;

import com.epam.gym.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StorageTest {

    private Storage storage;

    @BeforeEach
    void setUp() {
        storage = new Storage();
    }

    @Test
    void constructor_ShouldInitializeNamespaces() {
        assertTrue(storage.getDataStore().containsKey("trainees"));
        assertTrue(storage.getDataStore().containsKey("trainers"));
        assertTrue(storage.getDataStore().containsKey("trainings"));
        assertTrue(storage.getDataStore().containsKey("trainingTypes"));
    }

    @Test
    void generateId_ShouldReturnIncrementalIds() {
        Long id1 = storage.generateId("trainees");
        Long id2 = storage.generateId("trainees");
        Long id3 = storage.generateId("trainers");

        assertEquals(1L, id1);
        assertEquals(2L, id2);
        assertEquals(1L, id3); // Different namespace, starts from 1
    }

    @Test
    void putAndGet_ShouldStoreAndRetrieveValue() {
        String value = "Test Trainee";
        storage.put("trainees", 1L, value);

        Object retrieved = storage.get("trainees", 1L);

        assertEquals(value, retrieved);
    }

    @Test
    void get_WhenNotExists_ShouldReturnNull() {
        Object result = storage.get("trainees", 999L);

        assertNull(result);
    }

    @Test
    void remove_ShouldDeleteValue() {
        storage.put("trainees", 1L, "Value");

        Object removed = storage.remove("trainees", 1L);
        Object retrieved = storage.get("trainees", 1L);

        assertEquals("Value", removed);
        assertNull(retrieved);
    }

    @Test
    void getAll_ShouldReturnCopyOfMap() {
        storage.put("trainees", 1L, "Value1");
        storage.put("trainees", 2L, "Value2");

        Map<Long, Object> all = storage.getAll("trainees");

        assertEquals(2, all.size());
        assertTrue(all.containsKey(1L));
        assertTrue(all.containsKey(2L));

        // Verify it's a copy
        all.put(3L, "Value3");
        assertNull(storage.get("trainees", 3L));
    }

    @Test
    void contains_WhenExists_ShouldReturnTrue() {
        storage.put("trainees", 1L, "Value");

        assertTrue(storage.contains("trainees", 1L));
    }

    @Test
    void contains_WhenNotExists_ShouldReturnFalse() {
        assertFalse(storage.contains("trainees", 999L));
    }

    @Test
    void count_ShouldReturnSize() {
        assertEquals(0, storage.count("trainees"));

        storage.put("trainees", 1L, "Value1");
        assertEquals(1, storage.count("trainees"));

        storage.put("trainees", 2L, "Value2");
        assertEquals(2, storage.count("trainees"));
    }

    @Test
    void concurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        // Basic thread safety test
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                Long id = storage.generateId("trainees");
                storage.put("trainees", id, "Value" + id);
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(200, storage.count("trainees"));
    }
}

