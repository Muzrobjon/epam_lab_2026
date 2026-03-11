package com.epam.gym.repository;

import com.epam.gym.config.TestJpaConfig;
import com.epam.gym.entity.*;
import com.epam.gym.enums.TrainingTypeName;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestJpaConfig.class)
@Transactional
@Rollback
class TrainingRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TrainingRepository trainingRepository;

    private TrainingType cardio;
    private TrainingType yoga;
    private Trainee trainee1;
    private Trainee trainee2;
    private Trainer trainer1;
    private Trainer trainer2;

    @BeforeEach
    void setUp() {
        // TrainingType
        cardio = new TrainingType(null, TrainingTypeName.CARDIO);
        yoga = new TrainingType(null, TrainingTypeName.YOGA);
        em.persist(cardio);
        em.persist(yoga);

        // Trainee 1
        User traineeUser1 = createUser("John", "Doe", "john.trainee", true);
        trainee1 = createTrainee(traineeUser1);

        // Trainee 2
        User traineeUser2 = createUser("Jane", "Smith", "jane.trainee", true);
        trainee2 = createTrainee(traineeUser2);

        // Trainer 1
        User trainerUser1 = createUser("Mike", "Coach", "mike.trainer", true);
        trainer1 = createTrainer(trainerUser1, cardio);

        // Trainer 2
        User trainerUser2 = createUser("Sara", "Fit", "sara.trainer", true);
        trainer2 = createTrainer(trainerUser2, yoga);

        em.flush();
    }

    // ============ findTrainingsWithAllUsers ============

    @Test
    void findTrainings_noFilters_returnsAll() {
        createTraining(trainee1, trainer1, cardio, "Morning Cardio", LocalDate.now(), 60);
        createTraining(trainee2, trainer2, yoga, "Evening Yoga", LocalDate.now(), 45);
        em.flush();
        em.clear();

        List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                null, null, null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void findTrainings_byTraineeUsername() {
        createTraining(trainee1, trainer1, cardio, "Cardio 1", LocalDate.now(), 60);
        createTraining(trainee2, trainer2, yoga, "Yoga 1", LocalDate.now(), 45);
        em.flush();
        em.clear();

        List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                "john.trainee", null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainee().getUser().getUsername()).isEqualTo("john.trainee");
    }

    @Test
    void findTrainings_byTrainerUsername() {
        createTraining(trainee1, trainer1, cardio, "Cardio 1", LocalDate.now(), 60);
        createTraining(trainee2, trainer2, yoga, "Yoga 1", LocalDate.now(), 45);
        em.flush();
        em.clear();

        List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                null, "sara.trainer", null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainer().getUser().getUsername()).isEqualTo("sara.trainer");
    }

    @Test
    void findTrainings_byDateRange() {
        createTraining(trainee1, trainer1, cardio, "Old", LocalDate.now().minusDays(10), 60);
        createTraining(trainee1, trainer1, cardio, "Recent", LocalDate.now().minusDays(2), 60);
        createTraining(trainee1, trainer1, cardio, "Today", LocalDate.now(), 60);
        em.flush();
        em.clear();

        LocalDate fromDate = LocalDate.now().minusDays(5);
        LocalDate toDate = LocalDate.now();

        List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                null, null, fromDate, toDate);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Training::getTrainingName)
                .containsExactlyInAnyOrder("Recent", "Today");
    }

    @Test
    void findTrainings_byAllFilters() {
        createTraining(trainee1, trainer1, cardio, "Target", LocalDate.now(), 60);
        createTraining(trainee1, trainer2, yoga, "Wrong trainer", LocalDate.now(), 45);
        createTraining(trainee2, trainer1, cardio, "Wrong trainee", LocalDate.now(), 60);
        em.flush();
        em.clear();

        List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                "john.trainee", "mike.trainer",
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainingName()).isEqualTo("Target");
    }

    @Test
    void findTrainings_eagerlyFetchesAllRelations() {
        createTraining(trainee1, trainer1, cardio, "Test", LocalDate.now(), 60);
        em.flush();
        em.clear();

        List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                null, null, null, null);

        assertThat(result).hasSize(1);
        Training training = result.get(0);

        // EntityGraph tufayli LazyInitializationException bo'lmasligi kerak
        assertThat(training.getTrainee().getUser().getUsername()).isEqualTo("john.trainee");
        assertThat(training.getTrainer().getUser().getUsername()).isEqualTo("mike.trainer");
        assertThat(training.getTrainingType().getTrainingTypeName()).isEqualTo(TrainingTypeName.CARDIO);
    }

    @Test
    void findTrainings_returnsEmptyWhenNoMatch() {
        createTraining(trainee1, trainer1, cardio, "Test", LocalDate.now(), 60);
        em.flush();
        em.clear();

        List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                "nonexistent", null, null, null);

        assertThat(result).isEmpty();
    }

    // ============ JPA Default Methods ============

    @Test
    void save_createsTraining() {
        Training training = Training.builder()
                .trainee(trainee1)
                .trainer(trainer1)
                .trainingType(cardio)
                .trainingName("New Training")
                .trainingDate(LocalDate.now())
                .trainingDurationMinutes(60)
                .build();

        Training saved = trainingRepository.save(training);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findById_found() {
        Training training = createTraining(trainee1, trainer1, cardio, "Test", LocalDate.now(), 60);
        em.flush();

        assertThat(trainingRepository.findById(training.getId())).isPresent();
    }

    @Test
    void delete_removesTraining() {
        Training training = createTraining(trainee1, trainer1, cardio, "Test", LocalDate.now(), 60);
        Long id = training.getId();
        em.flush();

        trainingRepository.deleteById(id);
        em.flush();

        assertThat(trainingRepository.findById(id)).isEmpty();
    }

    // ============ Helper Methods ============

    private User createUser(String firstName, String lastName, String username, boolean active) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password("pass123")
                .isActive(active)
                .build();
        em.persist(user);
        return user;
    }

    private Trainee createTrainee(User user) {
        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Address")
                .trainers(new ArrayList<>())
                .build();
        em.persist(trainee);
        return trainee;
    }

    private Trainer createTrainer(User user, TrainingType spec) {
        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(spec)
                .build();
        em.persist(trainer);
        return trainer;
    }

    private Training createTraining(Trainee trainee, Trainer trainer, TrainingType type,
                                    String name, LocalDate date, int duration) {
        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(type)
                .trainingName(name)
                .trainingDate(date)
                .trainingDurationMinutes(duration)
                .build();
        em.persist(training);
        return training;
    }
}