package com.epam.gym.repository;

import com.epam.gym.config.TestJpaConfig;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestJpaConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByUserName() {
        Trainee user = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .password("password")
                .isActive(true)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .build();

        user = userRepository.save(user);

        Optional<User> found = userRepository.findByUserName("johndoe");
        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());
        assertEquals("johndoe", found.get().getUserName());
    }

    @Test
    void testExistsByUserName() {
        Trainee user = Trainee.builder()
                .firstName("Jane")
                .lastName("Smith")
                .userName("janesmith")
                .password("password")
                .isActive(true)
                .dateOfBirth(LocalDate.of(1995, 5, 5))
                .address("456 Oak Ave")
                .build();

        userRepository.save(user);

        assertTrue(userRepository.existsByUserName("janesmith"));
        assertFalse(userRepository.existsByUserName("notfound"));
    }
}