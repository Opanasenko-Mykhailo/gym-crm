# Gym CRM Core
![Build](https://github.com/Opanasenko-Mykhailo/gym-crm-system/actions/workflows/ci.yml/badge.svg?branch=dev)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Opanasenko-Mykhailo_gym-crm-system&metric=coverage)](https://sonarcloud.io/summary/overall?id=Opanasenko-Mykhailo_gym-crm-system)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=Opanasenko-Mykhailo_gym-crm-system&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Opanasenko-Mykhailo_gym-crm-system)
![Java](https://img.shields.io/badge/java-17-blue.svg)
![Last Commit](https://img.shields.io/github/last-commit/Opanasenko-Mykhailo/gym-crm-system)

## Prerequisites

To run this application, you should have the following installed:

- **Java Development Kit (JDK) 17**
- **Maven**
- **PostgreSQL** 13+
- **Git**

---

## Setup Instructions

Run the following SQL script to create the database and user:

```sql
CREATE DATABASE "gym_db";
CREATE USER gcs WITH PASSWORD 'gcs';
GRANT ALL PRIVILEGES ON DATABASE "gym_db" TO gcs;
```

---

## Clone & Build

```bash
git clone https://github.com/Opanasenko-Mykhailo/gym-crm-system.git
cd gym-crm-system
mvn clean install
```

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
```

> 💡 **Note:** This secret is used only for local development and can be safely committed to the repository.
> For production environments, secrets should be stored in environment variables and **not** committed.

---
## Running the Application

Use the following command to run the application with the **local** profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## Profiles

The application supports the following Spring profiles:

| Profile | Purpose           | Description                                 |
|---------|-------------------|---------------------------------------------|
| local   | Developer machine | Uses test credentials and full SQL logging  |
| dev     | Development env   | Less verbose logging                        |
| stg     | Staging           | Simulates prod logging                      |
| prod    | Production        | Minimal logging, safe defaults              |

> All profiles use the **same PostgreSQL database**: `gym_db` with user `gcs`.

---

## Monitoring & Metrics

The application exposes monitoring endpoints via Spring Boot Actuator:

- Health check:
  [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

- Prometheus metrics:
  [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)

You can use these endpoints for health monitoring and integrating with Prometheus.

---

## API Documentation

- Swagger UI (auto-generated from controllers):  
  [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

- Swagger UI (generated from `gym.yml` file):  
  [http://localhost:8080/gym-docs.html](http://localhost:8080/gym-docs.html)

---

## Postman Collection

To quickly test the available API endpoints:

1. Open **Postman**.
2. Click on the **"Import"** button (top left).
3. Select the **"File"** tab.
4. Choose the file:  
   `src/main/resources/postman/GCA-API-Collection.json`
5. Click **"Open"** to import.

The collection includes predefined requests for authentication, trainers, trainees, and training operations.  
You can modify the environment variables or headers as needed for your local setup.