package com.epam.gym.repository;

import com.epam.gym.model.Trainer;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // TODO:
    //  [Optional]
    //  This looks correct. As an alternative, consider whether this can be expressed with a derived query method,
    //  which is often less error-prone, easier to read and maintain than custom JPQL.
    //  For example: findByTraineesNotContaining(Trainee trainee)
    @Query("SELECT t FROM Trainer t WHERE t.id NOT IN " +
            "(SELECT tr.id FROM Trainee te JOIN te.trainers tr WHERE te.userName = :traineeUsername)")
    List<Trainer> findUnassignedTrainersByTraineeUsername(@Param("traineeUsername") String traineeUsername);
}