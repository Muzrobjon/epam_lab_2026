package com.epam.gym.repository;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.model.Training;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TrainingSpecification {

    private TrainingSpecification() {}

    public static Specification<Training> findTraineeTrainingsByCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            TrainingTypeName trainingTypeName) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (traineeUsername != null && !traineeUsername.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("trainee").get("userName"), traineeUsername));
            }
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("trainingDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("trainingDate"), toDate));
            }
            if (trainerName != null && !trainerName.isBlank()) {
                String pattern = "%" + trainerName.toLowerCase() + "%";
                Predicate firstNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("trainer").get("firstName")),
                        pattern
                );
                Predicate lastNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("trainer").get("lastName")),
                        pattern
                );
                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch));
            }
            if (trainingTypeName != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("trainingType"),
                        trainingTypeName
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Training> findTrainerTrainingsByCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (trainerUsername != null && !trainerUsername.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("trainer").get("userName"), trainerUsername));
            }
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("trainingDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("trainingDate"), toDate));
            }
            if (traineeName != null && !traineeName.isBlank()) {
                String pattern = "%" + traineeName.toLowerCase() + "%";
                Predicate firstNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("trainee").get("firstName")),
                        pattern
                );
                Predicate lastNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("trainee").get("lastName")),
                        pattern
                );
                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}