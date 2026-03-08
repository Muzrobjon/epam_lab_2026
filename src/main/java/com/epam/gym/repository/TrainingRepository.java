package com.epam.gym.repository;

import com.epam.gym.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>, JpaSpecificationExecutor<Training> {

    List<Training> findByTraineeUserName(String traineeUsername);

    List<Training> findByTrainerUserName(String trainerUsername);
}