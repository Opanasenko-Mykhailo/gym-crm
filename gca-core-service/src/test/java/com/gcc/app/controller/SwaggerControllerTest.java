package com.gcc.app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SwaggerController.class)
@AutoConfigureMockMvc(addFilters = false)
class SwaggerControllerTest extends AbstractControllerTest {

    @Value("${app.swagger.file-name}")
    private String swaggerFileName;

    @Test
    void givenYamlFileExists_whenGetOpenApi_thenReturnsYamlContentWithCorrectContentType() throws Exception {
        ClassPathResource resource = new ClassPathResource(swaggerFileName);
        assertThat(resource.exists()).as("gym.yml must exist in test classpath").isTrue();

        String expectedContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        mockMvc.perform(get(basePath + "/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/x-yaml"))
                .andExpect(content().string(expectedContent));
    }
}