# Gateway Service

Spring Cloud Gateway service that serves as the unified entry point and traffic orchestrator for the microservices ecosystem.

Routes incoming requests to appropriate backend services, handles cross-cutting concerns, and provides a centralized layer for API management, security, and monitoring across all registered microservices.

Includes Resilience4j Circuit Breaker to isolate failures, prevent cascading errors, and provide graceful fallbacks when downstream services are unavailable.

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

# Eureka Discovery Server Host
EUREKA_HOST=localhost
```

> 💡 **Note:** This secret is used only for local development and can be safely committed to the repository.
> For production environments, secrets should be stored in environment variables and **not** committed.
