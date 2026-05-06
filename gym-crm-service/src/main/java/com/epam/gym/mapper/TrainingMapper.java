package com.epam.gym.mapper;

import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.entity.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(source = "trainingName", target = "trainingName")
    @Mapping(source = "trainingDate", target = "trainingDate")
    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainingDurationMinutes", target = "trainingDuration")
    @Mapping(target = "trainerName", expression = "java(getFullName(training.getTrainer().getUser().getFirstName(), training.getTrainer().getUser().getLastName()))")
    @Mapping(target = "traineeName", expression = "java(getFullName(training.getTrainee().getUser().getFirstName(), training.getTrainee().getUser().getLastName()))")
    TrainingResponse toResponse(Training training);

    List<TrainingResponse> toResponseList(List<Training> trainings);

    default String getFullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}