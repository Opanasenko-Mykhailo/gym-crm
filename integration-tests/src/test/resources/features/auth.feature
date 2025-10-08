Feature: End-to-End Authentication Flow
  As a system user
  I want to test the full authentication flow
  So that I can verify login, token refresh, logout, and password change

  Background:
    Given GCA service is running for authentication tests
    And new trainee is registered for auth flow

  @PositiveCase
  Scenario: Login with registered trainee
    When I log in with the newly registered trainee credentials
    Then login is successful and tokens are returned

  @PositiveCase
  Scenario: Refresh access token
    When I log in with the newly registered trainee credentials
    And I refresh the access token using the valid refresh token
    Then access token is successfully refreshed

  @PositiveCase
  Scenario: Logout
    When I log in with the newly registered trainee credentials
    And I log out with the refresh token
    Then logout is successful

  @PositiveCase
  Scenario: Change password
    When I log in with the newly registered trainee credentials
    And I change the trainee password from current to "NewPassword123!"
    Then password is successfully changed

  @NegativeCase
  Scenario: Refresh token with invalid value
    When I refresh the access token using the invalid token "invalid-token"
    Then token refresh fails with status 401

  @NegativeCase
  Scenario: Change password with wrong old password
    When I log in with the newly registered trainee credentials
    And I attempt to change the trainee password to "AnotherPassword1!" using old password "WrongOldPassword1!"
    Then password change fails with status 401

  @NegativeCase
  Scenario: Login with incorrect password
    When I attempt to log in with registered username and password "WrongPassword123!"
    Then login fails with status 401

  @NegativeCase
  Scenario: Brute force attack protection
    When I attempt to log in 6 times with password pattern "WrongPassword!"
    Then account should be locked
    And subsequent login attempts fail with status 429