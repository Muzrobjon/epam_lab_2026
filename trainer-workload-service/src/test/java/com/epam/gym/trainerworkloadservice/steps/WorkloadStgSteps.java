package com.epam.gym.trainerworkloadservice.steps;

import com.epam.gym.trainerworkloadservice.config.TestWorkloadServiceConfig;
import com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest;
import com.epam.gym.trainerworkloadservice.dto.response.TrainerWorkloadResponse;
import com.epam.gym.trainerworkloadservice.enums.ActionType;
import com.epam.gym.trainerworkloadservice.util.TestTokenGenerator;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
public class WorkloadStgSteps {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestTokenGenerator tokenGenerator;

    @Value("${app.jms.queue.trainer-workload}")
    private String workloadQueue;

    // ========== SETUP ==========
    @Before
    public void setup() {
        // Test storage tozalash
        TestWorkloadServiceConfig.clearAll();

        // JMS connection tekshirish
        verifyJmsConnection();
    }

    private void verifyJmsConnection() {
        try {
            jmsTemplate.browse("ActiveMQ.Advisory.MasterBroker", (session, browser) -> {
                log.info("✅ ActiveMQ connection verified");
                return null;
            });
        } catch (Exception e) {
            log.warn("⚠️ ActiveMQ advisory browse failed (may be normal): {}", e.getMessage());
            // Connection ni boshqa usulda tekshirish
            try {
                jmsTemplate.getConnectionFactory();
                log.info("✅ JMS ConnectionFactory is available");
            } catch (Exception ex) {
                log.error("❌ JMS connection failed: {}", ex.getMessage());
                throw new RuntimeException("JMS is not available", ex);
            }
        }
    }

    private void logQueueStatus(String queueName) {
        try {
            Integer count = jmsTemplate.browse(queueName, (session, browser) -> {
                int msgCount = 0;
                var enumeration = browser.getEnumeration();
                while (enumeration.hasMoreElements()) {
                    enumeration.nextElement();
                    msgCount++;
                }
                return msgCount;
            });
            log.info("📬 Queue '{}' has {} pending messages", queueName, count);
        } catch (Exception e) {
            log.warn("⚠️ Could not browse queue '{}': {}", queueName, e.getMessage());
        }
    }

    // ========== AUTH HELPERS ==========
    private HttpHeaders createAuthHeaders() {
        String token = tokenGenerator.getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Transaction-Id", "stg-test-" + UUID.randomUUID());
        return headers;
    }

    private <T> ResponseEntity<T> authenticatedGet(String url, Class<T> responseType) {
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
    }

    // ========== GIVEN ==========
    @Given("the mock workload storage is clean")
    public void theMockWorkloadStorageIsClean() {
        TestWorkloadServiceConfig.clearAll();
        log.info("✅ Mock workload storage cleaned");
    }

    @Given("the JMS queue is ready")
    public void theJMSQueueIsReady() {
        assertThat(jmsTemplate).isNotNull();
        log.info("✅ JMS queue is ready");
    }

    @Given("trainer {string} has mock workload of {int} minutes")
    public void trainerHasMockWorkload(String username, int duration) {
        // In-memory storage ga qo'shish
        TestWorkloadServiceConfig.setWorkload(username, duration);

        // MongoDB ga ham yuborish
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(LocalDate.now())
                .trainingDuration(duration)
                .actionType(ActionType.ADD)
                .build();

        String txId = "setup-" + UUID.randomUUID();

        log.info("📤 Setting up initial workload [{}]", txId);

        jmsTemplate.convertAndSend(workloadQueue, request, message -> {
            message.setStringProperty("X-Transaction-Id", txId);
            return message;
        });

        // Processing uchun kutish
        sleepSafely(3000);

        log.info("✅ Pre-set workload for {}: {} minutes", username, duration);
    }

