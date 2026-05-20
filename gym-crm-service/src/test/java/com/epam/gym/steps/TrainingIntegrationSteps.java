package com.epam.gym.steps;

import com.epam.gym.config.TestWorkloadServiceConfig;
import com.epam.gym.support.ScenarioContext;
import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.response.LoginResponse;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.enums.TrainingTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Slf4j
public class TrainingIntegrationSteps {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final ScenarioContext ctx;

    // Constructor injection
    public TrainingIntegrationSteps(MockMvc mockMvc, ObjectMapper objectMapper, ScenarioContext ctx) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.ctx = ctx;
    }

    // HOOKS

    @io.cucumber.java.Before
    public void beforeScenario() {
        TestWorkloadServiceConfig.clearAllWorkloads();
        log.info("TEST: Cleared workloads before scenario");
    }

    @io.cucumber.java.After
    public void afterScenario() {
        TestWorkloadServiceConfig.clearAllWorkloads();
        log.info("TEST: Cleared workloads after scenario");
    }

    //GIVEN

    @Given("a trainer exists")
    public void aTrainerExists() throws Exception {
        TrainerRegistrationRequest req = TrainerRegistrationRequest.builder()
                .firstName("John" + uniqueSuffix())
                .lastName("Trainer")
                .specialization(TrainingTypeName.CARDIO)
                .build();

        MvcResult result = mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("Trainer registration should return 201")
                .isEqualTo(201);

        RegistrationResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationResponse.class);

        ctx.setTrainerUsername(resp.getUsername());
        ctx.setTrainerPassword(resp.getPassword());

        log.info("TEST: Created trainer with username: {}", resp.getUsername());
    }

    @Given("a trainee exists")
    public void aTraineeExists() throws Exception {
        TraineeRegistrationRequest req = TraineeRegistrationRequest.builder()
                .firstName("Mike" + uniqueSuffix())
                .lastName("Student")
                .dateOfBirth(LocalDate.of(2000, 1, 15))
                .address("Tashkent")
                .build();

        MvcResult result = mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("Trainee registration should return 201")
                .isEqualTo(201);

        RegistrationResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationResponse.class);

        ctx.setTraineeUsername(resp.getUsername());
        ctx.setTraineePassword(resp.getPassword());

        log.info("TEST: Created trainee with username: {}", resp.getUsername());
    }

    @Given("another trainee exists")
    public void anotherTraineeExists() throws Exception {
        TraineeRegistrationRequest req = TraineeRegistrationRequest.builder()
                .firstName("Other" + uniqueSuffix())
                .lastName("Trainee")
                .dateOfBirth(LocalDate.of(2001, 5, 5))
                .address("Tashkent")
                .build();

        MvcResult result = mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("Another trainee registration should return 201")
                .isEqualTo(201);

        RegistrationResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationResponse.class);

        ctx.setAnotherTraineeUsername(resp.getUsername());

        log.info("TEST: Created another trainee with username: {}", resp.getUsername());
    }

    @Given("trainee is authenticated")
    public void traineeIsAuthenticated() throws Exception {
        LoginRequest req = new LoginRequest(
                ctx.getTraineeUsername(),
                ctx.getTraineePassword());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("Login should return 200")
                .isEqualTo(200);

        LoginResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class);

        ctx.setTraineeToken("Bearer " + resp.getAccessToken());

        log.info("TEST: Trainee {} authenticated successfully", ctx.getTraineeUsername());
    }

    //  WHEN

    @When("trainee creates training with trainer")
    public void traineeCreatesTrainingWithTrainer() throws Exception {
        long initialWorkload = TestWorkloadServiceConfig.getWorkload(ctx.getTrainerUsername());

        AddTrainingRequest req = buildValidTrainingRequest(
                ctx.getTraineeUsername(),
                ctx.getTrainerUsername(),
                LocalDate.now().plusDays(2),
                60);

        performAddTraining(req, ctx.getTraineeToken());

        log.info("TEST: Training created, initial workload: {}, waiting for JMS processing...",
                initialWorkload);
    }

    @When("trainee creates another training at the same time with the same trainer")
    public void traineeCreatesAnotherTrainingSameTime() throws Exception {
        long currentWorkload = TestWorkloadServiceConfig.getWorkload(ctx.getTrainerUsername());

        AddTrainingRequest req = buildValidTrainingRequest(
                ctx.getTraineeUsername(),
                ctx.getTrainerUsername(),
                LocalDate.now().plusDays(2), //(overlap!)
                60);

        performAddTraining(req, ctx.getTraineeToken());

        log.info("TEST: Overlapping training attempt completed with status: {}, current workload: {}",
                ctx.getResponseStatus(), currentWorkload);
    }

    @When("trainee creates another training at different time with the same trainer")
    public void traineeCreatesAnotherTrainingDifferentTime() throws Exception {
        long currentWorkload = TestWorkloadServiceConfig.getWorkload(ctx.getTrainerUsername());

        AddTrainingRequest req = buildValidTrainingRequest(
                ctx.getTraineeUsername(),
                ctx.getTrainerUsername(),
                LocalDate.now().plusDays(3), 60);

        performAddTraining(req, ctx.getTraineeToken());

        log.info("TEST: Second training created, current workload: {}, waiting for JMS...",
                currentWorkload);
    }

    @When("trainee creates training with non existing trainer")
    public void traineeCreatesTrainingWithNonExistingTrainer() throws Exception {
        AddTrainingRequest req = buildValidTrainingRequest(
                ctx.getTraineeUsername(),
                "non.existing.trainer",
                LocalDate.now().plusDays(2),
                60);

        performAddTraining(req, ctx.getTraineeToken());

        log.info("TEST: Training creation with non-existing trainer completed with status: {}",
                ctx.getResponseStatus());
    }

    @When("trainee creates training with date in the past")
    public void traineeCreatesTrainingWithDateInThePast() throws Exception {
        AddTrainingRequest req = buildValidTrainingRequest(
                ctx.getTraineeUsername(),
                ctx.getTrainerUsername(),
                LocalDate.now().minusDays(5),
                60);

        performAddTraining(req, ctx.getTraineeToken());

        log.info("TEST: Training creation with past date completed with status: {}",
                ctx.getResponseStatus());
    }

    @When("trainee creates training without authentication")
    public void traineeCreatesTrainingWithoutAuthentication() throws Exception {
        AddTrainingRequest req = buildValidTrainingRequest(
                ctx.getTraineeUsername(),
                ctx.getTrainerUsername(),
                LocalDate.now().plusDays(2),
                60);

        performAddTraining(req, null);

        log.info("TEST: Training creation without auth completed with status: {}",
                ctx.getResponseStatus());
    }

    @When("trainee creates training with zero duration")
    public void traineeCreatesTrainingWithZeroDuration() throws Exception {
        AddTrainingRequest req = AddTrainingRequest.builder()
                .traineeUsername(ctx.getTraineeUsername())
                .trainerUsername(ctx.getTrainerUsername())
                .trainingName("Zero Duration Training")
                .trainingDate(LocalDate.now().plusDays(2))
                .trainingDuration(0)
                .build();

        performAddTraining(req, ctx.getTraineeToken());

        log.info("TEST: Training creation with zero duration completed with status: {}",
                ctx.getResponseStatus());
    }

    @When("trainee creates {int} trainings with {int} minutes each")
    public void traineeCreatesNTrainings(int count, int minutes) throws Exception {
        long initialWorkload = TestWorkloadServiceConfig.getWorkload(ctx.getTrainerUsername());
        int successfulTrainings = 0;

        for (int i = 0; i < count; i++) {
            AddTrainingRequest req = AddTrainingRequest.builder()
                    .traineeUsername(ctx.getTraineeUsername())
                    .trainerUsername(ctx.getTrainerUsername())
                    .trainingName("Training-" + i)
                    .trainingDate(LocalDate.now().plusDays(2 + i))
                    .trainingDuration(minutes)
                    .build();

            performAddTraining(req, ctx.getTraineeToken());

            if (ctx.getResponseStatus() == 200) {
                successfulTrainings++;
            }

            Thread.sleep(100);
        }

        log.info("TEST: Created {}/{} trainings successfully, initial workload: {}, waiting for JMS...",
                successfulTrainings, count, initialWorkload);
    }

    @When("trainee creates training using another trainee username")
    public void traineeCreatesTrainingUsingAnotherTraineeUsername() throws Exception {
        AddTrainingRequest req = buildValidTrainingRequest(
                ctx.getAnotherTraineeUsername(),
                ctx.getTrainerUsername(),
                LocalDate.now().plusDays(2),
                60);

        performAddTraining(req, ctx.getTraineeToken());

        log.info("TEST: Training creation using another trainee username completed with status: {}",
                ctx.getResponseStatus());
    }

    @When("trainee deletes account")
    public void traineeDeletesAccount() throws Exception {
        long workloadBeforeDeletion = TestWorkloadServiceConfig.getWorkload(ctx.getTrainerUsername());

        MvcResult result = mockMvc.perform(
                        delete("/api/trainees/" + ctx.getTraineeUsername())
                                .header("Authorization", ctx.getTraineeToken()))
                .andReturn();

        ctx.setResponseStatus(result.getResponse().getStatus());

        log.info("TEST: Trainee deleted with status: {}, workload before deletion: {}, waiting for JMS...",
                result.getResponse().getStatus(), workloadBeforeDeletion);
    }

    //  THEN

    @Then("trainer workload should increase by {int} minutes")
    public void trainerWorkloadShouldIncreaseBy(int expectedMinutes) {
        final String trainerUsername = ctx.getTrainerUsername();

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    long actual = TestWorkloadServiceConfig.getWorkload(trainerUsername);
                    log.info("TEST: Checking workload for {}: {}, expecting: {}",
                            trainerUsername, actual, expectedMinutes);

                    assertThat(actual)
                            .as("Trainer %s workload should be %d minutes", trainerUsername, expectedMinutes)
                            .isEqualTo(expectedMinutes);
                });
    }

    @Then("trainer workload should be {int}")
    public void trainerWorkloadShouldBe(int expected) {
        final String trainerUsername = ctx.getTrainerUsername();

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    long actual = TestWorkloadServiceConfig.getWorkload(trainerUsername);
                    log.info("TEST: Checking workload for {}: {}, expected: {}",
                            trainerUsername, actual, expected);

                    assertThat(actual)
                            .as("Trainer %s workload should be %d", trainerUsername, expected)
                            .isEqualTo(expected);
                });
    }

    @Then("trainer workload should increase by {int}")
    public void trainerWorkloadShouldIncreaseBy2(int expectedIncrease) {
        trainerWorkloadShouldIncreaseBy(expectedIncrease);
    }

    @Then("request should fail with status {int}")
    public void requestShouldFailWithStatus(int expectedStatus) {
        assertThat(ctx.getResponseStatus())
                .as("Response status should be %d", expectedStatus)
                .isEqualTo(expectedStatus);

        log.info("TEST: Request failed as expected with status: {}", expectedStatus);
    }

    @Then("trainer workload should not change")
    public void trainerWorkloadShouldNotChange() {
        final String trainerUsername = ctx.getTrainerUsername();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(300))
                .untilAsserted(() -> {
                    long actual = TestWorkloadServiceConfig.getWorkload(trainerUsername);
                    log.info("TEST: Verifying workload unchanged for {}: {}", trainerUsername, actual);

                    assertThat(actual)
                            .as("Trainer %s workload should remain 0", trainerUsername)
                            .isEqualTo(0);
                });
    }

    //  HELPER METHODS

    private AddTrainingRequest buildValidTrainingRequest(
            String traineeUsername,
            String trainerUsername,
            LocalDate date,
            int duration) {
        return AddTrainingRequest.builder()
                .traineeUsername(traineeUsername)
                .trainerUsername(trainerUsername)
                .trainingName("Integration Training")
                .trainingDate(date)
                .trainingDuration(duration)
                .build();
    }

    private void performAddTraining(AddTrainingRequest req, String token) throws Exception {
        MockHttpServletRequestBuilder request = post("/api/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req));

        if (token != null && !token.isBlank()) {
            request.header("Authorization", token);
        }

        MvcResult result = mockMvc.perform(request).andReturn();
        ctx.setResponseStatus(result.getResponse().getStatus());
        ctx.setResponseBody(result.getResponse().getContentAsString());

        log.info("TEST: Training creation request completed with status: {}", ctx.getResponseStatus());
    }

    private long getTrainerTotalWorkload(String trainerUsername, String token) throws Exception {
        long directWorkload = TestWorkloadServiceConfig.getWorkload(trainerUsername);

        try {
            MvcResult result = mockMvc.perform(
                            get("/api/trainers/workload/" + trainerUsername)
                                    .header("Authorization", token))
                    .andReturn();

            int status = result.getResponse().getStatus();
            String json = result.getResponse().getContentAsString();

            if (status != 200 || json == null || json.isBlank()) {
                return directWorkload;
            }

            JsonNode node = objectMapper.readTree(json);
            return calculateTotalDuration(node);

        } catch (Exception e) {
            log.warn("TEST: Error calling workload API, using direct value: {}", directWorkload);
            return directWorkload;
        }
    }

    private long calculateTotalDuration(JsonNode node) {
        if (node == null || node.isNull()) {
            return 0;
        }

        // Direct fields check first
        if (node.has("totalDuration")) return node.get("totalDuration").asLong();
        if (node.has("totalMinutes")) return node.get("totalMinutes").asLong();

        // Years structure check
        if (node.has("years") && node.get("years").isArray()) {
            long total = 0;
            for (JsonNode year : node.get("years")) {
                if (year.has("months") && year.get("months").isArray()) {
                    for (JsonNode month : year.get("months")) {
                        if (month.has("trainingsSummaryDuration")) {
                            total += month.get("trainingsSummaryDuration").asLong();
                        } else if (month.has("totalDuration")) {
                            total += month.get("totalDuration").asLong();
                        }
                    }
                }
            }
            return total;
        }

        return 0;
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 5);
    }
}