package com.epam.gym.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"trainings", "trainers"})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trainees")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("TRAINEE")
public class Trainee extends User {

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    private String address;

    @OneToMany(mappedBy = "trainee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Training> trainings = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    @Builder.Default
    private List<Trainer> trainers = new ArrayList<>();

    // Helper methods for bidirectional relationships
    public void addTrainer(Trainer trainer) {
        if (!this.trainers.contains(trainer)) {
            this.trainers.add(trainer);
            trainer.getTrainees().add(this);
        }
    }

    public void removeTrainer(Trainer trainer) {
        this.trainers.remove(trainer);
        trainer.getTrainees().remove(this);
    }

    public void addTraining(Training training) {
        trainings.add(training);
        training.setTrainee(this);
    }

    public void removeTraining(Training training) {
        trainings.remove(training);
        training.setTrainee(null);
    }
}