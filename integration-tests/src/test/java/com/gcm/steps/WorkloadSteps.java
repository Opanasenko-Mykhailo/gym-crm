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

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WorkloadSteps {

    @Value("${workload.test.url}")
    private String workloadTestUrl;

    @Value("${workload.test.port}")
    private int workloadTestPort;

    private Response response;

    @Given("the workload service is running")
    public void theWorkloadServiceIsRunning() {
        RestAssured.baseURI = this.workloadTestUrl;
        RestAssured.port = this.workloadTestPort;

        String authToken = JwtTokenGenerator.generateToken("test-admin");

        RestAssured.requestSpecification = given().header("Authorization", "Bearer " + authToken);
    }

    @Given("trainer {string} does not exist")
    public void trainerDoesNotExist(String username) {
    }

    @Given("trainer {string} exists with following workload:")
    public void trainerExistsWithWorkload(String username, DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        String requestBody = createAddWorkloadBody(username, data.get("year"), data.get("month"), data.get("duration"));

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/workload")
                .then()
                .statusCode(200);
    }

    @Given("trainer {string} exists with workload across multiple months")
    public void trainerExistsWithWorkloadAcrossMonths(String username) {
        addWorkload(username, "2025-09-15", 120);
        addWorkload(username, "2025-10-15", 150);
        addWorkload(username, "2025-11-15", 90);
    }

    @When("I submit a workload request:")
    public void iSubmitWorkloadRequest(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        String requestBody = createGeneralWorkloadBody(
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
                .post("/api/workload");
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
                .get("/api/workload/" + username);
    }

    @Then("the request is successful")
    public void theRequestIsSuccessful() {
        response.then().statusCode(200);
    }

    @Then("the request is unsuccessful with status {int}")
    public void theRequestIsUnsuccessfulWithStatus(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @Then("trainer {string} has total duration of {int} minutes for October {int}")
    public void trainerHasTotalDuration(String username, int expectedDuration, int year) {
        Response summaryResponse = given()
                .when()
                .get("/api/workload/" + username);

        summaryResponse.then().statusCode(200);

        JsonPath jsonPath = summaryResponse.jsonPath();

        Integer actualDuration = jsonPath.getInt("years[0].months[0].duration");

        assertEquals(expectedDuration, actualDuration);
    }

    @Then("I receive a {int} not found error")
    public void iReceiveNotFoundError(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @Then("I receive complete trainer summary with yearly and monthly breakdown")
    public void iReceiveCompleteTrainerSummary() {
        response.then().statusCode(200);

        JsonPath jsonPath = response.jsonPath();
        assertNotNull(jsonPath.get("username"));
        assertNotNull(jsonPath.get("years"));
    }

    private void addWorkload(String username, String date, int duration) {
        String requestBody = createAddWorkloadBodyFromDate(username, date, duration);

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/workload");
    }

    private String createAddWorkloadBody(String username, String year, String month, String duration) {
        return String.format("""
                        {
                            "username": "%s",
                            "firstName": "%s",
                            "lastName": "%s",
                            "active": true,
                            "trainingDate": "%s-%s-15",
                            "durationInMinutes": %s,
                            "actionType": "ADD"
                        }
                        """,
                username,
                capitalize(username.split("\\.")[0]),
                capitalize(username.split("\\.")[1]),
                year,
                String.format("%02d", Integer.parseInt(month)),
                duration);
    }

    private String createAddWorkloadBodyFromDate(String username, String date, int duration) {
        String[] nameParts = username.split("\\.");

        return String.format("""
                        {
                            "username": "%s",
                            "firstName": "%s",
                            "lastName": "%s",
                            "active": true,
                            "trainingDate": "%s",
                            "durationInMinutes": %d,
                            "actionType": "ADD"
                        }
                        """,
                username,
                capitalize(nameParts[0]),
                capitalize(nameParts[1]),
                date,
                duration);
    }

    private String createGeneralWorkloadBody(String username, String firstName, String lastName, String active, String trainingDate, String durationInMinutes, String actionType) {
        return String.format("""
                        {
                            "username": "%s",
                            "firstName": "%s",
                            "lastName": "%s",
                            "active": %s,
                            "trainingDate": "%s",
                            "durationInMinutes": %s,
                            "actionType": "%s"
                        }
                        """,
                username,
                firstName,
                lastName,
                active,
                trainingDate,
                durationInMinutes,
                actionType);
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}