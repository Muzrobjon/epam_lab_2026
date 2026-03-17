package com.epam.gym.repository;

import com.epam.gym.entity.Trainer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    @EntityGraph(attributePaths = {"user", "specialization"})
    Optional<Trainer> findByUser_Username(String username);

    @EntityGraph(attributePaths = {"user", "specialization"})
    @Query("SELECT t FROM Trainer t JOIN t.user u WHERE u.username IN :usernames")
    List<Trainer> findByUser_UsernameIn(@Param("usernames") List<String> usernames);

    @EntityGraph(attributePaths = {"user", "specialization"})
    @Query("""
        SELECT t FROM Trainer t
        WHERE t.id NOT IN (
            SELECT tr.id FROM Trainee te
            JOIN te.trainers tr
            WHERE te.user.username = :traineeUsername
        )
        AND t.user.isActive = true
        """)
    List<Trainer> findAvailableTrainers(@Param("traineeUsername") String traineeUsername);
}