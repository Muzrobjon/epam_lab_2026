package com.epam.gym.repository;

import com.epam.gym.model.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUserName(String username);

    boolean existsByUserName(String username);

    @Query("SELECT COUNT(t) FROM Trainee t WHERE t.userName LIKE :pattern")
    long countByUserNamePattern(@Param("pattern") String pattern);

    void deleteByUserName(String username);
}