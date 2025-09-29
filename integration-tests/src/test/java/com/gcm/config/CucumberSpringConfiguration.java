package com.gcm.config;

import com.gcm.CucumberIntegrationApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = CucumberIntegrationApplication.class)
public class CucumberSpringConfiguration {
}