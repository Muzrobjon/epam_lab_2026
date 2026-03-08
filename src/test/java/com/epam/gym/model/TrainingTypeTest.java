package com.epam.gym.model;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainingTypeTest {

    @Test
    void testEqualsAndHashCode() {
        TrainingType t1 = new TrainingType(1L, TrainingTypeName.CARDIO);
        TrainingType t2 = new TrainingType(1L, TrainingTypeName.STRENGTH);
        TrainingType t3 = new TrainingType(2L, TrainingTypeName.CARDIO);

        assertEquals(t1, t2, "TrainingTypes with same id should be equal");
        assertEquals(t1.hashCode(), t2.hashCode(), "Hash codes should match for same id");
        assertNotEquals(t1, t3, "TrainingTypes with different ids should not be equal");
    }

    @Test
    void testToString() {
        TrainingType t = new TrainingType(1L, TrainingTypeName.CARDIO);
        String toString = t.toString();
        assertTrue(toString.contains("TrainingType{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("trainingTypeName='CARDIO'"));
    }

    @Test
    void testGettersAndConstructor() {
        TrainingType t = new TrainingType(5L, TrainingTypeName.STRENGTH);
        assertEquals(5L, t.getId());
        assertEquals(TrainingTypeName.STRENGTH, t.getTrainingTypeName());
    }
}