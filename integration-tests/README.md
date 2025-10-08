# Integration Tests

Cucumber-based integration tests for GCA microservices.

## Local Development Setup

For local development, you need to create a `.env` file in the root of the microservice and add the JWT secret.

### Steps

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

### What Happens Automatically

When you run integration tests:

- **Testcontainers** automatically starts the required services defined in `docker-compose.yml`, including:
  - 📨 **ActiveMQ** (message broker)
  - 🗄️ **MongoDB** (database for workload)
  - ⚙️ **GCA Core Service** (`gca-core-service`)
  - 🧮 **Workload Service** (`workload-service`)

These services are started in isolated Docker containers **before the tests begin** and are **automatically stopped** after all tests finish.

> ⚙️ You don't need to run Docker containers manually — **Testcontainers** will handle everything.

## Running Tests

### Run All Tests

To run all integration tests (both positive and negative scenarios):

```bash
mvn clean test
```

or

```bash
./mvnw clean test
```

### Run Only Positive Test Cases

To run only tests tagged with `@PositiveCase`:

```bash
mvn clean test -Dcucumber.filter.tags="@PositiveCase"
```

### Run Only Negative Test Cases

To run only tests tagged with `@NegativeCase`:

```bash
mvn clean test -Dcucumber.filter.tags="@NegativeCase"
```

## Test Reports

After running tests, Cucumber generates reports in:

```
target/cucumber-reports/
```

You can view the HTML report by opening:

```
target/cucumber-reports/cucumber-report.html
```

## Troubleshooting

### Tests Fail to Start

If tests fail to start, ensure:
- Docker is running on your machine
- Port conflicts are resolved (check if ports used by services are available)
- `.env` file is properly configured with `JWT_SECRET`