package com.gcc.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("${app.api.base-path}")
public class SwaggerController {

    @Value("${app.swagger.file-name:gym.yml}")
    private String swaggerFileName;

    @GetMapping(value = "/openapi", produces = "application/x-yaml")
    public ResponseEntity<String> getSwaggerYaml() throws IOException {
        ClassPathResource yamlFile = new ClassPathResource(swaggerFileName);

        if (!yamlFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        try (InputStream inputStream = yamlFile.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/x-yaml"))
                    .body(content);
        }
    }
}