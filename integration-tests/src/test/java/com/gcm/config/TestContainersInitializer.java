package com.gcm.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        DockerComposeContainer<?> environment = new DockerComposeContainer<>(new File("docker-compose.yml"))
                .withOptions("--compatibility")
                .withExposedService("activemq", 61616)
                .withExposedService("mongodb", 27017)
                .withExposedService("gca-core-service", 8081,
                        Wait.forHttp("/gym-crm-core/api/v1/trainees/nobody")
                                .withMethod("GET")
                                .forStatusCode(401)
                                .withStartupTimeout(Duration.ofSeconds(180)))
                .withExposedService("workload-service", 8082)
                .withLocalCompose(true);

        environment.start();
    }
}