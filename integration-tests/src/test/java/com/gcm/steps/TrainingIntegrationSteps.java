package com.gcm.steps;

import com.gcm.testutils.DataTableUtils;
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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.OK;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class TrainingIntegrationSteps {

    private static final String API_TRAINERS_GCC = "/gym-crm-core/api/v1/trainers";
    private static final String API_TRAINEES_GCC = "/gym-crm-core/api/v1/trainees";
    private static final String API_TRAININGS_GCC = "/gym-crm-core/api/v1/trainings";
    private static final String API_WORKLOAD = "/api/workload";
    private static final String REGISTER_PATH = "/register";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${gca.test.url}")
    private String gcaTestUrl;

    @Value("${gca.test.port}")
    private int gcaTestPort;

    @Value("${workload.test.url}")
    private String workloadTestUrl;

    @Value("${workload.test.port}")
    private int workloadTestPort;

    private Response trainingResponse;
    private Response summaryResponse;
    private String currentTrainerUsername;

    @Given("the GCA and workload services are running for integration tests")
    public void bothServicesAreRunning() {
        setupGcaServiceConnection();
        setupWorkloadServiceConnection();
    }

    @Given("integration test trainer {string} exists with trainer data:")
    public void integrationTrainerExistsWithData(String username, DataTable dataTable) {
        setupGcaServiceConnection();

        Map<String, String> data = dataTable.asMap(String.class, String.class);

        String requestBody = buildTrainerRegistrationBody(
                data.get("firstName"),
                data.get("lastName"),
                data.get("specialization"));

        Response registerResponse = sendTrainerRegistrationRequest(requestBody);
        registerResponse.then().statusCode(OK.value());

        currentTrainerUsername = registerResponse.jsonPath().getString("username");
    }

    @Given("integration test trainee {string} exists with trainee data:")
    public void integrationTraineeExistsWithData(String username, DataTable dataTable) {
        setupGcaServiceConnection();

        Map<String, String> data = dataTable.asMap(String.class, String.class);

        String requestBody = buildTraineeRegistrationBody(
                data.get("firstName"),
                data.get("lastName"),
                data.get("dateOfBirth"),
                data.get("address"));

        Response registerResponse = sendTraineeRegistrationRequest(requestBody);
        registerResponse.then().statusCode(OK.value());
    }

    @Then("integration test trainer {string} has total duration of {int} minutes for October {int}")
    public void integrationTrainerHasTotalDuration(String username, int expectedDuration, int year) {
        setupWorkloadServiceConnection();

        String authToken = JwtTokenGenerator.generateToken(currentTrainerUsername);

        Response workloadResponse = given()
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + authToken)
                .when()
                .get(API_WORKLOAD + "/" + username);

        workloadResponse.then().statusCode(OK.value());
        JsonPath jsonPath = workloadResponse.jsonPath();
        Integer actualDuration = jsonPath.getInt("years[0].months[0].duration");

        assertEquals(expectedDuration, actualDuration);
    }

    @When("I create a training in GCA:")
    public void createTrainingInGCA(DataTable dataTable) {
        setupGcaServiceConnection();

        Map<String, String> data = DataTableUtils.extractData(dataTable);
        data = DataTableUtils.normalizeEmptyValues(data);

        String requestBody = buildTrainingCreateRequestBody(
                data.get("traineeUsername"),
                data.get("trainerUsername"),
                data.get("trainingName"),
                data.get("trainingDate"),
                data.get("trainingDuration"),
                data.get("trainingTypeName"));

        String authToken = JwtTokenGenerator.generateToken(currentTrainerUsername);

        trainingResponse = given()
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + authToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(API_TRAININGS_GCC);

        trainingResponse.then().statusCode(OK.value());

        waitForTrainingToPropagate();
    }

    @When("I request workload summary for trainer {string}")
    public void requestWorkloadSummaryForTrainer(String username) {
        setupWorkloadServiceConnection();

        String authToken = JwtTokenGenerator.generateToken(currentTrainerUsername);

        summaryResponse = given()
                .header(HEADER_AUTHORIZATION, BEARER_PREFIX + authToken)
                .when()
                .get(API_WORKLOAD + "/" + username);
    }

    @Then("the training creation is successful")
    public void trainingCreationIsSuccessful() {
        trainingResponse.then().statusCode(OK.value());
    }

    @Then("I receive trainer workload summary with yearly and monthly breakdown")
    public void receiveTrainerWorkloadSummary() {
        summaryResponse.then().statusCode(OK.value());

        JsonPath jsonPath = summaryResponse.jsonPath();
        assertNotNull(jsonPath.get("username"));
        assertNotNull(jsonPath.get("years"));
    }

    @Then("the workload summary contains trainer information:")
    public void workloadSummaryContainsTrainerInfo(DataTable dataTable) {
        Map<String, String> expected = dataTable.asMap(String.class, String.class);
        JsonPath jsonPath = summaryResponse.jsonPath();

        assertEquals(expected.get("username"), jsonPath.getString("username"));
        assertEquals(expected.get("firstName"), jsonPath.getString("firstName"));
        assertEquals(expected.get("lastName"), jsonPath.getString("lastName"));
        assertEquals(Boolean.parseBoolean(expected.get("active")), jsonPath.getBoolean("active"));
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

    private String buildTraineeRegistrationBody(String firstName, String lastName, String dateOfBirth, String address) {
        return String.format("""
                {
                    "firstName": "%s",
                    "lastName": "%s",
                    "dateOfBirth": "%s",
                    "address": "%s"
                }
                """, firstName, lastName, dateOfBirth, address);
    }

    private String buildTrainingCreateRequestBody(
            String traineeUsername,
            String trainerUsername,
            String trainingName,
            String trainingDate,
            String trainingDuration,
            String trainingTypeName) {
        return String.format("""
                {
                    "traineeUsername": "%s",
                    "trainerUsername": "%s",
                    "trainingName": "%s",
                    "trainingDate": "%s",
                    "trainingDuration": %s,
                    "trainingTypeName": "%s"
                }
                """, traineeUsername, trainerUsername, trainingName, trainingDate, trainingDuration, trainingTypeName);
    }

    private Response sendTrainerRegistrationRequest(String requestBody) {
        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(API_TRAINERS_GCC + REGISTER_PATH);
    }

    private Response sendTraineeRegistrationRequest(String requestBody) {
        return given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(API_TRAINEES_GCC + REGISTER_PATH);
    }

    private void setupGcaServiceConnection() {
        RestAssured.baseURI = this.gcaTestUrl;
        RestAssured.port = this.gcaTestPort;
        RestAssured.requestSpecification = null;
    }

    private void setupWorkloadServiceConnection() {
        RestAssured.baseURI = this.workloadTestUrl;
        RestAssured.port = this.workloadTestPort;
    }

    private void waitForTrainingToPropagate() {
        await().pollDelay(1, SECONDS).until(() -> true);
    }
}