package com.epam;

import com.epam.gym.config.AppConfig;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

@Slf4j
public class GymCrmApplication {

    public static void main(String[] args) {
        log.info("Starting Gym CRM Application - Testing All 18 Functions...");

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        GymFacade facade = context.getBean(GymFacade.class);
        demonstrateAllFunctions(facade);

        context.close();
        log.info("Application stopped");
    }

    private static void demonstrateAllFunctions(GymFacade facade) {

        String trainer1Pass;
        String trainer2Pass;
        String trainee1Pass;
        String trainee2Pass;
        String traineeToDeletePass;

        String newTraineePass = "newTraineePass123";
        String newTrainerPass = "newTrainerPass123";

        System.out.println("1️⃣  Create Trainer Profile:");
        Trainer trainer1 = facade.createTrainer("Alice", "Fitness", TrainingTypeName.CARDIO);
        trainer1Pass = getRawPassword(trainer1);
        System.out.println("    ✓ Created: " + trainer1.getUser().getUsername() +
                " | Password: " + trainer1Pass);

        Trainer trainer2 = facade.createTrainer("Bob", "Strong", TrainingTypeName.STRENGTH);
        trainer2Pass = getRawPassword(trainer2);
        System.out.println("    ✓ Created: " + trainer2.getUser().getUsername() +
                " | Password: " + trainer2Pass);

        System.out.println("\n2️⃣  Create Trainee Profile:");
        Trainee trainee1 = facade.createTrainee("John", "Doe",
                LocalDate.of(1995, 3, 20), "789 Pine St, Chicago");
        trainee1Pass = getRawPassword(trainee1);
        System.out.println("    ✓ Created: " + trainee1.getUser().getUsername() +
                " | Password: " + trainee1Pass);

        Trainee trainee2 = facade.createTrainee("Jane", "Smith",
                LocalDate.of(1998, 7, 15), "456 Oak Ave, Boston");
        trainee2Pass = getRawPassword(trainee2);
        System.out.println("    ✓ Created: " + trainee2.getUser().getUsername() +
                " | Password: " + trainee2Pass);

        System.out.println("\n3️⃣  Trainee Authentication:");
        facade.authenticateTrainee(trainee1.getUser().getUsername(), trainee1Pass);
        System.out.println("    ✓ Trainee " + trainee1.getUser().getUsername() + " authenticated successfully!");

        System.out.println("\n4️⃣  Trainer Authentication:");
        facade.authenticateTrainer(trainer1.getUser().getUsername(), trainer1Pass);
        System.out.println("    ✓ Trainer " + trainer1.getUser().getUsername() + " authenticated successfully!");

        System.out.println("\n5️⃣  Select Trainer Profile by Username:");
        Trainer foundTrainer = facade.getTrainerByUsername(trainer1.getUser().getUsername());
        System.out.println("    ✓ Found: " + foundTrainer.getUser().getFirstName() + " " +
                foundTrainer.getUser().getLastName() + " | Specialization: " + foundTrainer.getSpecialization());

        System.out.println("\n6️⃣  Select Trainee Profile by Username:");
        Trainee foundTrainee = facade.getTraineeByUsername(trainee1.getUser().getUsername());
        System.out.println("    ✓ Found: " + foundTrainee.getUser().getFirstName() + " " +
                foundTrainee.getUser().getLastName() + " | Address: " + foundTrainee.getAddress());

        System.out.println("\n7️⃣  Trainee Password Change:");
        facade.changeTraineePassword(trainee1.getUser().getUsername(), trainee1Pass, newTraineePass);
        System.out.println("    ✓ Password changed from '" + trainee1Pass + "' to '" + newTraineePass + "'");
        trainee1Pass = newTraineePass;

        System.out.println("\n8️⃣  Trainer Password Change:");
        facade.changeTrainerPassword(trainer1.getUser().getUsername(), trainer1Pass, newTrainerPass);
        System.out.println("    ✓ Password changed from '" + trainer1Pass + "' to '" + newTrainerPass + "'");
        trainer1Pass = newTrainerPass;

        System.out.println("\n9️⃣  Update Trainer Profile:");
        Trainer updatedTrainerData = Trainer.builder()
                .user(User.builder().firstName("Alicia").build())
                .build();
        Trainer updatedTrainer = facade.updateTrainer(trainer1.getUser().getUsername(), trainer1Pass, updatedTrainerData);
        System.out.println("    ✓ Updated: " + updatedTrainer.getUser().getFirstName() +
                " | Specialization: " + updatedTrainer.getSpecialization());

        System.out.println("\n🔟 Update Trainee Profile:");
        Trainee updatedTraineeData = Trainee.builder()
                .user(User.builder().firstName("Johnny").build())
                .address("New Address, New York")
                .build();
        Trainee updatedTrainee = facade.updateTrainee(trainee1.getUser().getUsername(), trainee1Pass, updatedTraineeData);
        System.out.println("    ✓ Updated: " + updatedTrainee.getUser().getFirstName() +
                " | New Address: " + updatedTrainee.getAddress());

        System.out.println("\n1️⃣1️⃣ Activate/De-activate Trainee:");
        System.out.println("    Before: isActive = " + updatedTrainee.getUser().getIsActive());
        facade.toggleTraineeStatus(trainee1.getUser().getUsername(), trainee1Pass);
        Trainee toggledTrainee = facade.getTraineeByUsername(trainee1.getUser().getUsername());
        System.out.println("    After:  isActive = " + toggledTrainee.getUser().getIsActive());

        System.out.println("\n1️⃣2️⃣ Activate/De-activate Trainer:");
        System.out.println("    Before: isActive = " + updatedTrainer.getUser().getIsActive());
        facade.toggleTrainerStatus(trainer1.getUser().getUsername(), trainer1Pass);
        Trainer toggledTrainer = facade.getTrainerByUsername(trainer1.getUser().getUsername());
        System.out.println("    After:  isActive = " + toggledTrainer.getUser().getIsActive());

        facade.toggleTraineeStatus(trainee1.getUser().getUsername(), trainee1Pass);
        facade.toggleTrainerStatus(trainer1.getUser().getUsername(), trainer1Pass);

        System.out.println("\n1️⃣6️⃣ Add Training:");
        Training training1 = facade.createTraining(
                trainee1.getUser().getUsername(), trainee1Pass,
                trainer1.getUser().getUsername(), trainer1Pass,
                "Morning Yoga Session", TrainingTypeName.YOGA,
                LocalDate.now(), 60
        );
        System.out.println("    ✓ Created: " + training1.getTrainingName() +
                " | Date: " + training1.getTrainingDate() + " | Duration: " + training1.getTrainingDurationMinutes() + " min");

        Training training2 = facade.createTraining(
                trainee1.getUser().getUsername(), trainee1Pass,
                trainer2.getUser().getUsername(), trainer2Pass,
                "Evening Strength Training", TrainingTypeName.STRENGTH,
                LocalDate.now().minusDays(1), 45
        );
        System.out.println("    ✓ Created: " + training2.getTrainingName() +
                " | Date: " + training2.getTrainingDate() + " | Duration: " + training2.getTrainingDurationMinutes() + " min");

        System.out.println("\n1️⃣4️⃣ Get Trainee Trainings List by Criteria:");
        List<Training> traineeTrainings = facade.getTraineeTrainingsByCriteria(
                trainee1.getUser().getUsername(), trainee1Pass,
                LocalDate.now().minusDays(7), LocalDate.now().plusDays(1),
                null, null
        );
        System.out.println("    ✓ Found " + traineeTrainings.size() + " training(s):");
        traineeTrainings.forEach(t -> System.out.println("      - " + t.getTrainingName() +
                " | Type: " + t.getTrainingType() + " | Date: " + t.getTrainingDate()));

        System.out.println("\n1️⃣5️⃣ Get Trainer Trainings List by Criteria:");
        List<Training> trainerTrainings = facade.getTrainerTrainingsByCriteria(
                trainer1.getUser().getUsername(), trainer1Pass,
                LocalDate.now().minusDays(7), LocalDate.now().plusDays(1),
                null
        );
        System.out.println("    ✓ Found " + trainerTrainings.size() + " training(s):");
        trainerTrainings.forEach(t -> System.out.println("      - " + t.getTrainingName() +
                " | Trainee: " + t.getTrainee().getUser().getFirstName()));

        System.out.println("\n1️⃣7️⃣ Get Unassigned Trainers:");
        List<Trainer> unassignedBefore = facade.getUnassignedTrainers(trainee2.getUser().getUsername());
        System.out.println("    ✓ Unassigned trainers for " + trainee2.getUser().getUsername() + ": " +
                unassignedBefore.size());
        unassignedBefore.forEach(t -> System.out.println("      - " + t.getUser().getUsername() +
                " | Specialization: " + t.getSpecialization()));

        System.out.println("\n1️⃣8️⃣ Update Trainee's Trainers List:");
        facade.updateTraineeTrainersList(
                trainee2.getUser().getUsername(),
                trainee2Pass,
                List.of(trainer1.getUser().getUsername())
        );
        System.out.println("    ✓ Assigned trainer " + trainer1.getUser().getUsername() +
                " to trainee " + trainee2.getUser().getUsername());

        System.out.println("\n1️⃣3️⃣ Delete Trainee Profile:");
        Trainee traineeToDelete = facade.createTrainee("Delete", "Me",
                LocalDate.of(2000, 1, 1), "To be deleted");
        traineeToDeletePass = getRawPassword(traineeToDelete);
        System.out.println("    Created trainee to delete: " + traineeToDelete.getUser().getUsername() +
                " | Password: " + traineeToDeletePass);

        facade.deleteTrainee(traineeToDelete.getUser().getUsername(), traineeToDeletePass);
        System.out.println("    ✓ Trainee " + traineeToDelete.getUser().getUsername() +
                " deleted successfully (cascade deletion of trainings)!");
    }

    private static String getRawPassword(Trainee trainee) {
        return trainee.getUser().getPassword();
    }

    private static String getRawPassword(Trainer trainer) {
        return trainer.getUser().getPassword();
    }
}