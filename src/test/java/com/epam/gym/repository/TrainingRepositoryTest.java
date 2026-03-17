package com.epam.gym.repository;

import com.epam.gym.config.TestJpaConfig;
import com.epam.gym.entity.*;
import com.epam.gym.enums.TrainingTypeName;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(TestJpaConfig.class)
@Transactional
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
        cardio = findOrCreateTrainingType(TrainingTypeName.CARDIO);
        yoga = findOrCreateTrainingType(TrainingTypeName.YOGA);

        User traineeUser1 = createUser("John", "Doe", "john.trainee", true);
        trainee1 = createTrainee(traineeUser1);

        User traineeUser2 = createUser("Jane", "Smith", "jane.trainee", true);
        trainee2 = createTrainee(traineeUser2);

        User trainerUser1 = createUser("Mike", "Coach", "mike.trainer", true);
        trainer1 = createTrainer(trainerUser1, cardio);

        User trainerUser2 = createUser("Sara", "Fit", "sara.trainer", true);
        trainer2 = createTrainer(trainerUser2, yoga);

        em.flush();
    }

    private TrainingType findOrCreateTrainingType(TrainingTypeName name) {
        return em.createQuery(
                        "SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name",
                        TrainingType.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .orElseGet(() -> {
                    TrainingType type = new TrainingType(null, name);
                    em.persist(type);
                    return type;
                });
    }

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
    }

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