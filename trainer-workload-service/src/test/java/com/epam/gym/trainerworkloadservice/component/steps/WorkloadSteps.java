package com.epam.gym.trainerworkloadservice.component.steps;

import com.epam.gym.trainerworkloadservice.component.WorkloadTestContext;
import com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest;
import com.epam.gym.trainerworkloadservice.entity.TrainerWorkload;
import com.epam.gym.trainerworkloadservice.enums.ActionType;
import com.epam.gym.trainerworkloadservice.service.TrainerWorkloadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jms.core.JmsTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
@Slf4j
public class WorkloadSteps {

    private final WorkloadTestContext context;
    private final JmsTemplate jmsTemplate;
    private final MongoTemplate mongoTemplate;
    private final TrainerWorkloadService workloadService;

    @Value("${app.jms.queue.trainer-workload}")
    private String workloadQueue;



    @Before
    public void cleanDatabase() {
        log.info("=== CLEANING MONGODB BEFORE SCENARIO ===");
        mongoTemplate.dropCollection(TrainerWorkload.class);
        log.info("MongoDB collection dropped. Count: {}", mongoTemplate.count(new Query(), TrainerWorkload.class));
    }

    @Given("the trainer workload service is running")
    public void theTrainerWorkloadServiceIsRunning() {
        assertThat(context.getServerPort()).isGreaterThan(0);
        log.info("Service running on port: {}", context.getServerPort());
    }

    @Given("workload is processed for trainer {string} date {string} duration {int} action {string}")
    public void workloadIsProcessedForTrainer(String username, String date, int duration, String action) {
        TrainerWorkloadRequest request = buildRequest(username, "First", "Trainer", true, date, duration, action);

        try {
            sendJmsMessage(request);
            waitForTrainerInDb(username);
        } catch (Exception e) {
            log.warn("JMS failed, using direct service call: {}", e.getMessage());
            workloadService.processWorkload(request, "direct-" + System.currentTimeMillis());
        }

        context.setCurrentTrainerUsername(username);
    }

    @When("a workload message is sent for trainer {string} firstName {string} lastName {string} date {string} duration {int} action {string}")
    public void aWorkloadMessageIsSent(String username, String firstName, String lastName,
                                       String date, int duration, String action) {
        TrainerWorkloadRequest request = buildRequest(username, firstName, lastName, true, date, duration, action);
        sendJmsMessage(request);
        context.setCurrentTrainerUsername(username);
    }

    @When("a workload message is sent for inactive trainer {string} date {string} duration {int}")
    public void aWorkloadMessageIsSentForInactiveTrainer(String username, String date, int duration) {
        TrainerWorkloadRequest request = buildRequest(username, "Inactive", "Trainer", false, date, duration, "ADD");
        sendJmsMessage(request);
        context.setCurrentTrainerUsername(username);
    }

    @When("I get workload for trainer {string} year {int} month {int}")
    public void iGetWorkload(String username, int year, int month) {
        executeGetRequest("/api/v1/trainer-workload/" + username + "?year=" + year + "&month=" + month);
    }

    @When("I get workload for trainer {string} without filters")
    public void iGetWorkloadWithoutFilters(String username) {
        executeGetRequest("/api/v1/trainer-workload/" + username);
    }

    @When("I get workload for trainer {string} year {int} month {int} without authentication")
    public void iGetWorkloadWithoutAuth(String username, int year, int month) {
        executeGetRequestWithoutAuth("/api/v1/trainer-workload/" + username + "?year=" + year + "&month=" + month);
    }

    @Then("the workload response status should be {int}")
    public void theWorkloadResponseStatusShouldBe(int expected) {
        assertThat(context.getLastHttpResponse().statusCode())
                .as("Expected HTTP %d but got %d. Body: %s",
                        expected, context.getLastHttpResponse().statusCode(),
                        context.getLastHttpResponse().body())
                .isEqualTo(expected);
    }

    @Then("the training summary duration should be {long}")
    public void theTrainingSummaryDurationShouldBe(long expected) throws Exception {
        long total = extractTotalDuration(context.getLastHttpResponse().body());
        assertThat(total).isEqualTo(expected);
    }

    @Then("the response should contain trainer username {string}")
    public void theResponseShouldContainTrainerUsername(String username) throws Exception {
        String body = context.getLastHttpResponse().body();
        var root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
        assertThat(root.get("trainerUsername").asText()).isEqualTo(username);
    }

    @Then("when I get workload for trainer {string} year {int} month {int} the duration should be {long}")
    public void whenIGetWorkloadTheDurationShouldBe(String username, int year, int month, long expected) throws Exception {
        executeGetRequest("/api/v1/trainer-workload/" + username + "?year=" + year + "&month=" + month);
        assertThat(context.getLastHttpResponse().statusCode()).isEqualTo(200);
        long total = extractTotalDuration(context.getLastHttpResponse().body());
        assertThat(total).isEqualTo(expected);
    }

    private void executeGetRequest(String path) {
        try {
            int port = context.getServerPort();
            String url = "http://localhost:" + port + path;
            log.info("GET request to: {}", url);

            String token = context.getOrFetchToken();
            log.debug("Using auth token: {}...", token.substring(0, Math.min(20, token.length())));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            context.setLastHttpResponse(response);
            log.info("Response status: {}", response.statusCode());
        } catch (Exception e) {
            throw new RuntimeException("GET request failed: " + e.getMessage(), e);
        }
    }

    private void executeGetRequestWithoutAuth(String path) {
        try {
            int port = context.getServerPort();
            String url = "http://localhost:" + port + path;
            log.info("GET request (no auth) to: {}", url);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            context.setLastHttpResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("GET request failed: " + e.getMessage(), e);
        }
    }

    private long extractTotalDuration(String body) throws Exception {
        var root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
        long total = 0L;
        if (root.has("years") && root.get("years").isArray()) {
            for (var year : root.get("years")) {
                if (year.has("months") && year.get("months").isArray()) {
                    for (var month : year.get("months")) {
                        total += month.get("trainingSummaryDuration").asLong();
                    }
                }
            }
        }
        return total;
    }

    private void sendJmsMessage(TrainerWorkloadRequest request) {
        log.info("Sending JMS message to queue '{}': trainer={}, duration={}, action={}",
                workloadQueue, request.getTrainerUsername(), request.getTrainingDuration(), request.getActionType());

        jmsTemplate.convertAndSend(workloadQueue, request, msg -> {
            msg.setStringProperty("X-Transaction-Id", "test-" + java.util.UUID.randomUUID());
            return msg;
        });

        log.info("JMS message sent successfully");
    }

    private void waitForTrainerInDb(String username) {
        log.info("Waiting for trainer '{}' in MongoDB...", username);
        Awaitility.await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    long count = mongoTemplate.count(
                            Query.query(Criteria.where("trainerUsername").is(username)),
                            TrainerWorkload.class);
                    assertThat(count)
                            .as("Trainer '%s' should exist in MongoDB (count > 0)", username)
                            .isGreaterThan(0);
                });
        log.info("Trainer '{}' found in MongoDB", username);
    }

    private TrainerWorkloadRequest buildRequest(String username, String firstName, String lastName,
                                                boolean isActive, String date, int duration, String action) {
        return TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName(firstName)
                .trainerLastName(lastName)
                .isActive(isActive)
                .trainingDate(LocalDate.parse(date))
                .trainingDuration(duration)
                .actionType(ActionType.valueOf(action))
                .build();
    }
}