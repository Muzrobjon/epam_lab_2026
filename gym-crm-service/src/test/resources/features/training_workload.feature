@training @positive
Feature: Training workload integration

  @positive @workload
  Scenario: Training creation updates trainer workload
    Given a trainer exists
    And a trainee exists
    And trainee is authenticated
    When trainee creates training with trainer
    Then trainer workload should increase by 60 minutes

  @positive @workload
  Scenario: Multiple trainings increase trainer workload
    Given a trainer exists
    And a trainee exists
    And trainee is authenticated
    When trainee creates training with trainer
    When trainee creates another training at different time with the same trainer
    Then trainer workload should be 120

  @positive @workload
  Scenario: Deleting trainee decreases workload
    Given a trainer exists
    And a trainee exists
    And trainee is authenticated
    When trainee creates training with trainer
    And trainee deletes account
    Then trainer workload should be 0

  @negative @workload
  Scenario: Creating training with non-existing trainer
    Given a trainee exists
    And trainee is authenticated
    When trainee creates training with non existing trainer
    Then request should fail with status 404

  @negative @workload
  Scenario: Training date cannot be in the past
    Given a trainer exists
    And a trainee exists
    And trainee is authenticated
    When trainee creates training with date in the past
    Then request should fail with status 400

  @negative @nfr @auth @workload
  Scenario: Unauthenticated trainee cannot create training
    Given a trainer exists
    And a trainee exists
    When trainee creates training without authentication
    Then request should fail with status 401

  @negative @workload
  Scenario: Training with zero duration is rejected
    Given a trainer exists
    And a trainee exists
    And trainee is authenticated
    When trainee creates training with zero duration
    Then request should fail with status 400

  @negative @workload @auth
  Scenario: Trainee cannot create training for another trainee's session
    Given a trainer exists
    And a trainee exists
    And another trainee exists
    And trainee is authenticated
    When trainee creates training using another trainee username
    Then request should fail with status 401

  @negative @workload
  Scenario: Trainee cannot create training when trainer has overlapping schedule
    Given a trainer exists
    And a trainee exists
    And trainee is authenticated
    When trainee creates training with trainer
    When trainee creates another training at the same time with the same trainer
    Then request should fail with status 409

  @integration @workload
  Scenario: Trainer adds new training session and workload service is updated
    Given a trainer exists
    And a trainee exists
    And trainee is authenticated
    When trainee creates training with trainer
    Then workload service should have updated trainee hours