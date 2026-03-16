package com.epam.gym.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingTest {

    @Test
    void builderAndGettersSettersWorkCorrectly() {
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        TrainingType trainingType = new TrainingType();
        LocalDate date = LocalDate.of(2026, 3, 11);

        Training training = Training.builder()
                .id(1L)
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Cardio")
                .trainingType(trainingType)
                .trainingDate(date)
                .trainingDurationMinutes(60)
                .build();

        assertThat(training.getId()).isEqualTo(1L);
        assertThat(training.getTrainee()).isEqualTo(trainee);
        assertThat(training.getTrainer()).isEqualTo(trainer);
        assertThat(training.getTrainingName()).isEqualTo("Cardio");
        assertThat(training.getTrainingType()).isEqualTo(trainingType);
        assertThat(training.getTrainingDate()).isEqualTo(date);
        assertThat(training.getTrainingDurationMinutes()).isEqualTo(60);
    }

    @Test
    void equalsAndHashCodeWorkCorrectly() {
        Training training1 = new Training();
        training1.setId(1L);

        Training training2 = new Training();
        training2.setId(1L);

        Training training3 = new Training();
        training3.setId(2L);

        assertThat(training1).isEqualTo(training2);
        assertThat(training1).hasSameHashCodeAs(training2);
        assertThat(training1).isNotEqualTo(training3);
    }

    @Test
    void toStringWorksCorrectly() {
        TrainingType trainingType = new TrainingType();
        LocalDate date = LocalDate.of(2026, 3, 11);

        Training training = new Training();
        training.setId(5L);
        training.setTrainingName("Yoga");
        training.setTrainingType(trainingType);
        training.setTrainingDate(date);
        training.setTrainingDurationMinutes(45);

        String toString = training.toString();

        assertThat(toString).contains("Training{");
        assertThat(toString).contains("id=5");
        assertThat(toString).contains("trainingName='Yoga'");
        assertThat(toString).contains("trainingType=");
        assertThat(toString).contains("trainingDate=2026-03-11");
        assertThat(toString).contains("trainingDurationMinutes=45");
    }

    @Test
    void defaultConstructorAndSettersWorkCorrectly() {
        Training training = new Training();
        training.setId(7L);
        Trainee trainee = new Trainee();
        training.setTrainee(trainee);
        Trainer trainer = new Trainer();
        training.setTrainer(trainer);
        training.setTrainingName("Strength");
        TrainingType trainingType = new TrainingType();
        training.setTrainingType(trainingType);
        LocalDate date = LocalDate.of(2026, 3, 11);
        training.setTrainingDate(date);
        training.setTrainingDurationMinutes(90);

        assertThat(training.getId()).isEqualTo(7L);
        assertThat(training.getTrainee()).isEqualTo(trainee);
        assertThat(training.getTrainer()).isEqualTo(trainer);
        assertThat(training.getTrainingName()).isEqualTo("Strength");
        assertThat(training.getTrainingType()).isEqualTo(trainingType);
        assertThat(training.getTrainingDate()).isEqualTo(date);
        assertThat(training.getTrainingDurationMinutes()).isEqualTo(90);
    }
}