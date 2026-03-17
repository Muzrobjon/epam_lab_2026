package com.epam.gym.mapper;

import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TraineeSummaryResponse;
import com.epam.gym.dto.response.TrainerSummaryResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TraineeMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.isActive", target = "isActive")
    @Mapping(source = "trainers", target = "trainers")
    TraineeProfileResponse toProfileResponse(Trainee trainee);

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    TraineeSummaryResponse toSummaryResponse(Trainee trainee);

    List<TraineeSummaryResponse> toSummaryResponseList(List<Trainee> trainees);

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "specialization.trainingTypeName", target = "specialization")
    TrainerSummaryResponse toTrainerSummary(Trainer trainer);

    List<TrainerSummaryResponse> toTrainerSummaryList(List<Trainer> trainers);
}