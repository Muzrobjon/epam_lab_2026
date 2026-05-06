package com.epam.gym.mapper;

import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TrainingTypeMapperTest {

    private final TrainingTypeMapper mapper = Mappers.getMapper(TrainingTypeMapper.class);

    @Test
    void shouldMapTrainingTypeToTrainingTypeResponse() {
        TrainingType trainingType = new TrainingType(1L, TrainingTypeName.YOGA);

        TrainingTypeResponse response = mapper.toResponse(trainingType);

        assertNotNull(response, "The response should not be null");
        assertEquals(trainingType.getId(), response.getId(), "The ID should match");
        assertEquals(trainingType.getTrainingTypeName(), response.getTrainingTypeName(), "The trainingTypeName should match");
    }

    @Test
    void shouldMapTrainingTypeListToTrainingTypeResponseList() {
        TrainingType trainingType1 = new TrainingType(1L, TrainingTypeName.FITNESS);
        TrainingType trainingType2 = new TrainingType(2L, TrainingTypeName.CARDIO);

        List<TrainingType> trainingTypes = List.of(trainingType1, trainingType2);

        List<TrainingTypeResponse> responses = mapper.toResponseList(trainingTypes);

        assertNotNull(responses, "The response list should not be null");
        assertEquals(trainingTypes.size(), responses.size(), "The size of the response list should match the input list size");

        assertEquals(trainingType1.getId(), responses.get(0).getId(), "The ID of the first training type should match");
        assertEquals(trainingType1.getTrainingTypeName(), responses.get(0).getTrainingTypeName(), "The trainingTypeName of the first training type should match");

        assertEquals(trainingType2.getId(), responses.get(1).getId(), "The ID of the second training type should match");
        assertEquals(trainingType2.getTrainingTypeName(), responses.get(1).getTrainingTypeName(), "The trainingTypeName of the second training type should match");
    }
}