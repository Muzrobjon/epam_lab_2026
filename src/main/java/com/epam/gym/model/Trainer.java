package com.epam.gym.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"trainings", "trainees"})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trainers")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("TRAINER")
public class Trainer extends User {

    @NotNull(message = "Specialization is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingType specialization;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Training> trainings = new ArrayList<>();

    @ManyToMany(mappedBy = "trainers", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Trainee> trainees = new ArrayList<>();

    // Helper methods
    public void addTraining(Training training) {
        trainings.add(training);
        training.setTrainer(this);
    }

    public void removeTraining(Training training) {
        trainings.remove(training);
        training.setTrainer(null);
    }
}