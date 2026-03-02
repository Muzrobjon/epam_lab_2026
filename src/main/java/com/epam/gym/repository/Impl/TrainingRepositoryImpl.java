package com.epam.gym.repository.Impl;

import com.epam.gym.model.Training;
import com.epam.gym.repository.TrainingRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class TrainingRepositoryImpl implements TrainingRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager; // keep private

     @Override
    public List<Training> findTraineeTrainingsByCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingTypeName) {

        log.debug("Finding trainee trainings by criteria: traineeUsername={}, fromDate={}, toDate={}, trainerName={}, trainingTypeName={}",
                traineeUsername, fromDate, toDate, trainerName, trainingTypeName);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> training = query.from(Training.class);

        Join<Object, Object> trainee = training.join("trainee");
        Join<Object, Object> trainer = training.join("trainer");
        Join<Object, Object> trainingType = training.join("trainingType");

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(trainee.get("userName"), traineeUsername));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.get("trainingDate"), fromDate));
        }

        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(training.get("trainingDate"), toDate));
        }

        if (trainerName != null && !trainerName.trim().isEmpty()) {
            String pattern = "%" + trainerName.toLowerCase() + "%";
            Predicate firstNameMatch = cb.like(cb.lower(trainer.get("firstName")), pattern);
            Predicate lastNameMatch = cb.like(cb.lower(trainer.get("lastName")), pattern);
            predicates.add(cb.or(firstNameMatch, lastNameMatch));
        }

        if (trainingTypeName != null && !trainingTypeName.trim().isEmpty()) {
            predicates.add(cb.equal(trainingType.get("trainingTypeName"), trainingTypeName));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(training.get("trainingDate")));

        List<Training> results = entityManager.createQuery(query).getResultList();
        log.debug("Found {} trainings for trainee {}", results.size(), traineeUsername);

        return results;
    }

    @Override
    public List<Training> findTrainerTrainingsByCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName) {

        log.debug("Finding trainer trainings by criteria: trainerUsername={}, fromDate={}, toDate={}, traineeName={}",
                trainerUsername, fromDate, toDate, traineeName);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> training = query.from(Training.class);

        Join<Object, Object> trainer = training.join("trainer");
        Join<Object, Object> trainee = training.join("trainee");

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(trainer.get("userName"), trainerUsername));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.get("trainingDate"), fromDate));
        }

        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(training.get("trainingDate"), toDate));
        }

        if (traineeName != null && !traineeName.trim().isEmpty()) {
            String pattern = "%" + traineeName.toLowerCase() + "%";
            Predicate firstNameMatch = cb.like(cb.lower(trainee.get("firstName")), pattern);
            Predicate lastNameMatch = cb.like(cb.lower(trainee.get("lastName")), pattern);
            predicates.add(cb.or(firstNameMatch, lastNameMatch));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(training.get("trainingDate")));

        List<Training> results = entityManager.createQuery(query).getResultList();
        log.debug("Found {} trainings for trainer {}", results.size(), trainerUsername);

        return results;
    }
}