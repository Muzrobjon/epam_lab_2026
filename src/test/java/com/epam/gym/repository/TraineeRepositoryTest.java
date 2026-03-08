package com.epam.gym.repository;

import com.epam.gym.config.TestJpaConfig;
import com.epam.gym.model.Trainee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestJpaConfig.class)
@Transactional
class TraineeRepositoryTest {

    @Autowired
    private TraineeRepository traineeRepository;

    @Test
    void testFindByUserName() {
        Trainee trainee = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .password("password")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Somewhere")
                .isActive(true)
                .build();

        trainee = traineeRepository.save(trainee);

        Optional<Trainee> found = traineeRepository.findByUserName("johndoe");
        assertTrue(found.isPresent());
        assertEquals(trainee.getId(), found.get().getId());
    }

    @Test
    void testExistsByUserName() {
        Trainee trainee = Trainee.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("janesmith")
                .password("password")
                .dateOfBirth(LocalDate.of(1999, 2, 2))
                .address("Anywhere")
                .isActive(true)
                .build();

        traineeRepository.save(trainee);

        assertTrue(traineeRepository.existsByUserName("janesmith"));
        assertFalse(traineeRepository.existsByUserName("notfound"));
    }
}