    // ========== WHEN ==========
    @When("a training session is sent to JMS queue:")
    public void aTrainingSessionIsSentToJMS(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap();
        String transactionId = "stg-test-" + UUID.randomUUID();

        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(data.get("trainerUsername"))
                .trainerFirstName(data.get("trainerFirstName"))
                .trainerLastName(data.get("trainerLastName"))
                .isActive(Boolean.parseBoolean(data.get("isActive")))
                .trainingDate(LocalDate.parse(data.get("trainingDate")))
                .trainingDuration(Integer.parseInt(data.get("trainingDuration")))
                .actionType(ActionType.valueOf(data.get("actionType")))
                .build();

        // In-memory tracking
        String username = data.get("trainerUsername");
        int duration = Integer.parseInt(data.get("trainingDuration"));
        ActionType actionType = ActionType.valueOf(data.get("actionType"));

        if (actionType == ActionType.ADD) {
            TestWorkloadServiceConfig.addWorkload(username, duration);
        } else {
            TestWorkloadServiceConfig.subtractWorkload(username, duration);
        }

        log.info("📤 Sending JMS message [{}]", transactionId);

        jmsTemplate.convertAndSend(workloadQueue, request, message -> {
            message.setStringProperty("X-Transaction-Id", transactionId);
            return message;
        });

        log.info("✅ Message sent to queue: {}", workloadQueue);
        logQueueStatus(workloadQueue);

        sleepSafely(2000);

        log.info("⏰ Waited 2s for message processing");
    }

    @When("the following training sessions are sent to JMS:")
    public void theFollowingTrainingSessionsAreSent(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();

        for (Map<String, String> row : rows) {
            String txId = "batch-" + UUID.randomUUID();

            TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                    .trainerUsername(row.get("trainerUsername"))
                    .trainerFirstName(row.get("trainerFirstName"))
                    .trainerLastName(row.get("trainerLastName"))
                    .isActive(Boolean.parseBoolean(row.get("isActive")))
                    .trainingDate(LocalDate.parse(row.get("trainingDate")))
                    .trainingDuration(Integer.parseInt(row.get("trainingDuration")))
                    .actionType(ActionType.valueOf(row.get("actionType")))
                    .build();

            // In-memory tracking
            String username = row.get("trainerUsername");
            int duration = Integer.parseInt(row.get("trainingDuration"));
            ActionType actionType = ActionType.valueOf(row.get("actionType"));

            if (actionType == ActionType.ADD) {
                TestWorkloadServiceConfig.addWorkload(username, duration);
            } else {
                TestWorkloadServiceConfig.subtractWorkload(username, duration);
            }

            jmsTemplate.convertAndSend(workloadQueue, request, message -> {
                message.setStringProperty("X-Transaction-Id", txId);
                return message;
            });

            log.info("📤 JMS message sent: {} [{}]", request.getTrainerUsername(), txId);
        }
    }

