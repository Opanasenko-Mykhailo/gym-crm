Feature: Workload Service
  WorkloadService should update trainer monthly summaries correctly
  and handle invalid input properly.

  @PositiveCase
  Scenario: Add workload for existing trainer
    Given a trainer exists with username "john.doe"
    When I add a workload of 120 minutes for "john.doe" in month 9 of 2030
    Then the trainer summary for "john.doe" should contain 120 minutes for month 9 of 2030

  @PositiveCase
  Scenario: Add multiple workloads for the same trainer
    Given a trainer exists with username "anna.smith"
    When I add a workload of 60 minutes for "anna.smith" in month 9 of 2030
    And I add a workload of 90 minutes for "anna.smith" in month 9 of 2030
    Then the trainer summary for "anna.smith" should contain 150 minutes for month 9 of 2030

  @PositiveCase
  Scenario: Add workloads for different months
    Given a trainer exists with username "mark.jones"
    When I add a workload of 45 minutes for "mark.jones" in month 8 of 2030
    And I add a workload of 30 minutes for "mark.jones" in month 9 of 2030
    Then the trainer summary for "mark.jones" should contain 45 minutes for month 8 of 2030
    And the trainer summary for "mark.jones" should contain 30 minutes for month 9 of 2030

  @NegativeCase
  Scenario: Add workload with invalid duration
    Given a trainer exists with username "maria.lee"
    When I try to add workload of -30 minutes for "maria.lee" in month 9 of 2025

  @NegativeCase
  Scenario: Add workload with past date
    Given a trainer exists with username "maria.lee"
    When I try to add workload of 60 minutes for "maria.lee" in month 1 of 2020

  @NegativeCase
  Scenario: Add workload with blank username
    When I try to add workload of 60 minutes for "" in month 9 of 2030

  @NegativeCase
  Scenario: Add workload with null duration
    Given a trainer exists with username "lucas.gray"
    When I try to add workload of 0 minutes for "lucas.gray" in month 9 of 2030