package com.epam.gym.trainerworkloadservice.mapper;

import com.epam.gym.trainerworkloadservice.dto.response.TrainerWorkloadResponse;
import com.epam.gym.trainerworkloadservice.entity.Trainer;
import com.epam.gym.trainerworkloadservice.entity.TrainingMonth;
import com.epam.gym.trainerworkloadservice.entity.TrainingYear;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerWorkloadMapper {

    @Mapping(source = "username", target = "trainerUsername")
    @Mapping(source = "firstName", target = "trainerFirstName")
    @Mapping(source = "lastName", target = "trainerLastName")
    @Mapping(source = "status", target = "trainerStatus")
    @Mapping(source = "years", target = "years")
    TrainerWorkloadResponse toResponse(Trainer trainer);

    @Mapping(source = "year", target = "year")
    @Mapping(source = "months", target = "months")
    TrainerWorkloadResponse.YearSummary toYearSummary(TrainingYear trainingYear);

    @Mapping(source = "month", target = "month")
    @Mapping(source = "duration", target = "trainingSummaryDuration")
    TrainerWorkloadResponse.MonthSummary toMonthSummary(TrainingMonth trainingMonth);
}