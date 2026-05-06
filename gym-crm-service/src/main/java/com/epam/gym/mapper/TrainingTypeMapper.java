package com.epam.gym.mapper;

import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.entity.TrainingType;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {

    TrainingTypeResponse toResponse(TrainingType trainingType);

    List<TrainingTypeResponse> toResponseList(List<TrainingType> trainingTypes);
}