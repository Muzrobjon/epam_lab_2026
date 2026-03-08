package com.epam.gym.repository;

import com.epam.gym.config.TestJpaConfig;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestJpaConfig.class)
@Transactional
class TrainerRepositoryTest {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Test
    void testFindByUserName() {
        Trainer trainer = Trainer.builder()
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .password("password")
                .specialization(TrainingTypeName.CARDIO)
                .isActive(true)
                .build();

        trainer = trainerRepository.save(trainer);

        Optional<Trainer> found = trainerRepository.findByUserName("johndoe");
        assertTrue(found.isPresent());
        assertEquals(trainer.getId(), found.get().getId());
    }

    @Test
    void testExistsByUserName() {
        Trainer trainer = Trainer.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("janesmith")
                .password("password")
                .specialization(TrainingTypeName.STRENGTH)
                .isActive(true)
                .build();

        trainerRepository.save(trainer);

        assertTrue(trainerRepository.existsByUserName("janesmith"));
        assertFalse(trainerRepository.existsByUserName("notfound"));
    }

    @Test
    void testFindUnassignedTrainersByTraineeUsername() {
        // Create trainers
        Trainer trainer1 = Trainer.builder()
                .firstName("Trainer1")
                .lastName("One")
                .userName("trainer1")
                .password("pass1")
                .specialization(TrainingTypeName.CARDIO)
                .isActive(true)
                .build();

        Trainer trainer2 = Trainer.builder()
                .firstName("Trainer2")
                .lastName("Two")
                .userName("trainer2")
                .password("pass2")
                .specialization(TrainingTypeName.STRENGTH)
                .isActive(true)
                .build();

        trainer1 = trainerRepository.save(trainer1);
        trainer2 = trainerRepository.save(trainer2);

        // Create trainee and assign only trainer1
        Trainee trainee = Trainee.builder()
                .firstName("Trainee")
                .lastName("Test")
                .userName("trainee")
                .password("password")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Somewhere")
                .isActive(true)
                .build();

        trainee.getTrainers().add(trainer1);
        traineeRepository.save(trainee);

        // Now, trainer2 should be unassigned for this trainee
        List<Trainer> unassigned = trainerRepository.findUnassignedTrainersByTraineeUsername("trainee");
        assertEquals(1, unassigned.size());
        assertEquals(trainer2.getId(), unassigned.get(0).getId());
    }
}