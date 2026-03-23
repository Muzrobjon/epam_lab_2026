package com.epam.gym.mapper;

import com.epam.gym.dto.response.TraineeSummaryResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainerSummaryResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "specialization.trainingTypeName", target = "specialization")
    @Mapping(source = "user.isActive", target = "isActive")
    @Mapping(source = "trainees", target = "trainees")
    TrainerProfileResponse toProfileResponse(Trainer trainer);

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "specialization.trainingTypeName", target = "specialization")
    TrainerSummaryResponse toSummaryResponse(Trainer trainer);

    List<TrainerSummaryResponse> toSummaryResponseList(List<Trainer> trainers);

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    TraineeSummaryResponse traineeToSummary(Trainee trainee);

    List<TraineeSummaryResponse> traineesToSummaryList(List<Trainee> trainees);
}