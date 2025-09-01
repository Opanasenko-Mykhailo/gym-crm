package com.gcs.app.controller.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.gcs.app.util.JsonReaderUtil;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrainerSecurityControllerTest extends AbstractSecurityControllerTest {

    private static final String USERNAME = "rowan.atkinson";
    private static final String TEST_DATA_PATH = "json/security/trainer-test-data.json";

    private static JsonNode testData;

    @BeforeAll
    static void setup() {
        testData = JsonReaderUtil.readFromJson(TEST_DATA_PATH, JsonNode.class);
    }

    @DisplayName("Register should be accessible without authentication")
    @Test
    void register_shouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(post(basePath + "/trainers/register")
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

    @DisplayName("Access control tests for different users and roles")
    @ParameterizedTest(name = "{index} => user={0}, role={1}, method={2}, endpoint={3}, expectedStatus={4}")
    @MethodSource("accessControlDataProvider")
    void accessControlTests(String username, String role, String method, String endpoint, int expectedStatus) throws Exception {
        mockMvc.perform(buildRequestWithAuth(method, endpoint, username, role))
                .andExpect(status().is(expectedStatus));
    }

    private static Stream<Arguments> unauthorizedRequestProvider() {
        return Stream.of(
                arguments("GET", "/trainers/" + USERNAME),
                arguments("PUT", "/trainers/" + USERNAME),
                arguments("PATCH", "/trainers/" + USERNAME + "/change-activation-status"),
                arguments("GET", "/trainers/" + USERNAME + "/trainings")
        );
    }

    private static Stream<Arguments> accessControlDataProvider() {
        return Stream.of(
                arguments("rowan.atkinson", "TRAINER", "GET", "/trainers/rowan.atkinson", 200),
                arguments("some.trainee", "TRAINEE", "GET", "/trainers/rowan.atkinson", 403),

                arguments("rowan.atkinson", "TRAINER", "PUT", "/trainers/rowan.atkinson", 200),
                arguments("other.trainer", "TRAINER", "PUT", "/trainers/rowan.atkinson", 403),
                arguments("some.trainee", "TRAINEE", "PUT", "/trainers/rowan.atkinson", 403),

                arguments("rowan.atkinson", "TRAINER", "PATCH", "/trainers/rowan.atkinson/change-activation-status", 200),
                arguments("other.trainer", "TRAINER", "PATCH", "/trainers/rowan.atkinson/change-activation-status", 403),
                arguments("some.trainee", "TRAINEE", "PATCH", "/trainers/rowan.atkinson/change-activation-status", 403),

                arguments("rowan.atkinson", "TRAINER", "GET", "/trainers/rowan.atkinson/trainings", 200),
                arguments("some.trainee", "TRAINEE", "GET", "/trainers/rowan.atkinson/trainings", 403)
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
            case "PATCH" -> patch(fullUrl).contentType(MediaType.APPLICATION_JSON).content(content);
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
    }

    private String getContentForEndpoint(String endpoint) {
        if (endpoint.endsWith("/change-activation-status")) {
            return getActivationJson();
        } else if (endpoint.equals("/trainers/" + USERNAME)) {
            return getUpdateJson();
        }
        return "";
    }

    private static String getCreateJson() {
        return testData.get("trainerCreateRequest").toString();
    }

    private static String getUpdateJson() {
        return testData.get("trainerUpdateRequest").toString();
    }

    private static String getActivationJson() {
        return testData.get("activationStatusRequest").toString();
    }
}