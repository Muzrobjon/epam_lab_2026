Feature: Trainer Workload Management
  As a GYM trainer-workload microservice
  I want to process trainer workload data via JMS
  So that training durations can be tracked per trainer per month

  Background:
    Given the trainer workload service is running

  Scenario: Successfully add workload for a trainer (ADD action)
    When a workload message is sent for trainer "trainer.one" firstName "Trainer" lastName "One" date "2024-06-15" duration 60 action "ADD"
    Then when I get workload for trainer "trainer.one" year 2024 month 6 the duration should be 60

  Scenario: Successfully get trainer workload after ADD action
    Given workload is processed for trainer "trainer.two" date "2024-06-15" duration 120 action "ADD"
    When I get workload for trainer "trainer.two" year 2024 month 6
    Then the workload response status should be 200
    And the training summary duration should be 120

  Scenario: Successfully delete duration with DELETE action
    Given workload is processed for trainer "trainer.three" date "2024-06-15" duration 120 action "ADD"
    When a workload message is sent for trainer "trainer.three" firstName "Trainer" lastName "Three" date "2024-06-15" duration 60 action "DELETE"
    Then when I get workload for trainer "trainer.three" year 2024 month 6 the duration should be 60

  Scenario: Successfully accumulate workload across multiple sessions
    Given workload is processed for trainer "trainer.four" date "2024-07-10" duration 60 action "ADD"
    And workload is processed for trainer "trainer.four" date "2024-07-20" duration 90 action "ADD"
    When I get workload for trainer "trainer.four" year 2024 month 7
    Then the workload response status should be 200
    And the training summary duration should be 150

  Scenario: Duration does not go below zero when deleting more than available
    Given workload is processed for trainer "trainer.six" date "2024-09-01" duration 30 action "ADD"
    When a workload message is sent for trainer "trainer.six" firstName "Trainer" lastName "Six" date "2024-09-01" duration 120 action "DELETE"
    Then when I get workload for trainer "trainer.six" year 2024 month 9 the duration should be 0

  Scenario: Returns empty for non-existent trainer
    When I get workload for trainer "nobody.here" year 2024 month 6
    Then the workload response status should be 200
    And the training summary duration should be 0

  Scenario: Returns empty for non-existent year
    Given workload is processed for trainer "trainer.eight" date "2024-06-01" duration 60 action "ADD"
    When I get workload for trainer "trainer.eight" year 2023 month 6
    Then the workload response status should be 200
    And the training summary duration should be 0