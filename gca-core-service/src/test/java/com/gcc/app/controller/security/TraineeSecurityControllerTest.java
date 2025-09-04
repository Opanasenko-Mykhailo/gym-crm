package com.gcc.app.controller.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.gcc.app.util.JsonReaderUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TraineeSecurityControllerTest extends AbstractSecurityControllerTest {

    private static final String TRAINEE_USERNAME = "oleksandr.kovalenko";
    private static final String TEST_DATA_PATH = "json/security/trainee-test-data.json";

    private static JsonNode testData;

    @BeforeAll
    static void init() {
        testData = JsonReaderUtil.readFromJson(TEST_DATA_PATH, JsonNode.class);
    }

    @DisplayName("Register should be accessible without authentication")
    @Test
    void register_shouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(post(basePath + "/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getCreateJson()))
                .andExpect(status().isOk());
    }

    @DisplayName("All protected endpoints should return 401 without authentication")
    @ParameterizedTest(name = "{index} => method={0}, endpoint={1}")
    @MethodSource("unauthorizedRequestProvider")
    void request_shouldBeUnauthorizedWithoutAuth(String method, String endpoint) throws Exception {
        mockMvc.perform(buildRequestWithoutAuth(method, endpoint))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Access control for different users and roles")
    @ParameterizedTest(name = "{index} => user={0}, role={1}, method={2}, endpoint={3}, expectedStatus={4}")
    @MethodSource("accessControlDataProvider")
    void accessControlTests(String username, String role, String method, String endpoint, int expectedStatus) throws Exception {
        mockMvc.perform(buildRequestWithAuth(method, endpoint, username, role))
                .andExpect(status().is(expectedStatus));
    }

    private static Stream<Arguments> unauthorizedRequestProvider() {
        return Stream.of(
                arguments("GET", "/trainees/" + TRAINEE_USERNAME),
                arguments("PUT", "/trainees/" + TRAINEE_USERNAME),
                arguments("DELETE", "/trainees/" + TRAINEE_USERNAME),
                arguments("PATCH", "/trainees/" + TRAINEE_USERNAME + "/change-activation-status"),
                arguments("GET", "/trainees/" + TRAINEE_USERNAME + "/available-trainers"),
                arguments("PUT", "/trainees/" + TRAINEE_USERNAME + "/trainers"),
                arguments("GET", "/trainees/" + TRAINEE_USERNAME + "/trainings")
        );
    }

    private static Stream<Arguments> accessControlDataProvider() {
        return Stream.of(
                arguments("oleksandr.kovalenko", "TRAINEE", "GET", "/trainees/oleksandr.kovalenko", 200),
                arguments("trainer.user", "TRAINER", "GET", "/trainees/oleksandr.kovalenko", 200),
                arguments("other.user", "TRAINEE", "GET", "/trainees/oleksandr.kovalenko", 200),

                arguments("oleksandr.kovalenko", "TRAINEE", "PUT", "/trainees/oleksandr.kovalenko", 200),
                arguments("trainer.user", "TRAINER", "PUT", "/trainees/oleksandr.kovalenko", 403),
                arguments("other.user", "TRAINEE", "PUT", "/trainees/oleksandr.kovalenko", 403),

                arguments("oleksandr.kovalenko", "TRAINEE", "DELETE", "/trainees/oleksandr.kovalenko", 200),
                arguments("trainer.user", "TRAINER", "DELETE", "/trainees/oleksandr.kovalenko", 403),
                arguments("other.user", "TRAINEE", "DELETE", "/trainees/oleksandr.kovalenko", 403),

                arguments("trainer.user", "TRAINER", "PATCH", "/trainees/oleksandr.kovalenko/change-activation-status", 200),
                arguments("oleksandr.kovalenko", "TRAINEE", "PATCH", "/trainees/oleksandr.kovalenko/change-activation-status", 403),
                arguments("other.user", "TRAINEE", "PATCH", "/trainees/oleksandr.kovalenko/change-activation-status", 403),

                arguments("oleksandr.kovalenko", "TRAINEE", "GET", "/trainees/oleksandr.kovalenko/available-trainers", 200),
                arguments("trainer.user", "TRAINER", "GET", "/trainees/oleksandr.kovalenko/available-trainers", 403),
                arguments("other.user", "TRAINEE", "GET", "/trainees/oleksandr.kovalenko/available-trainers", 403),

                arguments("oleksandr.kovalenko", "TRAINEE", "PUT", "/trainees/oleksandr.kovalenko/trainers", 200),
                arguments("trainer.user", "TRAINER", "PUT", "/trainees/oleksandr.kovalenko/trainers", 403),
                arguments("other.user", "TRAINEE", "PUT", "/trainees/oleksandr.kovalenko/trainers", 403),

                arguments("oleksandr.kovalenko", "TRAINEE", "GET", "/trainees/oleksandr.kovalenko/trainings", 200),
                arguments("trainer.user", "TRAINER", "GET", "/trainees/oleksandr.kovalenko/trainings", 200),
                arguments("other.user", "TRAINEE", "GET", "/trainees/oleksandr.kovalenko/trainings", 200)
        );
    }

    private MockHttpServletRequestBuilder buildRequestWithAuth(String method, String endpoint, String username, String role) {
        return buildRequest(method, endpoint)
                .with(SecurityMockMvcRequestPostProcessors.user(username).roles(role));
    }

    private MockHttpServletRequestBuilder buildRequestWithoutAuth(String method, String endpoint) {
        return buildRequest(method, endpoint);
    }

    private MockHttpServletRequestBuilder buildRequest(String method, String endpoint) {
        String fullUrl = basePath + endpoint;
        String content = getContentForEndpoint(endpoint);

        return switch (method) {
            case "GET" -> get(fullUrl);
            case "PUT" -> put(fullUrl).contentType(MediaType.APPLICATION_JSON).content(content);
            case "DELETE" -> delete(fullUrl);
            case "PATCH" -> patch(fullUrl).contentType(MediaType.APPLICATION_JSON).content(content);
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
    }

    private String getContentForEndpoint(String endpoint) {
        if (endpoint.endsWith("/change-activation-status")) {
            return getActivationJson();
        } else if (endpoint.endsWith("/trainers")) {
            return getTrainersUpdateJson();
        } else if (endpoint.equals("/trainees/" + TRAINEE_USERNAME)) {
            return getUpdateJson();
        }
        return "";
    }

    private static String getCreateJson() {
        return testData.get("traineeCreateRequest").toString();
    }

    private static String getUpdateJson() {
        return testData.get("traineeUpdateRequest").toString();
    }

    private static String getActivationJson() {
        return testData.get("activationStatusRequest").toString();
    }

    private static String getTrainersUpdateJson() {
        return testData.get("traineeAssignedTrainersUpdateRequest").toString();
    }
}