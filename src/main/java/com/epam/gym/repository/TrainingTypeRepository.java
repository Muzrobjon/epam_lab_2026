package com.epam.gym.repository;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.model.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {
    Optional<TrainingType> findByTrainingTypeName(TrainingTypeName trainingTypeName);
}