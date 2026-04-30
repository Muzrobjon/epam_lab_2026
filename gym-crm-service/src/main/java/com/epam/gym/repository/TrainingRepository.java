package com.epam.gym.repository;

import com.epam.gym.entity.Training;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    @EntityGraph(attributePaths = {
            "trainingType",
            "trainee",
            "trainee.user",
            "trainer",
            "trainer.user"
    })
    @Query("""
            SELECT t FROM Training t
            WHERE (:traineeUsername IS NULL OR t.trainee.user.username = :traineeUsername)
            AND (:trainerUsername IS NULL OR t.trainer.user.username = :trainerUsername)
            AND t.trainingDate >= COALESCE(:fromDate, t.trainingDate)
            AND t.trainingDate <= COALESCE(:toDate, t.trainingDate)
            """)
    List<Training> findTrainingsWithAllUsers(
            @Param("traineeUsername") String traineeUsername,
            @Param("trainerUsername") String trainerUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}