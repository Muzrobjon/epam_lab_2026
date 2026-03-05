package com.epam.gym.repository;

import com.epam.gym.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO:
//  1. Here and in other repositories, please double-check if you need a @Query annotation
//  or if Spring Data JPA can derive the query from the method name. In general,
//  first approach should be to rely on framework. It is mature enough to cover most of the cases.
//  Use @Query when the query cannot be expressed cleanly via method name
//  (e.g., complex joins/subqueries/projections) or when you need explicit fetch tuning.
//  2. Good job using Criteria API! Couple notes on that:
//  - Please consider more canonical setup via the JpaSpecificationExecutor - you would not need to go
//  at a lower level with entityManager.createQuery
//  - This is just a place when @Query can be useful:) We can get replace couple classes of yours with just
//  @Query("""
//                select t from Training t
//                where (:trainerUsername is null or t.trainer.user.username = :trainerUsername)
//                  and (:traineeUsername is null or t.trainee.user.username = :traineeUsername)
//                  and (:fromDate is null or t.date >= :fromDate)
//                  and (:toDate is null or t.date <= :toDate)
//            """)
@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>, TrainingRepositoryCustom {

    @Query("SELECT t FROM Training t WHERE t.trainee.userName = :username")
    List<Training> findByTraineeUsername(@Param("username") String username);

    @Query("SELECT t FROM Training t WHERE t.trainer.userName = :username")
    List<Training> findByTrainerUsername(@Param("username") String username);
}