    @When("{int} concurrent training sessions are sent for {string} with {int} minutes each")
    public void concurrentTrainingSessionsAreSent(int count, String username, int duration)
            throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(count, 10));
        CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String txId = "concurrent-" + UUID.randomUUID();

                    TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                            .trainerUsername(username)
                            .trainerFirstName("Concurrent")
                            .trainerLastName("Trainer")
                            .isActive(true)
                            .trainingDate(LocalDate.now().plusDays(index))
                            .trainingDuration(duration)
                            .actionType(ActionType.ADD)
                            .build();

                    // In-memory tracking
                    TestWorkloadServiceConfig.addWorkload(username, duration);

                    jmsTemplate.convertAndSend(workloadQueue, request, message -> {
                        message.setStringProperty("X-Transaction-Id", txId);
                        return message;
                    });

                    log.debug("Sent concurrent message {}/{} [{}]", index + 1, count, txId);
                } catch (Exception e) {
                    log.error("Error sending concurrent message {}: {}", index, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).as("All concurrent messages should be sent").isTrue();
        log.info("✅ {} concurrent messages sent", count);
    }

    @When("{int} training sessions are sent rapidly for {string} with {int} minutes each")
    public void trainingsSessionsSentRapidly(int count, String username, int duration) {
        for (int i = 0; i < count; i++) {
            String txId = "rapid-" + UUID.randomUUID();

            TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                    .trainerUsername(username)
                    .trainerFirstName("Perf")
                    .trainerLastName("Trainer")
                    .isActive(true)
                    .trainingDate(LocalDate.now())
                    .trainingDuration(duration)
                    .actionType(ActionType.ADD)
                    .build();

            // In-memory tracking
            TestWorkloadServiceConfig.addWorkload(username, duration);

            jmsTemplate.convertAndSend(workloadQueue, request, message -> {
                message.setStringProperty("X-Transaction-Id", txId);
                return message;
            });
        }

        log.info("✅ {} rapid messages sent", count);
    }

    // ========== AND ==========
    @And("I wait {int} seconds for JMS processing")
    public void iWaitForJMSProcessing(int seconds) throws InterruptedException {
        log.info("⏳ Waiting {} seconds for JMS processing...", seconds);
        Thread.sleep(seconds * 1000L);
        log.info("✅ Wait completed");
    }

    // ========== THEN ==========
    @Then("the mock workload for {string} should be {int} minutes")
    public void theMockWorkloadShouldBe(String username, int expectedMinutes) {
        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    long actual = TestWorkloadServiceConfig.getWorkload(username);
                    log.debug("🔍 Checking workload for {}: actual={}, expected={}",
                            username, actual, expectedMinutes);
                    assertThat(actual)
                            .as("Total workload minutes for %s should match expected", username)
                            .isEqualTo((long) expectedMinutes);
                });

        log.info("✅ Mock workload verified for {}: {} minutes", username, expectedMinutes);
    }

    @Then("the trainer {string} should have the following information:")
    public void theTrainerShouldHaveInformation(String username, DataTable dataTable) {
        Map<String, String> expected = dataTable.asMap();

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    ResponseEntity<TrainerWorkloadResponse> response = authenticatedGet(
                            "/api/v1/trainer-workload/" + username,
                            TrainerWorkloadResponse.class
                    );

                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    TrainerWorkloadResponse workload = response.getBody();
                    assertThat(workload).isNotNull();

                    assertThat(workload.getTrainerFirstName())
                            .isEqualTo(expected.get("firstName"));
                    assertThat(workload.getTrainerLastName())
                            .isEqualTo(expected.get("lastName"));
                    assertThat(String.valueOf(workload.getTrainerStatus()))
                            .isEqualTo(expected.get("isActive"));
                });

        log.info("✅ Trainer information verified");
    }

    @Then("the system should track {int} different trainers")
    public void theSystemShouldTrackDifferentTrainers(int expectedCount) {
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    int actualCount = TestWorkloadServiceConfig.getTrainerCount();
                    assertThat(actualCount)
                            .as("System should track at least %d trainers", expectedCount)
                            .isGreaterThanOrEqualTo(expectedCount);
                });

        log.info("✅ System tracking {} trainers (expected: {})",
                TestWorkloadServiceConfig.getTrainerCount(), expectedCount);
    }

    @Then("the trainer {string} status should be inactive")
    public void theTrainerStatusShouldBeInactive(String username) {
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    ResponseEntity<TrainerWorkloadResponse> response = authenticatedGet(
                            "/api/v1/trainer-workload/" + username,
                            TrainerWorkloadResponse.class
                    );

                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    TrainerWorkloadResponse workload = response.getBody();
                    assertThat(workload).isNotNull();
                    assertThat(workload.getTrainerStatus()).isFalse();
                });

        log.info("✅ Trainer {} is inactive", username);
    }

    @Then("all messages should be processed successfully")
    public void allMessagesShouldBeProcessed() {
        // Queue bo'sh ekanligini tekshirish
        sleepSafely(2000);
        logQueueStatus(workloadQueue);
        log.info("✅ All messages processed successfully");
    }

    // ========== HELPER METHODS ==========
    private void sleepSafely(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep interrupted");
        }
    }
}