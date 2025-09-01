package com.gcs.app.util;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonReaderUtil {

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .defaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SKIP))
            .build();

    public static <T> T readFromJson(String path, Class<T> clazz) {
        try (InputStream is = readResourceAsStream(path)) {
            return objectMapper.readValue(is, clazz);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read JSON from '%s'", path), e);
        }
    }

    public static <T> T readFromJson(String path, TypeReference<T> typeReference) {
        try (InputStream is = readResourceAsStream(path)) {
            return objectMapper.readValue(is, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read JSON from з '%s': %s", path, e.getMessage()), e);
        }
    }

    private static InputStream readResourceAsStream(String path) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        if (inputStream == null) {
            throw new IllegalArgumentException(String.format("Resource not found: '%s'", path));
        }
        return inputStream;
    }
}
