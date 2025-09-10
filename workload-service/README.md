# Workload-Service

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
```

> 💡 **Note:** This secret is used only for local development and can be safely committed to the repository.
> For production environments, secrets should be stored in environment variables and **not** committed.
