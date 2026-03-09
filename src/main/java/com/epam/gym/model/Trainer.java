package com.epam.gym.model;

import com.epam.gym.enums.TrainingTypeName;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trainers")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("TRAINER")
public class Trainer extends User {

    // TODO:
    //  Since TrainingType is a separate entity, Training should reference TrainingType via association,
    //  not store TrainingTypeName enum directly. Right now the entity exists, but the mapping still uses the enum,
    //  so the database model is not fully aligned with the app - the approaches are mixed
    @NotNull(message = "Specialization is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "specialization", nullable = false)
    private TrainingTypeName specialization;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trainer)) return false;
        Trainer trainer = (Trainer) o;
        return getId() != null && getId().equals(trainer.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", userName='" + getUserName() + '\'' +
                ", isActive=" + getIsActive() +
                ", specialization=" + specialization +
                '}';
    }
}