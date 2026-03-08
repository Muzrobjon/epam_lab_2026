package com.epam.gym.repository;

import com.epam.gym.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUserName(String username);

    boolean existsByUserName(String username);

    @Query("SELECT t FROM Trainer t WHERE t.id NOT IN " +
            "(SELECT tr.id FROM Trainee te JOIN te.trainers tr WHERE te.userName = :traineeUsername)")
    List<Trainer> findUnassignedTrainersByTraineeUsername(@Param("traineeUsername") String traineeUsername);
}