package com.gcc.app.logging.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataMaskerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SensitiveDataMasker masker = new SensitiveDataMasker(objectMapper);

    @ParameterizedTest(name = "{index} => input={0}")
    @MethodSource("provideSensitiveInputMaps")
    @DisplayName("Should mask only sensitive fields in flat structures")
    void shouldMaskSensitiveFlatFields(Map<String, Object> input) {
        String masked = masker.mask(input);

        List sensitiveKeys = input.keySet().stream()
                .filter(this::isSensitiveKey)
                .toList();
        List nonSensitiveKeys = input.keySet().stream()
                .filter(k -> !isSensitiveKey(k))
                .toList();

        sensitiveKeys.forEach(key ->
                assertThat(masked)
                        .as("Expect '%s' to be masked", key)
                        .contains("\"%s\":\"********\"".formatted(key))
        );
        nonSensitiveKeys.forEach(key -> {
            Object value = input.get(key);
            String expected = value instanceof String
                    ? "\"%s\":\"%s\"".formatted(key, value)
                    : "\"%s\":%s".formatted(key, value);

            assertThat(masked)
                    .as("Expect non-sensitive field '%s' to remain unmasked", key)
                    .contains(expected);
        });
    }

    @ParameterizedTest(name = "{index} => nestedInput={0}")
    @MethodSource("provideNestedSensitiveInputMaps")
    @DisplayName("Should mask nested sensitive fields")
    void shouldMaskNestedSensitiveFields(Map<String, Object> nestedInput) {
        String masked = masker.mask(nestedInput);
        assertThat(masked).contains("\"secret\":\"********\"");
    }

    @ParameterizedTest(name = "{index} => arrayInput={0}")
    @MethodSource("provideArraySensitiveInputMaps")
    @DisplayName("Should mask sensitive fields in arrays")
    void shouldMaskArraySensitiveFields(Map<String, Object> arrayInput) {
        String masked = masker.mask(arrayInput);
        assertThat(masked).contains("********");
    }

    @Test
    @DisplayName("Should mask ResponseEntity body")
    void shouldMaskResponseEntity() {
        Map<String, Object> body = Map.of(
                "token", "sensitive-token",
                "info", "public"
        );
        ResponseEntity<?> response = new ResponseEntity<>(body, HttpStatus.OK);

        String masked = masker.mask(response);

        assertThat(masked)
                .contains("status=200 OK")
                .contains("\"token\":\"********\"")
                .contains("\"info\":\"public\"");
    }

    @Test
    @DisplayName("Should handle null input gracefully")
    void shouldHandleNullInput() {
        String masked = masker.mask(null);
        assertThat(masked).isEmpty();
    }

    @Test
    @DisplayName("Should fallback to toString() if not JSON-serializable")
    void shouldFallbackToToString() {
        Object nonSerializable = new Object() {
            @Override
            public String toString() {
                return "custom-object";
            }
        };

        String masked = masker.mask(nonSerializable);
        assertThat(masked).isEqualTo("custom-object");
    }

    @Test
    @DisplayName("Should mask multiple arguments in array")
    void shouldMaskArguments() {
        Object[] args = new Object[]{
                Map.of("password", "1234"),
                Map.of("username", "bob"),
                "stringValue"
        };

        String result = masker.maskArguments(args);

        assertThat(result)
                .contains("\"password\":\"********\"")
                .contains("\"username\":\"bob\"")
                .contains("stringValue")
                .startsWith("[")
                .endsWith("]");
    }

    private static Stream<Map<String, Object>> provideSensitiveInputMaps() {
        return Stream.of(
                Map.of("username", "john", "password", "12345"),
                Map.of("token", "secret-token", "id", 1),
                Map.of("password", "abc", "token", "zzz", "extra", "value")
        );
    }

    private boolean isSensitiveKey(String key) {
        return key.toLowerCase().contains("password")
                || key.toLowerCase().contains("token")
                || key.toLowerCase().contains("secret");
    }

    private static Stream<Map<String, Object>> provideNestedSensitiveInputMaps() {
        return Stream.of(
                Map.of("user", Map.of("name", "Alice", "secret", "top-secret")),
                Map.of("config", Map.of("secret", "hidden")),
                Map.of("nested", Map.of("inner", Map.of("secret", "value")))
        );
    }

    private static Stream<Map<String, Object>> provideArraySensitiveInputMaps() {
        return Stream.of(
                Map.of("list", new Object[]{
                        Map.of("token", "abc"),
                        Map.of("password", "1234")
                }),
                Map.of("items", new Object[]{
                        Map.of("secret", "yes"),
                        Map.of("username", "admin")
                })
        );
    }
}