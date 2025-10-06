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

### What Happens Automatically

When you run integration tests:

- **Testcontainers** automatically starts the required services defined in `docker-compose.yml`, including:
    - 📨 **ActiveMQ** (message broker)
    - 🗄️ **MongoDB** (database for workload)
    - ⚙️ **GCA Core Service** (`gca-core-service`)
    - 🧮 **Workload Service** (`workload-service`)

These services are started in isolated Docker containers **before the tests begin** and are **automatically stopped** after all tests finish.

> ⚙️ You don’t need to run Docker containers manually — **Testcontainers** will handle everything.
