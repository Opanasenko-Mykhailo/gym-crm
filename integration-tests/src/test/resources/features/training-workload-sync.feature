Feature: Training and Workload Synchronization
  As a system
  I want to ensure that trainings created in GCA are synchronized with Workload service
  So that trainer workload is accurately tracked

  Background:
    Given GCA and workload services are running for integration tests

  @PositiveCase
  Scenario: Creating training in GCA updates workload in Workload service
    Given integration test trainer "Alex.Smith" exists with trainer data:
      | firstName      | Alex     |
      | lastName       | Smith    |
      | specialization | STRENGTH |
    And integration test trainee "Emma.Jones" exists with trainee data:
      | firstName   | Emma       |
      | lastName    | Jones      |
      | dateOfBirth | 1995-05-15 |
      | address     | 123 Oak St |
    When I create a training in GCA:
      | traineeUsername  | Emma.Jones       |
      | trainerUsername  | Alex.Smith       |
      | trainingName     | Morning Strength |
      | trainingDate     | 2025-10-15       |
      | trainingDuration | 60               |
      | trainingTypeName | STRENGTH         |
    Then training creation is successful
    Then integration test trainer "Alex.Smith" has total duration of 60 minutes for October 2025

  @PositiveCase
  Scenario: Multiple trainings accumulate in workload
    Given integration test trainer "Mike.Brown" exists with trainer data:
      | firstName      | Mike   |
      | lastName       | Brown  |
      | specialization | CARDIO |
    And integration test trainee "Sarah.Davis" exists with trainee data:
      | firstName   | Sarah      |
      | lastName    | Davis      |
      | dateOfBirth | 1992-11-20 |
      | address     | 456 Elm St |
    When I create a training in GCA:
      | traineeUsername  | Sarah.Davis    |
      | trainerUsername  | Mike.Brown     |
      | trainingName     | Morning Cardio |
      | trainingDate     | 2025-10-10     |
      | trainingDuration | 45             |
      | trainingTypeName | CARDIO         |
    And I create a training in GCA:
      | traineeUsername  | Sarah.Davis    |
      | trainerUsername  | Mike.Brown     |
      | trainingName     | Evening Cardio |
      | trainingDate     | 2025-10-15     |
      | trainingDuration | 60             |
      | trainingTypeName | CARDIO         |
    And I create a training in GCA:
      | traineeUsername  | Sarah.Davis      |
      | trainerUsername  | Mike.Brown       |
      | trainingName     | Afternoon Cardio |
      | trainingDate     | 2025-10-20       |
      | trainingDuration | 30               |
      | trainingTypeName | CARDIO           |
    Then training creation is successful
    Then integration test trainer "Mike.Brown" has total duration of 135 minutes for October 2025

  @PositiveCase
  Scenario: Workload summary includes all trainer information
    Given integration test trainer "Tom.Wilson" exists with trainer data:
      | firstName      | Tom    |
      | lastName       | Wilson |
      | specialization | YOGA   |
    And integration test trainee "Lisa.Taylor" exists with trainee data:
      | firstName   | Lisa         |
      | lastName    | Taylor       |
      | dateOfBirth | 1990-07-12   |
      | address     | 789 Pine Ave |
    When I create a training in GCA:
      | traineeUsername  | Lisa.Taylor       |
      | trainerUsername  | Tom.Wilson        |
      | trainingName     | Swimming Practice |
      | trainingDate     | 2025-10-15        |
      | trainingDuration | 90                |
      | trainingTypeName | YOGA              |
    And I request workload summary for trainer "Tom.Wilson"
    Then I receive trainer workload summary with yearly and monthly breakdown
    And workload summary contains trainer information:
      | username  | Tom.Wilson |
      | firstName | Tom        |
      | lastName  | Wilson     |
      | active    | true       |