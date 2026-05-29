package com.epam.gym.integration;

import com.epam.gym.config.TestWorkloadServiceConfig;
import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.response.LoginResponse;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.enums.TrainingTypeName;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("stg")
@Import(TestWorkloadServiceConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
class InfraIntegrationTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        TestWorkloadServiceConfig.clearAllWorkloads();
        log.info("Test setup completed. Base URL: {}", baseUrl);
    }

    @Test
    void trainerAddTrainingSession_updatesWorkloadService() throws InterruptedException {
        // 1. Register trainer
        log.info("Step 1: Registering trainer");
        TrainerRegistrationRequest trainerReq = new TrainerRegistrationRequest();
        trainerReq.setFirstName("John");
        trainerReq.setLastName("Trainer");
        trainerReq.setSpecialization(TrainingTypeName.CARDIO);

        ResponseEntity<RegistrationResponse> trainerResp = restTemplate.postForEntity(
                baseUrl + "/api/trainers",
                trainerReq,
                RegistrationResponse.class
        );
        assertThat(trainerResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(trainerResp.getBody()).isNotNull();

        String trainerUsername = trainerResp.getBody().getUsername();
        String trainerPassword = trainerResp.getBody().getPassword();
        log.info("Trainer registered: username={}, password={}", trainerUsername, trainerPassword);

        // 2. Register trainee
        log.info("Step 2: Registering trainee");
        TraineeRegistrationRequest traineeReq = new TraineeRegistrationRequest();
        traineeReq.setFirstName("Mike");
        traineeReq.setLastName("Student");
        traineeReq.setDateOfBirth(LocalDate.of(2000, 1, 15));
        traineeReq.setAddress("Tashkent");

        ResponseEntity<RegistrationResponse> traineeResp = restTemplate.postForEntity(
                baseUrl + "/api/trainees",
                traineeReq,
                RegistrationResponse.class
        );
        assertThat(traineeResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(traineeResp.getBody()).isNotNull();

        String traineeUsername = traineeResp.getBody().getUsername();
        String traineePassword = traineeResp.getBody().getPassword();
        log.info("Trainee registered: username={}, password={}", traineeUsername, traineePassword);

        // 3. Login as trainee to get JWT token
        log.info("Step 3: Logging in as trainee");
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(traineeUsername);
        loginReq.setPassword(traineePassword);

        ResponseEntity<LoginResponse> loginResp = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginReq,
                LoginResponse.class
        );
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody()).isNotNull();

        String jwtToken = loginResp.getBody().getAccessToken();
        assertThat(jwtToken).isNotNull();
        log.info("Login successful, JWT token obtained");

        // 4. Add training session with authentication
        log.info("Step 4: Creating training session");
        AddTrainingRequest addTrainingReq = new AddTrainingRequest();
        addTrainingReq.setTraineeUsername(traineeUsername);
        addTrainingReq.setTrainerUsername(trainerUsername);
        addTrainingReq.setTrainingName("Integration Training");
        addTrainingReq.setTrainingDate(LocalDate.now().plusDays(2));
        addTrainingReq.setTrainingDuration(60);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<AddTrainingRequest> trainingEntity = new HttpEntity<>(addTrainingReq, headers);

        ResponseEntity<Void> trainingResp = restTemplate.postForEntity(
                baseUrl + "/api/trainings",
                trainingEntity,
                Void.class
        );
        assertThat(trainingResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        log.info("Training session created successfully");

        log.info("Step 5: Waiting for JMS message processing");
        Thread.sleep(3000);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    long workload = TestWorkloadServiceConfig.getWorkload(trainerUsername);
                    log.info("Current workload for trainer {}: {}", trainerUsername, workload);
                    assertThat(workload).isEqualTo(60L);
                });

        log.info("Test completed successfully - workload updated to 60 minutes");
    }

    @Test
    void multipleTrainings_increaseWorkload() throws InterruptedException {

        log.info("Test 2 - Step 1: Registering trainer");
        TrainerRegistrationRequest trainerReq = new TrainerRegistrationRequest();
        trainerReq.setFirstName("Jane");
        trainerReq.setLastName("Coach");
        trainerReq.setSpecialization(TrainingTypeName.FITNESS);

        ResponseEntity<RegistrationResponse> trainerResp = restTemplate.postForEntity(
                baseUrl + "/api/trainers",
                trainerReq,
                RegistrationResponse.class
        );
        assertThat(trainerResp.getBody()).isNotNull();
        String trainerUsername = trainerResp.getBody().getUsername();
        log.info("Trainer registered: {}", trainerUsername);

        // 2. Register trainee
        log.info("Test 2 - Step 2: Registering trainee");
        TraineeRegistrationRequest traineeReq = new TraineeRegistrationRequest();
        traineeReq.setFirstName("Alex");
        traineeReq.setLastName("Runner");
        traineeReq.setDateOfBirth(LocalDate.of(1995, 5, 10));
        traineeReq.setAddress("Samarkand");

        ResponseEntity<RegistrationResponse> traineeResp = restTemplate.postForEntity(
                baseUrl + "/api/trainees",
                traineeReq,
                RegistrationResponse.class
        );
        assertThat(traineeResp.getBody()).isNotNull();
        String traineeUsername = traineeResp.getBody().getUsername();
        String traineePassword = traineeResp.getBody().getPassword();
        log.info("Trainee registered: {}", traineeUsername);

        // 3. Login
        log.info("Test 2 - Step 3: Logging in");
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(traineeUsername);
        loginReq.setPassword(traineePassword);

        ResponseEntity<LoginResponse> loginResp = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginReq,
                LoginResponse.class
        );
        assertThat(loginResp.getBody()).isNotNull();
        String jwtToken = loginResp.getBody().getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken);

        // 4. Add first training (60 minutes)
        log.info("Test 2 - Step 4: Creating first training");
        AddTrainingRequest training1 = new AddTrainingRequest();
        training1.setTraineeUsername(traineeUsername);
        training1.setTrainerUsername(trainerUsername);
        training1.setTrainingName("Morning Session");
        training1.setTrainingDate(LocalDate.now().plusDays(1));
        training1.setTrainingDuration(60);

        restTemplate.postForEntity(
                baseUrl + "/api/trainings",
                new HttpEntity<>(training1, headers),
                Void.class
        );
        log.info("First training created");

        Thread.sleep(2000);

        // 5. Add second training (60 minutes)
        log.info("Test 2 - Step 5: Creating second training");
        AddTrainingRequest training2 = new AddTrainingRequest();
        training2.setTraineeUsername(traineeUsername);
        training2.setTrainerUsername(trainerUsername);
        training2.setTrainingName("Evening Session");
        training2.setTrainingDate(LocalDate.now().plusDays(2));
        training2.setTrainingDuration(60);

        restTemplate.postForEntity(
                baseUrl + "/api/trainings",
                new HttpEntity<>(training2, headers),
                Void.class
        );
        log.info("Second training created");

        Thread.sleep(3000);

        // 6. Verify total workload is 120 minutes
        log.info("Test 2 - Step 6: Verifying workload");
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    long workload = TestWorkloadServiceConfig.getWorkload(trainerUsername);
                    log.info("Total workload for trainer {}: {}", trainerUsername, workload);
                    assertThat(workload).isEqualTo(120L);
                });

        log.info("Multiple trainings test completed - total workload: 120 minutes");
    }
}