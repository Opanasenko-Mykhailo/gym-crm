Feature: Trainer Workload Management
  As a system user
  I want to manage trainer workload
  So that I can track training hours

  Background:
    Given the workload service is running

  @PositiveCase
  Scenario: Add new training session for trainer
    Given trainer "ethan.woodward" does not exist
    When I submit a workload request:
      | username       | firstName | lastName | active | trainingDate | durationInMinutes | actionType |
      | ethan.woodward | Ethan     | Woodward | true   | 2025-10-15   | 60                | ADD        |
    Then the request is successful
    And trainer "ethan.woodward" has total duration of 60 minutes for October 2025

  @PositiveCase
  Scenario: Add multiple training sessions
    Given trainer "lila.harrington" exists with following workload:
      | year | month | duration |
      | 2025 | 10    | 120      |
    When I add a training session:
      | username        | firstName | lastName   | trainingDate | durationInMinutes | actionType |
      | lila.harrington | Lila      | Harrington | 2025-10-20   | 90                | ADD        |
    Then trainer "lila.harrington" has total duration of 210 minutes for October 2025

  @PositiveCase
  Scenario: Delete training session
    Given trainer "graham.ellison" exists with following workload:
      | year | month | duration |
      | 2025 | 10    | 180      |
    When I delete a training session:
      | username       | firstName | lastName | trainingDate | durationInMinutes | actionType |
      | graham.ellison | Graham    | Ellison  | 2025-10-10   | 60                | DELETE     |
    Then trainer "graham.ellison" has total duration of 120 minutes for October 2025

  @PositiveCase
  Scenario: Retrieve trainer summary
    Given trainer "nora.fitzgerald" exists with workload across multiple months
    When I request summary for trainer "nora.fitzgerald"
    Then I receive complete trainer summary with yearly and monthly breakdown

  @NegativeCase
  Scenario: Trainer not found
    Given trainer "test.invalid" does not exist
    When I request summary for trainer "test.invalid"
    Then I receive a 404 not found error

  @NegativeCase
  Scenario: Add training session with invalid negative duration
    Given trainer "nora.fitzgerald" does not exist
    When I submit a workload request:
      | username        | firstName | lastName   | active | trainingDate | durationInMinutes | actionType |
      | nora.fitzgerald | Nora      | Fitzgerald | true   | 2025-10-15   | -60               | ADD        |
    Then the request is unsuccessful with status 400