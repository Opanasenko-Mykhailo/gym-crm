# Integration Tests

Cucumber-based integration tests for GCA microservices.

# Local Development Setup

For local development, you need to create a `.env` file in the root of the microservice and add the JWT secret.

## Steps

1. In the root of the project, create the `.env` file:

```bash
touch .env
```

2. Add the following inside `.env`:

```env
# JWT secret for local development
JWT_SECRET=test-secret-key-for-integration-tests
```

> 💡 **Note:** This secret is used only for local development and can be safely committed to the repository.
> For production environments, secrets should be stored in environment variables and **not** committed.


## Running Services for Integration Tests

Integration tests require both the GCA Service and Workload Service to be running under the `automation-test` profile.

### Running GCA Service

```bash
cd gca-core-service
mvn spring-boot:run -Dspring-boot.run.profiles=automation-test
```

### Running Workload Service

```bash
cd workload-service
mvn spring-boot:run -Dspring-boot.run.profiles=automation-test
```