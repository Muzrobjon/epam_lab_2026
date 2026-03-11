package com.epam.gym.repository;

import com.epam.gym.config.TestJpaConfig;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(TestJpaConfig.class)
@Transactional
class TrainerRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TrainerRepository trainerRepository;

    private TrainingType cardio;
    private TrainingType yoga;

    @BeforeEach
    void setUp() {
        // Mavjud bo'lsa topish, bo'lmasa yaratish
        cardio = findOrCreateTrainingType(TrainingTypeName.CARDIO);
        yoga = findOrCreateTrainingType(TrainingTypeName.YOGA);
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
    void findByUsername_found() {
        User user = createUser("John", "Doe", "john.doe", true);
        createTrainer(user, cardio);
        em.flush();
        em.clear();

        Optional<Trainer> result = trainerRepository.findByUser_Username("john.doe");

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getFirstName()).isEqualTo("John");
    }

    @Test
    void findByUsername_notFound() {
        Optional<Trainer> result = trainerRepository.findByUser_Username("nonexistent");
        assertThat(result).isEmpty();
    }

    @Test
    void findByUsernameIn_found() {
        createTrainer(createUser("John", "Doe", "john.doe", true), cardio);
        createTrainer(createUser("Jane", "Smith", "jane.smith", true), yoga);
        em.flush();
        em.clear();

        List<Trainer> result = trainerRepository.findByUser_UsernameIn(
                List.of("john.doe", "jane.smith"));

        assertThat(result).hasSize(2);
    }

    @Test
    void findAvailableTrainers_returnsUnassigned() {
        User traineeUser = createUser("Trainee", "User", "trainee.user", true);
        Trainee trainee = createTrainee(traineeUser);

        Trainer trainer1 = createTrainer(createUser("T", "One", "t.one", true), cardio);
        Trainer trainer2 = createTrainer(createUser("T", "Two", "t.two", true), yoga);

        trainee.getTrainers().add(trainer1);
        em.merge(trainee);
        em.flush();
        em.clear();

        List<Trainer> result = trainerRepository.findAvailableTrainers("trainee.user");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getUsername()).isEqualTo("t.two");
    }

    @Test
    void findAvailableTrainers_excludesInactive() {
        User traineeUser = createUser("Trainee", "User", "trainee.user", true);
        createTrainee(traineeUser);

        createTrainer(createUser("Active", "T", "active.t", true), cardio);
        createTrainer(createUser("Inactive", "T", "inactive.t", false), yoga);
        em.flush();
        em.clear();

        List<Trainer> result = trainerRepository.findAvailableTrainers("trainee.user");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getUsername()).isEqualTo("active.t");
    }

    @Test
    void save_createsTrainer() {
        User user = createUser("New", "Trainer", "new.trainer", true);
        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(cardio)
                .build();

        Trainer saved = trainerRepository.save(trainer);

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

    private Trainer createTrainer(User user, TrainingType spec) {
        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(spec)
                .build();
        em.persist(trainer);
        return trainer;
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
}