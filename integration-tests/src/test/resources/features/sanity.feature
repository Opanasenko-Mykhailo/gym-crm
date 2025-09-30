Feature: Sanity Check

  Scenario: Simple true check
    Given a test scenario
    When I check true
    Then it should be true