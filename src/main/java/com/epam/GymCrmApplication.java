package com.epam;

import com.epam.gym.config.AppConfig;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

@Slf4j
public class GymCrmApplication {
    public static void main(String[] args) {
        log.info("Starting Gym CRM Application...");

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        GymFacade facade = context.getBean(GymFacade.class);
        demonstrateApplication(facade);

        context.close();
        log.info("Application stopped");
    }

    private static void demonstrateApplication(GymFacade facade) {
        System.out.println("\n=== Gym CRM System Demo ===\n");

        System.out.println("Loaded Trainees:");
        facade.getAllTrainees().forEach(t ->
                System.out.printf("  - %s %s (%s)%n",
                        t.getFirstName(), t.getLastName(), t.getUserName()));

        System.out.println("\nLoaded Trainers:");
        facade.getAllTrainers().forEach(t ->
                System.out.printf("  - %s %s (%s) - %s%n",
                        t.getFirstName(), t.getLastName(), t.getUserName(), t.getSpecialization()));

        System.out.println("\nCreating new trainee:");
        Trainee newTrainee = facade.createTrainee("John", "Doe",
                LocalDate.of(1995, 3, 20), "789 Pine St, Chicago");
        System.out.printf("  Created: %s%n", newTrainee.getUserName());

        System.out.println("\nCreating new trainer:");
        Trainer newTrainer = facade.createTrainer("Alice", "Fitness", "Cardio");
        System.out.printf("  Created: %s%n", newTrainer.getUserName());

        System.out.println("\nCreating new training:");
        Training training = facade.createTraining(
                newTrainee.getUserId(),
                newTrainer.getUserId(),
                "Cardio Blast",
                TrainingType.builder().trainingTypeName("Cardio").build(),
                LocalDate.now(),
                45
        );
        System.out.printf("  Created: %s (ID: %d)%n",
                training.getTrainingName(), training.getTrainingId());

        System.out.println("\n=== Demo Complete ===");
    }
}
