package com.epam;

import com.epam.gym.config.AppConfig;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

@Slf4j
public class GymCrmApplication {

    public static void main(String[] args) {
        log.info("Starting Gym CRM Application with Hibernate...");

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        GymFacade facade = context.getBean(GymFacade.class);
        demonstrateApplication(facade);

        context.close();
        log.info("Application stopped");
    }

    private static void demonstrateApplication(GymFacade facade) {
        // 1. Create Trainee profile
        System.out.println("1. Creating Trainee Profile:");
        Trainee trainee = facade.createTrainee("John", "Doe",
                LocalDate.of(1995, 3, 20), "789 Pine St, Chicago");
        System.out.println("   Created: " + trainee.getUserName() + " (Password: " + trainee.getPassword() + ")");

        // 2. Create Trainer profile
        System.out.println("\n2. Creating Trainer Profile:");
        Trainer trainer = facade.createTrainer("Alice", "Fitness", TrainingTypeName.CARDIO);
        System.out.println("   Created: " + trainer.getUserName() + " (Password: " + trainer.getPassword() + ")");

        Trainer trainer2 = facade.createTrainer("Bob", "Strong", TrainingTypeName.STRENGTH);
        System.out.println("   Created: " + trainer2.getUserName() + " (Password: " + trainer2.getPassword() + ")");

        // 3 & 4. Authentication
        System.out.println("\n3 & 4. Authentication:");
        facade.authenticateTrainee(trainee.getUserName(), trainee.getPassword());
        System.out.println("   Trainee authenticated successfully!");
        facade.authenticateTrainer(trainer.getUserName(), trainer.getPassword());
        System.out.println("   Trainer authenticated successfully!");

        // 5 & 6. Select profiles by username
        System.out.println("\n5 & 6. Select Profiles by Username:");
        Trainee foundTrainee = facade.getTraineeByUsername(trainee.getUserName());
        System.out.println("   Found Trainee: " + foundTrainee.getFirstName() + " " + foundTrainee.getLastName());
        Trainer foundTrainer = facade.getTrainerByUsername(trainer.getUserName());
        System.out.println("   Found Trainer: " + foundTrainer.getFirstName() + " " + foundTrainer.getLastName());

        // 7 & 8. Password change
        System.out.println("\n7 & 8. Password Change:");
        String oldPassword = trainee.getPassword();
        String newPassword = "newpassword123";
        facade.changeTraineePassword(trainee.getUserName(), oldPassword, newPassword);
        System.out.println("   Trainee password changed successfully!");

        // 9 & 10. Update profiles
        System.out.println("\n9 & 10. Update Profiles:");
        Trainee updatedTrainee = Trainee.builder()
                .firstName("Johnny")
                .address("Updated Address, New York")
                .build();
        facade.updateTrainee(trainee.getUserName(), newPassword, updatedTrainee);
        System.out.println("   Trainee profile updated!");

        // 11 & 12. Activate/De-activate
        System.out.println("\n11 & 12. Toggle Active Status:");
        facade.toggleTraineeStatus(trainee.getUserName(), newPassword);
        System.out.println("   Trainee status toggled!");


        // 16. Add training
        System.out.println("\n16. Add Training:");
        Training training = facade.createTraining(
                trainee.getUserName(), newPassword,
                trainer.getUserName(), trainer.getPassword(),
                "Morning Cardio Blast",
                TrainingTypeName.CARDIO,
                LocalDate.now(),
                45
        );
        System.out.println("   Created Training: " + training.getTrainingName() + " (ID: " + training.getId() + ")");

        // Add another training for criteria testing
        Training training2 = facade.createTraining(
                trainee.getUserName(), newPassword,
                trainer2.getUserName(), trainer2.getPassword(),
                "Evening Strength",
                TrainingTypeName.STRENGTH,
                LocalDate.now().minusDays(1),
                60
        );
        System.out.println("   Created Training: " + training2.getTrainingName() + " (ID: " + training2.getId() + ")");

        // 14. Get Trainee Trainings by Criteria
        System.out.println("\n14. Get Trainee Trainings by Criteria:");
        List<Training> traineeTrainings = facade.getTraineeTrainingsByCriteria(
                trainee.getUserName(), newPassword,
                LocalDate.now().minusDays(7), LocalDate.now().plusDays(7),
                null, null
        );
        System.out.println("   Found " + traineeTrainings.size() + " trainings:");
        traineeTrainings.forEach(t -> System.out.println("     - " + t.getTrainingName() + " (" + t.getTrainingType() + ")"));

        // 15. Get Trainer Trainings by Criteria
        System.out.println("\n15. Get Trainer Trainings by Criteria:");

        List<Training> trainerTrainings = facade.getTrainerTrainingsByCriteria(
                trainer.getUserName(), trainer.getPassword(),
                LocalDate.now().minusDays(7), LocalDate.now().plusDays(7),
                null
        );
        System.out.println("   Found " + trainerTrainings.size() + " trainings");

        // 17. Get unassigned trainers
        System.out.println("\n17. Get Unassigned Trainers:");
        Trainee newTrainee = facade.createTrainee("Jane", "Smith",
                LocalDate.of(1990, 5, 15), "456 Oak Ave");
        System.out.println("   Created new trainee: " + newTrainee.getUserName());
        List<Trainer> unassignedTrainers = facade.getUnassignedTrainers(newTrainee.getUserName());
        System.out.println("   Unassigned trainers for " + newTrainee.getUserName() + ": " + unassignedTrainers.size());
        unassignedTrainers.forEach(t -> System.out.println("     - " + t.getUserName()));

        // 18. Update Trainee's trainers list
        System.out.println("\n18. Update Trainee's Trainers List:");
        facade.updateTraineeTrainersList(newTrainee.getUserName(), newTrainee.getPassword(),
                List.of(trainer.getUserName()));
        System.out.println("   Assigned trainer " + trainer.getUserName() + " to trainee " + newTrainee.getUserName());

        // Check unassigned trainers again
        List<Trainer> unassignedAfter = facade.getUnassignedTrainers(newTrainee.getUserName());
        System.out.println("   Unassigned trainers after update: " + unassignedAfter.size());

        // 13. Delete trainee profile
        System.out.println("\n13. Delete Trainee Profile:");
        Trainee traineeToDelete = facade.createTrainee("Delete", "Me",
                LocalDate.of(2000, 1, 1), "To be deleted");
        System.out.println("   Created trainee to delete: " + traineeToDelete.getUserName());
        facade.deleteTrainee(traineeToDelete.getUserName(), traineeToDelete.getPassword());
        System.out.println("   Trainee deleted successfully!");

    }
}