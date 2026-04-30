package com.epam.gym.trainerworkloadservice.repository;

import com.epam.gym.trainerworkloadservice.entity.TrainingMonth;
import com.epam.gym.trainerworkloadservice.entity.TrainingYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingMonthRepository extends JpaRepository<TrainingMonth, Long> {
    Optional<TrainingMonth> findByTrainingYearAndMonth(TrainingYear year, Integer month);
}