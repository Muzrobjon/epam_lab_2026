package com.epam.gym.repository;

import com.epam.gym.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>, TrainingRepositoryCustom {

    @Query("SELECT t FROM Training t WHERE t.trainee.userName = :username")
    List<Training> findByTraineeUsername(@Param("username") String username);

    @Query("SELECT t FROM Training t WHERE t.trainer.userName = :username")
    List<Training> findByTrainerUsername(@Param("username") String username);
}