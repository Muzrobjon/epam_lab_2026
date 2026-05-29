Feature: Trainer Workload Service Integration
  As a GYM platform
  I want the Workload service to process messages from ActiveMQ
  So that trainer durations are tracked correctly in MongoDB

  Background:
    Given the trainer workload service is running

  Scenario: ActiveMQ ADD message is processed and saved to MongoDB
    When a workload message is sent to the queue for trainer "mq.trainer.one" action "ADD" date "2024-06-15" duration 60
    Then the trainer workload collection should contain trainer "mq.trainer.one"
    And the training duration for trainer "mq.trainer.one" in year 2024 month 6 should be 60

  Scenario: ActiveMQ DELETE message reduces duration in MongoDB
    Given workload is processed for trainer "mq.trainer.two" action "ADD" date "2024-07-10" duration 120
    When a workload message is sent to the queue for trainer "mq.trainer.two" action "DELETE" date "2024-07-10" duration 60
    Then the training duration for trainer "mq.trainer.two" in year 2024 month 7 should be 60

  Scenario: Multiple ADD messages accumulate duration
    Given workload is processed for trainer "mq.trainer.three" action "ADD" date "2024-08-01" duration 60
    And workload is processed for trainer "mq.trainer.three" action "ADD" date "2024-08-15" duration 90
    Then the training duration for trainer "mq.trainer.three" in year 2024 month 8 should be 150

  Scenario: Duration tracked independently per month
    Given workload is processed for trainer "mq.trainer.four" action "ADD" date "2024-09-01" duration 60
    And workload is processed for trainer "mq.trainer.four" action "ADD" date "2024-10-01" duration 90
    Then the training duration for trainer "mq.trainer.four" in year 2024 month 9 should be 60
    And the training duration for trainer "mq.trainer.four" in year 2024 month 10 should be 90

  Scenario: Invalid message with blank username goes to DLQ and is not saved
    When an invalid workload message with blank username is sent to the queue
    Then the trainer workload collection should not contain trainer ""

  Scenario: Invalid message with zero duration goes to DLQ and is not saved
    When an invalid workload message with zero duration is sent for trainer "mq.trainer.bad"
    Then the trainer workload collection should not contain trainer "mq.trainer.bad"