@stg
Feature: Trainer Workload Service - STG Environment Tests
  As a Gym CRM System in STG environment
  I want to test workload tracking with mock services
  So that I can verify business logic without external dependencies

  Background:
    Given the mock workload storage is clean
    And the JMS queue is ready

  @smoke
  Scenario: Add single training session via JMS
    When a training session is sent to JMS queue:
      | trainerUsername  | john.trainer |
      | trainerFirstName | John         |
      | trainerLastName  | Trainer      |
      | isActive         | true         |
      | trainingDate     | 2025-06-15   |
      | trainingDuration | 60           |
      | actionType       | ADD          |
    And I wait 2 seconds for JMS processing
    Then the mock workload for "john.trainer" should be 60 minutes

  @smoke
  Scenario: Multiple training sessions accumulate workload
    When the following training sessions are sent to JMS:
      | trainerUsername | trainerFirstName | trainerLastName | isActive | trainingDate | trainingDuration | actionType |
      | jane.coach      | Jane             | Coach           | true     | 2025-06-10   | 60               | ADD        |
      | jane.coach      | Jane             | Coach           | true     | 2025-06-15   | 90               | ADD        |
      | jane.coach      | Jane             | Coach           | true     | 2025-06-20   | 45               | ADD        |
    And I wait 3 seconds for JMS processing
    Then the mock workload for "jane.coach" should be 195 minutes

  @smoke
  Scenario: Delete training session reduces workload
    Given trainer "mike.trainer" has mock workload of 120 minutes
    When a training session is sent to JMS queue:
      | trainerUsername  | mike.trainer |
      | trainerFirstName | Mike         |
      | trainerLastName  | Trainer      |
      | isActive         | true         |
      | trainingDate     | 2025-06-15   |
      | trainingDuration | 60           |
      | actionType       | DELETE       |
    And I wait 2 seconds for JMS processing
    Then the mock workload for "mike.trainer" should be 60 minutes

  @smoke
  Scenario: Workload cannot go below zero
    Given trainer "edge.trainer" has mock workload of 30 minutes
    When a training session is sent to JMS queue:
      | trainerUsername  | edge.trainer |
      | trainerFirstName | Edge         |
      | trainerLastName  | Trainer      |
      | isActive         | true         |
      | trainingDate     | 2025-06-15   |
      | trainingDuration | 100          |
      | actionType       | DELETE       |
    And I wait 2 seconds for JMS processing
    Then the mock workload for "edge.trainer" should be 0 minutes

  @regression
  Scenario: Concurrent training sessions
    When 10 concurrent training sessions are sent for "concurrent.trainer" with 30 minutes each
    And I wait 5 seconds for JMS processing
    Then the mock workload for "concurrent.trainer" should be 300 minutes

  @regression
  Scenario: Trainer information is stored correctly
    When a training session is sent to JMS queue:
      | trainerUsername  | info.trainer |
      | trainerFirstName | Info         |
      | trainerLastName  | Trainer      |
      | isActive         | true         |
      | trainingDate     | 2025-06-15   |
      | trainingDuration | 60           |
      | actionType       | ADD          |
    And I wait 2 seconds for JMS processing
    Then the trainer "info.trainer" should have the following information:
      | firstName | Info    |
      | lastName  | Trainer |
      | isActive  | true    |

  @regression
  Scenario: Multiple trainers workload tracking
    When the following training sessions are sent to JMS:
      | trainerUsername | trainerFirstName | trainerLastName | isActive | trainingDate | trainingDuration | actionType |
      | trainer1        | First            | Trainer         | true     | 2025-06-10   | 60               | ADD        |
      | trainer2        | Second           | Trainer         | true     | 2025-06-10   | 90               | ADD        |
      | trainer3        | Third            | Trainer         | true     | 2025-06-10   | 120              | ADD        |
    And I wait 3 seconds for JMS processing
    Then the system should track 3 different trainers
    And the mock workload for "trainer1" should be 60 minutes
    And the mock workload for "trainer2" should be 90 minutes
    And the mock workload for "trainer3" should be 120 minutes

  @edge-case
  Scenario: Very large training duration
    When a training session is sent to JMS queue:
      | trainerUsername  | large.trainer |
      | trainerFirstName | Large         |
      | trainerLastName  | Trainer       |
      | isActive         | true          |
      | trainingDate     | 2025-06-15    |
      | trainingDuration | 44640         |
      | actionType       | ADD           |
    And I wait 2 seconds for JMS processing
    Then the mock workload for "large.trainer" should be 44640 minutes

  @edge-case
  Scenario: Inactive trainer workload tracking
    When a training session is sent to JMS queue:
      | trainerUsername  | inactive.trainer |
      | trainerFirstName | Inactive         |
      | trainerLastName  | Trainer          |
      | isActive         | false            |
      | trainingDate     | 2025-06-15       |
      | trainingDuration | 60               |
      | actionType       | ADD              |
    And I wait 2 seconds for JMS processing
    Then the mock workload for "inactive.trainer" should be 60 minutes
    And the trainer "inactive.trainer" status should be inactive

  @performance
  Scenario: Process 100 messages quickly
    When 100 training sessions are sent rapidly for "perf.trainer" with 10 minutes each
    And I wait 10 seconds for JMS processing
    Then the mock workload for "perf.trainer" should be 1000 minutes
    And all messages should be processed successfully