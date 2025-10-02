# Integration Tests

Cucumber-based integration tests for GCA microservices.

## Running Workload Service for Integration Tests

Integration tests require the Workload Service to be running under the `test` profile. You can start it using Maven:

```bash
cd workload-service
mvn spring-boot:run -Dspring-boot.run.profiles=test