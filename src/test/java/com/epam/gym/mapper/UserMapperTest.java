package com.epam.gym.mapper;

import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void testToRegistrationResponse_User() {
        // Create User entity instance
        User user = User.builder()
                .username("John.Doe")
                .password("aB3$xY9@kL")
                .build();

        // Perform mapping
        RegistrationResponse response = userMapper.toRegistrationResponse(user);

        // Validate the result
        assertNotNull(response, "The response should not be null");
        assertEquals(user.getUsername(), response.getUsername(), "Username should match");
        assertEquals(user.getPassword(), response.getPassword(), "Password should match");
    }

    @Test
    void testToRegistrationResponse_Trainee() {
        // Create User entity for Trainee
        User user = User.builder()
                .username("Jane.Doe")
                .password("securePass123$")
                .build();

        // Create Trainee with the User
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        // Perform mapping
        RegistrationResponse response = userMapper.toRegistrationResponse(trainee);

        // Validate the mapping result
        assertNotNull(response, "The response should not be null");
        assertEquals(user.getUsername(), response.getUsername(), "Username should match");
        assertEquals(user.getPassword(), response.getPassword(), "Password should match");
    }

    @Test
    void testToRegistrationResponse_Trainer() {
        // Create User entity for Trainer
        User user = User.builder()
                .username("Trainer.Doe")
                .password("trainerPass#45")
                .build();

        // Create Trainer with the User
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        // Perform mapping
        RegistrationResponse response = userMapper.toRegistrationResponse(trainer);

        // Validate the mapping result
        assertNotNull(response, "The response should not be null");
        assertEquals(user.getUsername(), response.getUsername(), "Username should match");
        assertEquals(user.getPassword(), response.getPassword(), "Password should match");
    }

    @Test
    void testToRegistrationResponse_NullTrainee() {
        // Test null Trainee

        RegistrationResponse response = userMapper.toRegistrationResponse((Trainee) null);

        // The result should be null
        assertNull(response, "Response should be null for null Trainee.");
    }

    @Test
    void testToRegistrationResponse_NullTrainer() {
        // Test null Trainer

        RegistrationResponse response = userMapper.toRegistrationResponse((Trainer) null);

        // The result should be null
        assertNull(response, "Response should be null for null Trainer.");
    }

    @Test
    void testToRegistrationResponse_TraineeWithNullUser() {
        // Test Trainee with null User
        Trainee trainee = new Trainee();
        trainee.setUser(null);

        RegistrationResponse response = userMapper.toRegistrationResponse(trainee);

        // The result should be null
        assertNull(response, "Response should be null for Trainee with null User.");
    }

    @Test
    void testToRegistrationResponse_TrainerWithNullUser() {
        // Test Trainer with null User
        Trainer trainer = new Trainer();
        trainer.setUser(null);

        RegistrationResponse response = userMapper.toRegistrationResponse(trainer);

        // The result should be null
        assertNull(response, "Response should be null for Trainer with null User.");
    }
}