package com.epam.gym.repository;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.entity.Training;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TrainingSpecification {

    private TrainingSpecification() {
    }

    private static void addDateFilters(List<Predicate> predicates,
                                       Path<LocalDate> datePath,
                                       LocalDate fromDate,
                                       LocalDate toDate,
                                       jakarta.persistence.criteria.CriteriaBuilder cb) {
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(datePath, fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(datePath, toDate));
        }
    }

    private static void addNameMatchFilter(List<Predicate> predicates,
                                           Path<String> firstNamePath,
                                           Path<String> lastNamePath,
                                           String name,
                                           jakarta.persistence.criteria.CriteriaBuilder cb) {
        if (name != null && !name.isBlank()) {
            String trimmedName = name.trim().toLowerCase();  // trim() + lowercase
            String pattern = "%" + trimmedName + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(firstNamePath), pattern),
                    cb.like(cb.lower(lastNamePath), pattern)
            ));
        }
    }


    public static Specification<Training> findTraineeTrainingsByCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            TrainingTypeName trainingTypeName) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Username with trim() and case-insensitive
            if (traineeUsername != null && !traineeUsername.isBlank()) {
                String trimmedUsername = traineeUsername.trim();  // TRIM added
                predicates.add(cb.equal(
                        cb.lower(root.get("trainee").get("user").get("username")),
                        trimmedUsername.toLowerCase()  // CASE-INSENSITIVE
                ));
            }

            // Reusable date filter
            addDateFilters(predicates, root.get("trainingDate"), fromDate, toDate, cb);

            // Reusable name match filter
            addNameMatchFilter(
                    predicates,
                    root.get("trainer").get("user").get("firstName"),
                    root.get("trainer").get("user").get("lastName"),
                    trainerName,
                    cb
            );

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

            // Username with trim() and case-insensitive
            if (trainerUsername != null && !trainerUsername.isBlank()) {
                String trimmedUsername = trainerUsername.trim();  // TRIM added
                predicates.add(cb.equal(
                        cb.lower(root.get("trainer").get("user").get("username")),
                        trimmedUsername.toLowerCase()  // CASE-INSENSITIVE
                ));
            }

            // Reusable date filter
            addDateFilters(predicates, root.get("trainingDate"), fromDate, toDate, cb);

            // Reusable name match filter
            addNameMatchFilter(
                    predicates,
                    root.get("trainee").get("user").get("firstName"),
                    root.get("trainee").get("user").get("lastName"),
                    traineeName,
                    cb
            );

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}