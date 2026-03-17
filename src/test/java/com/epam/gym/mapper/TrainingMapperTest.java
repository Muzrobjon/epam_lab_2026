package com.epam.gym.mapper;

import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrainingMapperTest {

    private final TrainingMapper mapper = Mappers.getMapper(TrainingMapper.class);

    @Test
    void shouldMapTrainingToResponse() {

        User trainerUser = new User();
        trainerUser.setFirstName("John");
        trainerUser.setLastName("Doe");

        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);

        User traineeUser = new User();
        traineeUser.setFirstName("Jane");
        traineeUser.setLastName("Smith");

        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);

        TrainingType trainingType = new TrainingType();
        ReflectionTestUtils.setField(trainingType, "trainingTypeName", TrainingTypeName.STRENGTH);

        Training training = new Training();
        training.setTrainingName("Morning Workout");
        training.setTrainingDate(LocalDate.of(2025, 5, 1));
        training.setTrainingDurationMinutes(60);
        training.setTrainer(trainer);
        training.setTrainee(trainee);
        training.setTrainingType(trainingType);

        TrainingResponse response = mapper.toResponse(training);

        assertNotNull(response);
        assertEquals("Morning Workout", response.getTrainingName());
        assertEquals(LocalDate.of(2025, 5, 1), response.getTrainingDate());
        assertEquals(TrainingTypeName.STRENGTH, response.getTrainingType());
        assertEquals(60, response.getTrainingDuration());
        assertEquals("John Doe", response.getTrainerName());
        assertEquals("Jane Smith", response.getTraineeName());
    }

    @Test
    void shouldMapTrainingListToResponseList() {

        User trainerUser = new User();
        trainerUser.setFirstName("John");
        trainerUser.setLastName("Doe");

        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);

        User traineeUser = new User();
        traineeUser.setFirstName("Jane");
        traineeUser.setLastName("Smith");

        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);

        TrainingType trainingType = new TrainingType();
        ReflectionTestUtils.setField(trainingType, "trainingTypeName", TrainingTypeName.CARDIO);

        Training training = new Training();
        training.setTrainingName("Test Training");
        training.setTrainer(trainer);
        training.setTrainee(trainee);
        training.setTrainingType(trainingType);
        training.setTrainingDurationMinutes(30);

        List<TrainingResponse> responses = mapper.toResponseList(List.of(training));

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test Training", responses.get(0).getTrainingName());
    }
}