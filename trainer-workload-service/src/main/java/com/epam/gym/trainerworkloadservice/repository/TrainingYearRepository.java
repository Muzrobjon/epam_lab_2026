package com.epam.gym.trainerworkloadservice.repository;

import com.epam.gym.trainerworkloadservice.entity.Trainer;
import com.epam.gym.trainerworkloadservice.entity.TrainingYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingYearRepository extends JpaRepository<TrainingYear, Long> {
    Optional<TrainingYear> findByTrainerAndYear(Trainer trainer, Integer year);
}