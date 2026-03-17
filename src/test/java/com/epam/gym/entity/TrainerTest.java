package com.epam.gym.entity;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerTest {

    @Test
    void builderAndGettersSettersWorkCorrectly() {
        User user = new User();
        // Use constructor instead of builder
        TrainingType specialization = new TrainingType(1L, TrainingTypeName.CARDIO);
        List<Training> trainings = new ArrayList<>();
        List<Trainee> trainees = new ArrayList<>();

        Trainer trainer = Trainer.builder()
                .id(1L)
                .user(user)
                .specialization(specialization)
                .trainings(trainings)
                .trainees(trainees)
                .build();

        assertThat(trainer.getId()).isEqualTo(1L);
        assertThat(trainer.getUser()).isEqualTo(user);
        assertThat(trainer.getSpecialization()).isEqualTo(specialization);
        assertThat(trainer.getTrainings()).isEqualTo(trainings);
        assertThat(trainer.getTrainees()).isEqualTo(trainees);
    }

    @Test
    void equalsAndHashCodeWorkCorrectly() {
        Trainer trainer1 = new Trainer();
        trainer1.setId(1L);

        Trainer trainer2 = new Trainer();
        trainer2.setId(1L);

        Trainer trainer3 = new Trainer();
        trainer3.setId(2L);

        assertThat(trainer1).isEqualTo(trainer2);
        assertThat(trainer1.hashCode()).isEqualTo(trainer2.hashCode());
        assertThat(trainer1).isNotEqualTo(trainer3);
    }

    @Test
    void toStringWorksCorrectly() {
        // Use constructor
        TrainingType specialization = new TrainingType(10L, TrainingTypeName.STRENGTH);

        Trainer trainer = new Trainer();
        trainer.setId(5L);
        trainer.setSpecialization(specialization);

        String toString = trainer.toString();

        assertThat(toString).contains("Trainer{");
        assertThat(toString).contains("id=5");
        assertThat(toString).contains("specialization=");
    }

    @Test
    void defaultConstructorAndSettersWorkCorrectly() {
        Trainer trainer = new Trainer();
        trainer.setId(7L);
        User user = new User();
        trainer.setUser(user);
        // Use constructor
        TrainingType specialization = new TrainingType(1L, TrainingTypeName.YOGA);
        trainer.setSpecialization(specialization);

        assertThat(trainer.getId()).isEqualTo(7L);
        assertThat(trainer.getUser()).isEqualTo(user);
        assertThat(trainer.getSpecialization()).isEqualTo(specialization);
    }
}