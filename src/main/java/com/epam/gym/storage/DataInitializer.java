package com.epam.gym.storage;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Storage storage;
    private final ObjectMapper objectMapper;

    @Value("${storage.data.file}")
    private Resource dataFile;

    @PostConstruct
    public void init() {
        try {
            if (dataFile == null || !dataFile.exists()) {
                log.warn("Data file not found: {}", dataFile);
                return;
            }

            log.info("Loading initial data from: {}", dataFile.getFilename());
            JsonNode rootNode = objectMapper.readTree(dataFile.getInputStream());

            loadTrainingTypes(rootNode.path("trainingTypes"));
            loadTrainers(rootNode.path("trainers"));
            loadTrainees(rootNode.path("trainees"));
            loadTrainings(rootNode.path("trainings"));

            log.info("Data initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize data", e);
        }
    }

    private void loadTrainingTypes(JsonNode node) {
        if (node.isMissingNode() || !node.isArray()) return;

        for (JsonNode typeNode : node) {
            TrainingType type = TrainingType.builder()
                    .trainingTypeName(typeNode.path("trainingTypeName").asText())
                    .build();

            Long id = storage.generateId("trainingTypes");
            type.setId(id);
            storage.put("trainingTypes", id, type);
        }
        log.info("Loaded {} training types", storage.count("trainingTypes"));
    }

    private void loadTrainers(JsonNode node) {
        if (node.isMissingNode() || !node.isArray()) return;

        for (JsonNode trainerNode : node) {
            String firstName = trainerNode.path("firstName").asText();
            String lastName = trainerNode.path("lastName").asText();

            Trainer trainer = Trainer.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .userName(generateUsername(firstName, lastName))
                    .isActive(trainerNode.path("active").asBoolean(true))
                    .specialization(trainerNode.path("specialization").asText())
                    .build();

            Long id = storage.generateId("trainers");
            trainer.setUserId(id);
            storage.put("trainers", id, trainer);
        }
        log.info("Loaded {} trainers", storage.count("trainers"));
    }

    private void loadTrainees(JsonNode node) {
        if (node.isMissingNode() || !node.isArray()) return;

        for (JsonNode traineeNode : node) {
            String firstName = traineeNode.path("firstName").asText();
            String lastName = traineeNode.path("lastName").asText();

            String dobStr = traineeNode.path("dateOfBirth").asText();
            LocalDate dob = dobStr.isEmpty() ? null : LocalDate.parse(dobStr, DATE_FORMATTER);

            Trainee trainee = Trainee.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .userName(generateUsername(firstName, lastName))
                    .isActive(traineeNode.path("active").asBoolean(true))
                    .dateOfBirth(dob)
                    .address(traineeNode.path("address").asText())
                    .build();

            Long id = storage.generateId("trainees");
            trainee.setUserId(id);
            storage.put("trainees", id, trainee);
        }
        log.info("Loaded {} trainees", storage.count("trainees"));
    }

    private void loadTrainings(JsonNode node) {
        if (node.isMissingNode() || !node.isArray()) return;

        for (JsonNode trainingNode : node) {
            String dateStr = trainingNode.path("trainingDate").asText();
            LocalDate date = dateStr.isEmpty() ? null : LocalDate.parse(dateStr, DATE_FORMATTER);

            String typeName = trainingNode.path("trainingType").asText();
            TrainingType type = findTrainingTypeByName(typeName);

            Training training = Training.builder()
                    .traineeId(trainingNode.path("traineeId").asLong())
                    .trainerId(trainingNode.path("trainerId").asLong())
                    .trainingName(trainingNode.path("trainingName").asText())
                    .trainingType(type)
                    .trainingDate(date)
                    .trainingDuration(trainingNode.path("trainingDuration").asInt())
                    .build();

            Long id = storage.generateId("trainings");
            training.setTrainingId(id);
            storage.put("trainings", id, training);
        }
        log.info("Loaded {} trainings", storage.count("trainings"));
    }

    private String generateUsername(String firstName, String lastName) {
        return (firstName + "." + lastName).toLowerCase();
    }

    private TrainingType findTrainingTypeByName(String name) {
        Map<Long, Object> types = storage.getAll("trainingTypes");
        for (Object obj : types.values()) {
            TrainingType type = (TrainingType) obj;
            if (type.getTrainingTypeName().equals(name)) {
                return type;
            }
        }
        return TrainingType.builder().trainingTypeName(name).build();
    }
}
