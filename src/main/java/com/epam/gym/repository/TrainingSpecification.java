package com.epam.gym.repository;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.entity.Training;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TrainingSpecification {

    private TrainingSpecification() {
    }

    public static Specification<Training> findTraineeTrainingsByCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            TrainingTypeName trainingTypeName) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (traineeUsername != null && !traineeUsername.isBlank()) {
                predicates.add(cb.equal(root.get("trainee").get("user").get("username"), traineeUsername));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), toDate));
            }
            if (trainerName != null && !trainerName.isBlank()) {
                String pattern = "%" + trainerName.toLowerCase() + "%";
                Predicate firstNameMatch = cb.like(cb.lower(root.get("trainer").get("user").get("firstName")), pattern);
                Predicate lastNameMatch = cb.like(cb.lower(root.get("trainer").get("user").get("lastName")), pattern);
                predicates.add(cb.or(firstNameMatch, lastNameMatch));
            }
            if (trainingTypeName != null) {
                predicates.add(cb.equal(root.get("trainingType"), trainingTypeName));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Training> findTrainerTrainingsByCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (trainerUsername != null && !trainerUsername.isBlank()) {
                predicates.add(cb.equal(root.get("trainer").get("user").get("username"), trainerUsername));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), toDate));
            }
            if (traineeName != null && !traineeName.isBlank()) {
                String pattern = "%" + traineeName.toLowerCase() + "%";
                Predicate firstNameMatch = cb.like(cb.lower(root.get("trainee").get("user").get("firstName")), pattern);
                Predicate lastNameMatch = cb.like(cb.lower(root.get("trainee").get("user").get("lastName")), pattern);
                predicates.add(cb.or(firstNameMatch, lastNameMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}