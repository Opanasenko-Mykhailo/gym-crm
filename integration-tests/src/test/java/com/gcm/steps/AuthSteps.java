package com.gcm.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

public class AuthSteps {

    private static final String TRAINEES_API = "/gym-crm-core/api/v1/trainees";
    private static final String REGISTER_PATH = "/register";
    private static final String LOGIN_PATH = "/gym-crm-core/api/v1/login";
    private static final String REFRESH_PATH = "/gym-crm-core/api/v1/refresh-token";
    private static final String LOGOUT_PATH = "/gym-crm-core/api/v1/logout";
    private static final String CHANGE_PASSWORD_PATH = "/gym-crm-core/api/v1/change-password";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${gca.test.url}")
    private String baseUrl;

    @Value("${gca.test.port}")
    private int basePort;

    private String registeredUsername;
    private String registeredPassword;
    private String accessToken;
    private String refreshToken;
    private Response response;

    @Given("GCA service is running for authentication tests")
    public void serviceIsRunning() {
        RestAssured.baseURI = baseUrl;
        RestAssured.port = basePort;
    }

    @Given("new trainee is registered for auth flow")
    public void registerNewTrainee() {
        createTrainee();
    }

    @When("I log in with the newly registered trainee credentials")
    public void loginWithRegisteredTrainee() {
        loginWithCredentials(registeredUsername, registeredPassword);

        if (response.statusCode() == OK.value()) {
            JsonPath json = response.jsonPath();

            accessToken = json.getString("accessToken");
            refreshToken = json.getString("refreshToken");
        }
    }

    @Then("login is successful and tokens are returned")
    public void loginIsSuccessful() {
        response.then().statusCode(OK.value());
        JsonPath json = response.jsonPath();

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertTrue(json.getBoolean("success"));
    }

    @When("I refresh the access token using the valid refresh token")
    public void refreshAccessToken() {
        Map<String, String> body = Map.of("refreshToken", refreshToken);

        response = given()
                .contentType(ContentType.JSON)
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + accessToken)
                .body(body)
                .post(REFRESH_PATH);
    }

    @Then("access token is successfully refreshed")
    public void accessTokenRefreshed() {
        response.then().statusCode(OK.value());
        String newAccess = response.jsonPath().getString("accessToken");
        String newRefresh = response.jsonPath().getString("refreshToken");

        assertNotNull(newAccess);
        assertNotNull(newRefresh);

        accessToken = newAccess;
        refreshToken = newRefresh;
    }

    @When("I refresh the access token using the invalid token {string}")
    public void refreshWithInvalidToken(String invalidToken) {
        Map<String, String> body = Map.of("refreshToken", invalidToken);

        response = given()
                .contentType(ContentType.JSON)
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + (accessToken != null ? accessToken : "invalid"))
                .body(body)
                .post(REFRESH_PATH);
    }

    @Then("token refresh fails with status {int}")
    public void tokenRefreshFails(int status) {
        response.then().statusCode(status);
    }

    @When("I log out with the refresh token")
    public void logoutWithRefreshToken() {
        Map<String, String> body = Map.of("refreshToken", refreshToken);

        response = given()
                .contentType(ContentType.JSON)
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + accessToken)
                .body(body)
                .post(LOGOUT_PATH);
    }

    @Then("logout is successful")
    public void logoutSuccessful() {
        response.then().statusCode(OK.value());
    }

    @When("I change the trainee password from current to {string}")
    public void changePassword(String newPassword) {
        Map<String, String> body = Map.of(
                "username", registeredUsername,
                "oldPassword", registeredPassword,
                "newPassword", newPassword);

        response = given()
                .contentType(ContentType.JSON)
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + accessToken)
                .body(body)
                .put(CHANGE_PASSWORD_PATH);

        if (response.statusCode() == OK.value()) {
            registeredPassword = newPassword;
        }
    }

    @Then("password is successfully changed")
    public void passwordChangeSuccessful() {
        response.then().statusCode(OK.value());
    }

    @When("I attempt to change the trainee password to {string} using old password {string}")
    public void changePasswordWithSpecificOldPassword(String newPassword, String oldPassword) {
        Map<String, String> body = Map.of(
                "username", registeredUsername,
                "oldPassword", oldPassword,
                "newPassword", newPassword);

        response = given()
                .contentType(ContentType.JSON)
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + accessToken)
                .body(body)
                .put(CHANGE_PASSWORD_PATH);
    }

    @Then("password change fails with status {int}")
    public void passwordChangeFails(int status) {
        response.then().statusCode(status);
    }

    @When("I attempt to log in with registered username and password {string}")
    public void loginWithInvalidPassword(String password) {
        loginWithCredentials(registeredUsername, password);
    }

    @Then("login fails with status {int}")
    public void loginFails(int status) {
        response.then().statusCode(status);
    }

    @When("I attempt to log in {int} times with password pattern {string}")
    public void attemptMultipleLoginWithPasswordPattern(int attempts, String passwordPattern) {
        for (int i = 0; i < attempts; i++) {
            loginWithCredentials(registeredUsername, passwordPattern + i);
        }
    }

    @Then("account should be locked")
    public void accountShouldBeLocked() {
        assertEquals(TOO_MANY_REQUESTS.value(), response.statusCode());
    }

    @Then("subsequent login attempts fail with status {int}")
    public void subsequentLoginAttemptsFail(int status) {
        loginWithCredentials(registeredUsername, registeredPassword);
        response.then().statusCode(status);
    }

    private void createTrainee() {
        Map<String, Object> body = Map.of(
                "firstName", "Auth",
                "lastName", "Trainee",
                "dateOfBirth", LocalDate.of(2000, 1, 1).toString(),
                "address", "Test address 44");

        response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(TRAINEES_API + REGISTER_PATH);

        response.then().statusCode(OK.value());

        JsonPath json = response.jsonPath();
        registeredUsername = json.getString("username");
        registeredPassword = json.getString("password");

        assertNotNull(registeredUsername);
        assertNotNull(registeredPassword);
    }

    private void loginWithCredentials(String username, String password) {
        Map<String, String> body = Map.of(
                "username", username,
                "password", password);

        response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(LOGIN_PATH);
    }
}