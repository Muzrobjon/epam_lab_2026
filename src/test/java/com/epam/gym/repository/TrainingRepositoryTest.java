package com.epam.gym.repository;

import com.epam.gym.config.TestJpaConfig;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
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
class TrainingRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Test
    void testFindByTraineeUserName() {
        // Create and save trainee and trainer
        Trainee trainee = Trainee.builder()
                .firstName("Trainee")
                .lastName("One")
                .userName("trainee1")
                .password("pass")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Somewhere")
                .isActive(true)
                .build();
        trainee = traineeRepository.save(trainee);

        Trainer trainer = Trainer.builder()
                .firstName("Trainer")
                .lastName("One")
                .userName("trainer1")
                .password("pass")
                .specialization(TrainingTypeName.CARDIO)
                .isActive(true)
                .build();
        trainer = trainerRepository.save(trainer);

        // Create and save training
        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Morning Cardio")
                .trainingType(TrainingTypeName.CARDIO)
                .trainingDate(LocalDate.of(2024, 1, 1))
                .trainingDurationMinutes(60)
                .build();
        trainingRepository.save(training);

        // Test repository method
        List<Training> trainings = trainingRepository.findByTraineeUserName("trainee1");
        assertEquals(1, trainings.size());
        assertEquals("Morning Cardio", trainings.get(0).getTrainingName());
    }

    @Test
    void testFindByTrainerUserName() {
        // Create and save trainee and trainer
        Trainee trainee = Trainee.builder()
                .firstName("Trainee")
                .lastName("Two")
                .userName("trainee2")
                .password("pass")
                .dateOfBirth(LocalDate.of(2000, 2, 2))
                .address("Anywhere")
                .isActive(true)
                .build();
        trainee = traineeRepository.save(trainee);

        Trainer trainer = Trainer.builder()
                .firstName("Trainer")
                .lastName("Two")
                .userName("trainer2")
                .password("pass")
                .specialization(TrainingTypeName.STRENGTH)
                .isActive(true)
                .build();
        trainer = trainerRepository.save(trainer);

        // Create and save training
        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Evening Strength")
                .trainingType(TrainingTypeName.STRENGTH)
                .trainingDate(LocalDate.of(2024, 2, 2))
                .trainingDurationMinutes(45)
                .build();
        trainingRepository.save(training);

        // Test repository method
        List<Training> trainings = trainingRepository.findByTrainerUserName("trainer2");
        assertEquals(1, trainings.size());
        assertEquals("Evening Strength", trainings.get(0).getTrainingName());
    }
}