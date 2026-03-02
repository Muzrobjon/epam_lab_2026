package com.epam.gym.repository;

import com.epam.gym.model.Training;

import java.time.LocalDate;
import java.util.List;

public interface TrainingRepositoryCustom {

    List<Training> findTraineeTrainingsByCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingTypeName
    );

    List<Training> findTrainerTrainingsByCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    );
}