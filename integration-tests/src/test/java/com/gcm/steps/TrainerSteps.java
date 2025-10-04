package com.gcm.steps;

import com.gcm.testutils.JwtTokenGenerator;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

import static com.gcm.testutils.DataTableUtils.extractData;
import static com.gcm.testutils.DataTableUtils.normalizeEmptyValues;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class TrainerSteps {

    private static final String API_TRAINERS = "/gym-crm-core/api/v1/trainers";
    private static final String REGISTER_PATH = "/register";
    private static final String ACTIVATION_PATH = "/change-activation-status";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String EXISTING_TRAINER_USERNAME = "Mike.Wilson";

    private final Map<String, String> createdTrainerUsers = new HashMap<>();

    @Value("${gca.test.url}")
    private String gcaTestUrl;

    @Value("${gca.test.port}")
    private int gcaTestPort;

    private Response trainerResponse;

    @Given("the GCC service for trainer module is running")
    public void trainerStepServiceIsRunning() {
        RestAssured.baseURI = this.gcaTestUrl;
        RestAssured.port = this.gcaTestPort;
    }

    @Given("trainer {string} does not exist in trainer module")
    public void trainerStepDoesNotExist(String username) {
    }

    @Given("trainer {string} exists with trainer data:")
    public void trainerStepExistsWith(String username, DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        String requestBody = buildTrainerRegistrationBody(
                data.get("firstName"),
                data.get("lastName"),
                data.get("specialization"));

        Response registerResponse = sendRegistrationRequest(requestBody);
        registerResponse.then().statusCode(OK.value());

        String generatedUsername = registerResponse.jsonPath().getString("username");
        String password = registerResponse.jsonPath().getString("password");
        createdTrainerUsers.put(username, password);

        if ("false".equals(data.get("isActive"))) {
            deactivateTrainer(generatedUsername);
        }
    }

    @Given("trainer with name {string} {string} already exists")
    public void trainerWithNameAlreadyExists(String firstName, String lastName) {
        String username = String.format("%s.%s", firstName, lastName);

        if (!createdTrainerUsers.containsKey(username)) {
            createTrainerIfNotExists(firstName, lastName, username);
        }
    }

    @When("I request trainer profile for {string}")
    public void trainerStepRequestProfile(String username) {
        String authToken = JwtTokenGenerator.generateToken(EXISTING_TRAINER_USERNAME);
        trainerResponse = sendGetProfileRequest(username, authToken);
    }

    @When("I register a trainer via trainer module:")
    public void trainerStepRegisterTrainer(DataTable dataTable) {
        Map<String, String> data = extractData(dataTable);
        data = normalizeEmptyValues(data);

        String requestBody = buildTrainerRegistrationBody(
                data.get("firstName"),
                data.get("lastName"),
                data.get("specialization"));

        trainerResponse = sendRegistrationRequest(requestBody);
    }

    @When("I update trainer {string} profile via trainer module:")
    public void updateTrainerProfileViaModule(String username, DataTable dataTable) {
        Map<String, String> data = extractData(dataTable);
        data = normalizeEmptyValues(data);

        String authToken = JwtTokenGenerator.generateToken(username);
        String requestBody = buildTrainerUpdateBody(
                data.get("firstName"),
                data.get("lastName"),
                data.get("specialization"),
                data.get("isActive"));

        trainerResponse = sendUpdateRequest(username, requestBody, authToken);
    }

    @Then("the trainer registration is successful")
    public void trainerStepRegistrationSuccessful() {
        trainerResponse.then().statusCode(OK.value());
    }

    @Then("the trainer request is successful")
    public void trainerStepRequestSuccessful() {
        trainerResponse.then().statusCode(OK.value());
    }

    @Then("I receive generated trainer credentials")
    public void trainerStepReceiveGeneratedCredentials() {
        JsonPath jsonPath = trainerResponse.jsonPath();
        assertNotNull(jsonPath.getString("username"));
        assertNotNull(jsonPath.getString("password"));
        assertFalse(jsonPath.getString("username").isEmpty());
        assertFalse(jsonPath.getString("password").isEmpty());
    }

    @Then("the trainer username follows pattern {string}")
    public void trainerUsernameFollowsPattern(String pattern) {
        String actualUsername = trainerResponse.jsonPath().getString("username");
        assertEquals(pattern, actualUsername, "Trainer username should follow the expected pattern");
    }

    @Then("the trainer username has numeric suffix")
    public void trainerUsernameHasNumericSuffix() {
        String username = trainerResponse.jsonPath().getString("username");
        assertTrue(username.matches(".+\\d+$"), "Username should have numeric suffix");
    }

    @Then("I receive trainer profile data with:")
    public void trainerStepReceiveProfile(DataTable dataTable) {
        trainerResponse.then().statusCode(OK.value());

        Map<String, String> expected = dataTable.asMap(String.class, String.class);
        JsonPath jsonPath = trainerResponse.jsonPath();

        assertProfileData(expected, jsonPath);
    }

    @Then("I receive a trainer not found error")
    public void trainerStepNotFoundError() {
        trainerResponse.then().statusCode(NOT_FOUND.value());
    }

    @Then("I receive a trainer not found error with status {int}")
    public void trainerStepNotFoundErrorWithStatus(int statusCode) {
        trainerResponse.then().statusCode(statusCode);
    }

    @Then("the update is successful")
    public void theUpdateIsSuccessful() {
        trainerResponse.then().statusCode(OK.value());
    }

    @Then("trainer {string} has specialization {string}")
    public void trainerHasSpecialization(String username, String specialization) {
        String authToken = JwtTokenGenerator.generateToken(EXISTING_TRAINER_USERNAME);
        Response response = sendGetProfileRequest(username, authToken);

        response.then().statusCode(OK.value());
        String actualSpecialization = response.jsonPath().getString("specialization");
        assertEquals(specialization, actualSpecialization, "Trainer specialization should match");
    }

    @Then("the trainer request is unsuccessful with status {int}")
    public void trainerRequestUnsuccessful(int status) {
        trainerResponse.then().statusCode(status);
    }

    private String buildTrainerRegistrationBody(String firstName, String lastName, String specialization) {
        return String.format("""
                {
                    "firstName": "%s",
                    "lastName": "%s",
                    "specialization": "%s"
                }
                """, firstName, lastName, specialization);
    }

    private String buildTrainerUpdateBody(String firstName, String lastName, String specialization, String isActive) {
        return String.format("""
                {
                    "firstName": "%s",
                    "lastName": "%s",
                    "specialization": "%s",
                    "isActive": %s
                }
                """, firstName, lastName, specialization, isActive);
    }

    private String buildDeactivationBody() {
        return "{\"isActive\": false}";
    }

    private Response sendRegistrationRequest(String requestBody) {
        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(API_TRAINERS + REGISTER_PATH);
    }

    private Response sendGetProfileRequest(String username, String authToken) {
        return given()
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + authToken)
                .when()
                .get(API_TRAINERS + "/" + username);
    }

    private Response sendUpdateRequest(String username, String requestBody, String authToken) {
        return given()
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + authToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put(API_TRAINERS + "/" + username);
    }

    private void deactivateTrainer(String username) {
        String authToken = JwtTokenGenerator.generateToken(username);
        given()
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + authToken)
                .contentType(ContentType.JSON)
                .body(buildDeactivationBody())
                .when()
                .patch(API_TRAINERS + "/" + username + ACTIVATION_PATH);
    }

    private void createTrainerIfNotExists(String firstName, String lastName, String username) {
        String requestBody = buildTrainerRegistrationBody(firstName, lastName, "CARDIO");
        Response registerResponse = sendRegistrationRequest(requestBody);
        registerResponse.then().statusCode(OK.value());

        String password = registerResponse.jsonPath().getString("password");
        createdTrainerUsers.put(username, password);
    }

    private void assertProfileData(Map<String, String> expected, JsonPath jsonPath) {
        assertEquals(expected.get("firstName"), jsonPath.getString("firstName"));
        assertEquals(expected.get("lastName"), jsonPath.getString("lastName"));
        assertEquals(expected.get("specialization"), jsonPath.getString("specialization"));
        assertEquals(Boolean.parseBoolean(expected.get("isActive")), jsonPath.getBoolean("isActive"));
    }
}