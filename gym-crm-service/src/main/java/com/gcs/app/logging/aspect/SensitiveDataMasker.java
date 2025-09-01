package com.gcs.app.logging.aspect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SensitiveDataMasker {

    private static final Set<String> SENSITIVE_KEYWORDS = Set.of("password", "token", "secret");
    private static final String MASKED_VALUE = "********";

    private final ObjectMapper objectMapper;

    private final Map<Class<?>, Function<Object, String>> maskingHandlers = Map.of(
            ResponseEntity.class, this::maskResponseEntity
    );

    public String maskArguments(Object[] args) {
        return Arrays.stream(args)
                .map(this::mask)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String mask(Object data) {
        if (data == null) {
            return "";
        }
        return maskingHandlers.entrySet().stream()
                .filter(entry -> entry.getKey().isInstance(data))
                .findFirst()
                .map(entry -> entry.getValue().apply(data))
                .orElseGet(() -> maskAsJson(data));
    }

    private String maskAsJson(Object data) {
        try {
            JsonNode root = objectMapper.valueToTree(data);

            if (root instanceof ObjectNode objectNode) {
                maskSensitiveFields(objectNode);
            }

            return objectMapper.writeValueAsString(root);

        } catch (Exception e) {
            return data.toString();
        }
    }

    private String maskResponseEntity(Object object) {
        ResponseEntity<?> response = (ResponseEntity<?>) object;
        String maskedBody = mask(response.getBody());

        return String.format("ResponseEntity{status=%s, body=%s}", response.getStatusCode(), maskedBody);
    }

    private void maskSensitiveFields(ObjectNode node) {
        node.fieldNames().forEachRemaining(fieldName -> {
            JsonNode value = node.get(fieldName);
            if (isSensitiveField(fieldName)) {
                node.put(fieldName, MASKED_VALUE);
            } else if (value.isObject()) {
                maskSensitiveFields((ObjectNode) value);
            } else if (value.isArray()) {
                value.forEach(element -> {
                    if (element.isObject()) {
                        maskSensitiveFields((ObjectNode) element);
                    }
                });
            }
        });
    }

    private boolean isSensitiveField(String fieldName) {
        return SENSITIVE_KEYWORDS.stream().anyMatch(fieldName.toLowerCase()::contains);
    }
}