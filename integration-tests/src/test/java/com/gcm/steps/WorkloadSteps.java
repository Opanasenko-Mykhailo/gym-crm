package com.gcm.steps;

import com.gcm.utils.JwtTokenGenerator;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import static com.gcm.utils.DataTableUtils.extractData;
import static com.gcm.utils.DataTableUtils.normalizeEmptyValues;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class WorkloadSteps {

    private static final String API_WORKLOAD = "/api/workload";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACTION_ADD = "ADD";
    private static final String JSONPATH_DURATION = "years[0].months[0].duration";

    @Value("${workload.test.url}")
    private String workloadTestUrl;

    @Value("${workload.test.port}")
    private int workloadTestPort;

    private Response response;

    @Given("the workload service is running")
    public void theWorkloadServiceIsRunning() {
        RestAssured.baseURI = this.workloadTestUrl;
        RestAssured.port = this.workloadTestPort;

        String authToken = JwtTokenGenerator.generateTrainerToken("test-admin");

        RestAssured.requestSpecification = given().header(HEADER_AUTHORIZATION, BEARER_PREFIX + authToken);
    }

    @Given("trainer {string} does not exist")
    public void trainerDoesNotExist(String username) {
    }

    @Given("trainer {string} exists with following workload:")
    public void trainerExistsWithWorkload(String username, DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);
        data = normalizeEmptyValues(data);

        Map<String, Object> requestBody = createAddWorkloadBody(username, data.get("year"), data.get("month"), data.get("duration"));

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(API_WORKLOAD)
                .then()
                .statusCode(OK.value());
    }

    @Given("trainer {string} exists with workload across multiple months")
    public void trainerExistsWithWorkloadAcrossMonths(String username) {
        addWorkload(username, "2025-09-15", 120);
        addWorkload(username, "2025-10-15", 150);
        addWorkload(username, "2025-11-15", 90);
    }

    @When("I submit a workload request:")
    public void iSubmitWorkloadRequest(DataTable dataTable) {
        Map<String, String> data = extractData(dataTable);
        data = normalizeEmptyValues(data);

        Map<String, Object> requestBody = createGeneralWorkloadBody(
                data.get("username"),
                data.get("firstName"),
                data.get("lastName"),
                data.get("active"),
                data.get("trainingDate"),
                data.get("durationInMinutes"),
                data.get("actionType"));

        response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(API_WORKLOAD);
    }

    @When("I add a training session:")
    public void iAddTrainingSession(DataTable dataTable) {
        iSubmitWorkloadRequest(dataTable);
    }

    @When("I delete a training session:")
    public void iDeleteTrainingSession(DataTable dataTable) {
        iSubmitWorkloadRequest(dataTable);
    }

    @When("I request summary for trainer {string}")
    public void iRequestSummaryForTrainer(String username) {
        response = given()
                .when()
                .get(API_WORKLOAD + "/" + username);
    }

    @Then("the request is successful")
    public void theRequestIsSuccessful() {
        response.then().statusCode(OK.value());
    }

    @Then("the request is unsuccessful with status {int}")
    public void theRequestIsUnsuccessfulWithStatus(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @Then("trainer {string} has total duration of {int} minutes for October {int}")
    public void trainerHasTotalDuration(String username, int expectedDuration, int year) {
        Response summaryResponse = given()
                .when()
                .get(API_WORKLOAD + "/" + username);

        summaryResponse.then().statusCode(OK.value());

        JsonPath jsonPath = summaryResponse.jsonPath();
        Integer actualDuration = jsonPath.getInt(JSONPATH_DURATION);

        assertEquals(expectedDuration, actualDuration);
    }

    @Then("I receive a {int} not found error")
    public void iReceiveNotFoundError(int statusCode) {
        response.then().statusCode(NOT_FOUND.value());
    }

    @Then("I receive complete trainer summary with yearly and monthly breakdown")
    public void iReceiveCompleteTrainerSummary() {
        response.then().statusCode(OK.value());

        JsonPath jsonPath = response.jsonPath();
        assertNotNull(jsonPath.get("username"));
        assertNotNull(jsonPath.get("years"));
    }

    private void addWorkload(String username, String date, int duration) {
        Map<String, Object> requestBody = createAddWorkloadBodyFromDate(username, date, duration);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(API_WORKLOAD);
    }

    private Map<String, Object> createAddWorkloadBody(String username, String year, String month, String duration) {
        String[] nameParts = username.split("\\.");
        return Map.of(
                "username", username,
                "firstName", capitalize(nameParts[0]),
                "lastName", capitalize(nameParts[1]),
                "active", true,
                "trainingDate", String.format("%s-%02d-15", year, Integer.parseInt(month)),
                "durationInMinutes", Integer.parseInt(duration),
                "actionType", ACTION_ADD);
    }

    private Map<String, Object> createAddWorkloadBodyFromDate(String username, String date, int duration) {
        String[] nameParts = username.split("\\.");
        return Map.of(
                "username", username,
                "firstName", capitalize(nameParts[0]),
                "lastName", capitalize(nameParts[1]),
                "active", true,
                "trainingDate", date,
                "durationInMinutes", duration,
                "actionType", ACTION_ADD);
    }

    private Map<String, Object> createGeneralWorkloadBody(String username, String firstName, String lastName, String active, String trainingDate, String durationInMinutes, String actionType) {
        return Map.of(
                "username", username,
                "firstName", firstName,
                "lastName", lastName,
                "active", Boolean.parseBoolean(active),
                "trainingDate", trainingDate,
                "durationInMinutes", Integer.parseInt(durationInMinutes),
                "actionType", actionType);
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}