package com.epam.gym.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trainings")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long trainingId;

    @NotNull(message = "Trainee is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", nullable = false)
    @ToString.Exclude
    private Trainee trainee;

    @NotNull(message = "Trainer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    @ToString.Exclude
    private Trainer trainer;

    @NotBlank(message = "Training name is required")
    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @NotNull(message = "Training type is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType trainingType;

    @NotNull(message = "Training date is required")
    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    @Column(name = "training_duration_minutes", nullable = false)
    private Integer trainingDurationMinutes;
}