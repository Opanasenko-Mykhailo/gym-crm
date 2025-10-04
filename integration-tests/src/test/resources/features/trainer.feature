Feature: Trainer Management
  As a system user
  I want to manage trainers
  So that I can register and update trainer profiles

  Background:
    Given the GCC service for trainer module is running

  @PositiveCase
  Scenario: Register new trainer
    When I register a trainer via trainer module:
      | field          | value   |
      | firstName      | Marcus  |
      | lastName       | Johnson |
      | specialization | YOGA    |
    Then the trainer registration is successful
    And I receive generated trainer credentials
    And the trainer username follows pattern "Marcus.Johnson"

  @PositiveCase
  Scenario: Register trainer with same name (username uniqueness)
    Given trainer with name "Sarah" "Williams" already exists
    When I register a trainer via trainer module:
      | field          | value    |
      | firstName      | Sarah    |
      | lastName       | Williams |
      | specialization | CARDIO   |
    Then the trainer registration is successful
    And the trainer username has numeric suffix

  @PositiveCase
  Scenario: Get trainer profile
    Given trainer "Emma.Davis" exists with trainer data:
      | firstName      | Emma   |
      | lastName       | Davis  |
      | specialization | CARDIO |
      | isActive       | true   |
    When I request trainer profile for "Emma.Davis"
    Then I receive trainer profile data with:
      | firstName      | Emma   |
      | lastName       | Davis  |
      | specialization | CARDIO |
      | isActive       | true   |

  @PositiveCase
  Scenario: Update trainer profile
    Given trainer "James.Brown" exists with trainer data:
      | firstName      | James |
      | lastName       | Brown |
      | specialization | YOGA  |
      | isActive       | true  |
    When I update trainer "James.Brown" profile via trainer module:
      | field          | value  |
      | firstName      | James  |
      | lastName       | Brown  |
      | specialization | CARDIO |
      | isActive       | true   |
    Then the update is successful
    And trainer "James.Brown" has specialization "CARDIO"

  @NegativeCase
  Scenario: Register trainer with empty first name
    When I register a trainer via trainer module:
      | field          | value |
      | firstName      |       |
      | lastName       | Smith |
      | specialization | YOGA  |
    Then the trainer request is unsuccessful with status 400

  @NegativeCase
  Scenario: Register trainer with empty last name
    When I register a trainer via trainer module:
      | field          | value |
      | firstName      | John  |
      | lastName       |       |
      | specialization | YOGA  |
    Then the trainer request is unsuccessful with status 400

  @NegativeCase
  Scenario: Register trainer with empty specialization
    When I register a trainer via trainer module:
      | field          | value |
      | firstName      | John  |
      | lastName       | Smith |
      | specialization |       |
    Then the trainer request is unsuccessful with status 404

  @NegativeCase
  Scenario: Register trainer with too long first name
    When I register a trainer via trainer module:
      | field          | value                                                      |
      | firstName      | ThisIsAVeryLongFirstNameThatExceedsFiftyCharactersLimit123 |
      | lastName       | Smith                                                      |
      | specialization | YOGA                                                       |
    Then the trainer request is unsuccessful with status 400

  @NegativeCase
  Scenario: Get non-existent trainer profile
    Given trainer "Non.Existent" does not exist in trainer module
    When I request trainer profile for "Non.Existent"
    Then I receive a trainer not found error with status 404

  @NegativeCase
  Scenario: Update trainer with empty first name
    Given trainer "Mike.Wilson" exists with trainer data:
      | firstName      | Mike   |
      | lastName       | Wilson |
      | specialization | YOGA   |
      | isActive       | true   |
    When I update trainer "Mike.Wilson" profile via trainer module:
      | field          | value  |
      | firstName      |        |
      | lastName       | Wilson |
      | specialization | CARDIO |
      | isActive       | true   |
    Then the trainer request is unsuccessful with status 400