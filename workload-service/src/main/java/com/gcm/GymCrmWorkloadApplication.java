package com.gcm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.gcm.repository")
public class GymCrmWorkloadApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymCrmWorkloadApplication.class, args);
    }

}
