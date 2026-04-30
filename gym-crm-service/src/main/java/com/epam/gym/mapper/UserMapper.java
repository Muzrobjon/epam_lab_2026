package com.epam.gym.mapper;

import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserMapper {


    RegistrationResponse toRegistrationResponse(User user);

    default RegistrationResponse toRegistrationResponse(Trainee trainee) {
        if (trainee == null || trainee.getUser() == null) {
            return null;
        }
        return toRegistrationResponse(trainee.getUser());
    }

    default RegistrationResponse toRegistrationResponse(Trainer trainer) {
        if (trainer == null || trainer.getUser() == null) {
            return null;
        }
        return toRegistrationResponse(trainer.getUser());
    }
}