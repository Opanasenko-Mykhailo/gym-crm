package com.gcm.config;

import com.gcm.CucumberIntegrationApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.activemq.ActiveMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

@CucumberContextConfiguration
@SpringBootTest(classes = CucumberIntegrationApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

    private static final int ACTIVEMQ_PORT = 61616;

    private static final ActiveMQContainer ACTIVE_MQ_CONTAINER =
            new ActiveMQContainer(
                    DockerImageName.parse("rmohr/activemq:latest")
                            .asCompatibleSubstituteFor("apache/activemq-classic"))
                    .withExposedPorts(ACTIVEMQ_PORT)
                    .withReuse(true);

    static {
        ACTIVE_MQ_CONTAINER.setPortBindings(List.of(ACTIVEMQ_PORT + ":" + ACTIVEMQ_PORT));
        ACTIVE_MQ_CONTAINER.start();
    }

    @DynamicPropertySource
    static void registerActiveMqProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.activemq.broker-url", () -> "tcp://localhost:" + ACTIVEMQ_PORT);
    }
}