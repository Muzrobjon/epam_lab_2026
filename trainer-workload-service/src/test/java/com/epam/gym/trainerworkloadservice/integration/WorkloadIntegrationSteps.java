package com.epam.gym.trainerworkloadservice.integration;

import com.epam.gym.trainerworkloadservice.component.WorkloadTestContext;
import com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest;
import com.epam.gym.trainerworkloadservice.entity.TrainerWorkload;
import com.epam.gym.trainerworkloadservice.enums.ActionType;
import io.cucumber.java.en.*;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkloadIntegrationSteps {

    private final WorkloadTestContext context;
    private final MongoTemplate mongoTemplate;
    private final JmsTemplate jmsTemplate;

    @Value("${app.jms.queue.trainer-workload}")
    private String workloadQueue;

    public WorkloadIntegrationSteps(WorkloadTestContext context,
                                    MongoTemplate mongoTemplate,
                                    JmsTemplate jmsTemplate) {
        this.context = context;
        this.mongoTemplate = mongoTemplate;
        this.jmsTemplate = jmsTemplate;
    }

    @Given("workload is processed for trainer {string} action {string} date {string} duration {int}")
    public void workloadIsProcessedForTrainer(String username, String action, String date, int duration) {
        sendWorkloadMessage(username, action, date, duration, true);
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    TrainerWorkload trainer = mongoTemplate.findOne(
                            Query.query(Criteria.where("trainerUsername").is(username)),
                            TrainerWorkload.class);
                    assertThat(trainer).isNotNull();
                });
    }

    @When("a workload message is sent to the queue for trainer {string} action {string} date {string} duration {int}")
    public void aWorkloadMessageIsSentToQueue(String username, String action, String date, int duration) {
        sendWorkloadMessage(username, action, date, duration, true);
    }

    @When("an invalid workload message with blank username is sent to the queue")
    public void anInvalidWorkloadMessageWithBlankUsernameIsSent() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("")
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(LocalDate.parse("2024-06-01"))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();
        jmsTemplate.convertAndSend(workloadQueue, request);
    }

    @When("an invalid workload message with zero duration is sent for trainer {string}")
    public void anInvalidWorkloadMessageWithZeroDurationIsSent(String username) {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(LocalDate.parse("2024-06-01"))
                .trainingDuration(0)
                .actionType(ActionType.ADD)
                .build();
        jmsTemplate.convertAndSend(workloadQueue, request);
    }

    @Then("the trainer workload collection should contain trainer {string}")
    public void theCollectionShouldContainTrainer(String username) {
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    TrainerWorkload trainer = mongoTemplate.findOne(
                            Query.query(Criteria.where("trainerUsername").is(username)),
                            TrainerWorkload.class);
                    assertThat(trainer).isNotNull();
                });
    }

    @Then("the trainer workload collection should not contain trainer {string}")
    public void theCollectionShouldNotContainTrainer(String username) {
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        TrainerWorkload trainer = mongoTemplate.findOne(
                Query.query(Criteria.where("trainerUsername").is(username)),
                TrainerWorkload.class);
        assertThat(trainer).isNull();
    }

    @Then("the training duration for trainer {string} in year {int} month {int} should be {long}")
    public void theTrainingDurationShouldBe(String username, int year, int month, long expected) {
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    TrainerWorkload trainer = mongoTemplate.findOne(
                            Query.query(Criteria.where("trainerUsername").is(username)),
                            TrainerWorkload.class);
                    assertThat(trainer).isNotNull();

                    long total = trainer.getYears().stream()
                            .filter(y -> y.getYear().equals(year))
                            .flatMap(y -> y.getMonths().stream())
                            .filter(m -> m.getMonth().equals(month))
                            .mapToLong(TrainerWorkload.MonthSummary::getTrainingSummaryDuration)
                            .sum();

                    assertThat(total).isEqualTo(expected);
                });
    }

    private void sendWorkloadMessage(String username, String action, String date, int duration, boolean isActive) {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(isActive)
                .trainingDate(LocalDate.parse(date))
                .trainingDuration(duration)
                .actionType(ActionType.valueOf(action))
                .build();

        System.out.println("===> Sending to queue: " + workloadQueue);
        System.out.println("===> JmsTemplate: " + jmsTemplate);
        System.out.println("===> ConnectionFactory: " + jmsTemplate.getConnectionFactory());

        jmsTemplate.convertAndSend(workloadQueue, request, msg -> {
            msg.setStringProperty("X-Transaction-Id", "test-" + java.util.UUID.randomUUID());
            return msg;
        });

        System.out.println("===> Message sent!");
    }
}