package com.epam.gym.repository;

import com.epam.gym.config.TestJpaConfig;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestJpaConfig.class)
@Transactional
class TrainingSpecificationTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        trainee = Trainee.builder()
                .firstName("Alice")
                .lastName("Wonderland")
                .userName("alice")
                .password("pass")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Wonderland")
                .isActive(true)
                .build();
        trainee = traineeRepository.save(trainee);

        trainer = Trainer.builder()
                .firstName("Bob")
                .lastName("Builder")
                .userName("bob")
                .password("pass")
                .specialization(TrainingTypeName.CARDIO)
                .isActive(true)
                .build();
        trainer = trainerRepository.save(trainer);

        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Morning Cardio")
                .trainingType(TrainingTypeName.CARDIO)
                .trainingDate(LocalDate.of(2024, 3, 1))
                .trainingDurationMinutes(60)
                .build();
        trainingRepository.save(training);
    }

    @Test
    void testFindTraineeTrainingsByCriteria() {
        var spec = TrainingSpecification.findTraineeTrainingsByCriteria(
                "alice",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 1),
                "Bob",
                TrainingTypeName.CARDIO
        );
        List<Training> results = trainingRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals("Morning Cardio", results.get(0).getTrainingName());
    }

    @Test
    void testFindTrainerTrainingsByCriteria() {
        var spec = TrainingSpecification.findTrainerTrainingsByCriteria(
                "bob",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 1),
                "Alice"
        );
        List<Training> results = trainingRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals("Morning Cardio", results.get(0).getTrainingName());
    }

    @Test
    void testNoResults() {
        var spec = TrainingSpecification.findTraineeTrainingsByCriteria(
                "alice",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                null,
                null
        );
        List<Training> results = trainingRepository.findAll(spec);
        assertTrue(results.isEmpty());
    }
}