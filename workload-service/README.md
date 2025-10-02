# Workload-Service
![Build](https://github.com/Opanasenko-Mykhailo/gym-crm/actions/workflows/ci.yml/badge.svg?branch=dev)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Opanasenko-Mykhailo_workload-service&metric=coverage)](https://sonarcloud.io/summary/overall?id=Opanasenko-Mykhailo_gym-crm-system)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=Opanasenko-Mykhailo_workload-service&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Opanasenko-Mykhailo_gym-crm-system)
![Java](https://img.shields.io/badge/java-17-blue.svg)
![Last Commit](https://img.shields.io/github/last-commit/Opanasenko-Mykhailo/gym-crm)

Spring Boot microservice that serves as a dedicated service for calculating and tracking trainers' monthly workload.

Receives updates whenever a training session is added or canceled for a trainer, processes the data, and stores monthly summaries in an in-memory database. Handles requests to retrieve trainer workload and returns the calculated data to requesting services.

---

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
JWT_SECRET=gym-crm-secret-key-1234567890XXABCD

# JWT secret for automation-test
JWT_TEST_SECRET=test-secret-key-for-integration-tests
```

> 💡 **Note:** This secret is used only for local development and can be safely committed to the repository.
> For production environments, secrets should be stored in environment variables and **not** committed.
