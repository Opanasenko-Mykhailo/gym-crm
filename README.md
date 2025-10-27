# Gym CRM - Installation and Setup Guide

Gym CRM System is a microservices-based application for managing gyms, trainers, trainees, and workouts.  
Built with **Java 17**, **Spring Boot**, **PostgreSQL**, **MongoDB**, **ActiveMQ**, and **Resilience4j**.

---

## 📋 Table of Contents

1. [Services Overview](#-services-overview)
2. [Prerequisites](#-prerequisites)
3. [Initial Setup](#-initial-setup)
4. [Option A: Docker Compose Deployment (Recommended)](#-option-a-docker-compose-deployment-recommended)
5. [Option B: Local Development Setup](#-option-b-local-development-setup)
6. [Accessing Services](#-accessing-services)
7. [Integration Tests](#-integration-tests)
8. [Configuration Details](#-configuration-details)

---

## 🏗️ Services Overview

| Service            | Port | Description                                                | Database   |
|--------------------|------|------------------------------------------------------------|------------|
| `discovery-server` | 8761 | Eureka Server for service discovery                        | -          |
| `gateway-service`  | 8080 | API Gateway with routing and circuit breakers              | -          |
| `gca-core-service` | 8081 | CRUD operations, authentication, JWT, Liquibase migrations | PostgreSQL |
| `workload-service` | 8082 | Trainer workload management via JMS and REST               | MongoDB    |

---

## 📦 Prerequisites

### For Docker Deployment
- **Docker** (version 20.10+)
- **Docker Compose** (version 2.0+)
- **Git**

### For Local Development
- **Java Development Kit (JDK) 17**
- **Maven**
- **PostgreSQL 13+** (for `gca-core-service`)
- **MongoDB 6+** (for `workload-service`)
- **Apache ActiveMQ** (for message queuing between services)
- **Git**

---

## 🚀 Initial Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/Opanasenko-Mykhailo/gym-crm.git
cd gym-crm
```

### Step 2: Create Environment Files

Create `.env` files for each microservice with the following content:

#### `gca-core-service/.env`

```env
# JWT secret for local development
JWT_SECRET=gym-crm-secret-key-1234567890XXABCD

# JWT secret for automation-test
JWT_TEST_SECRET=test-secret-key-for-integration-tests

# Eureka Discovery Server Host
EUREKA_HOST=localhost

# Database
DB_HOST=localhost
DB_PORT=5432
DB_USER=gcs
DB_PASSWORD=gcs

# Message broker
ACTIVEMQ_HOST=localhost
ACTIVEMQ_PORT=61616
ACTIVEMQ_USER=gca
ACTIVEMQ_PASSWORD=gca
```

#### `gateway-service/.env`

```env
# JWT secret for local development
JWT_SECRET=gym-crm-secret-key-1234567890XXABCD

# Eureka Discovery Server Host
EUREKA_HOST=localhost
```

#### `workload-service/.env`

```env
# JWT secret for local development
JWT_SECRET=gym-crm-secret-key-1234567890XXABCD

# JWT secret for automation-test
JWT_TEST_SECRET=test-secret-key-for-integration-tests

# Eureka Discovery Server Host
EUREKA_HOST=localhost

# Database
MONGO_HOST=localhost
MONGO_PORT=27017
MONGO_USER=gym
MONGO_PASSWORD=gym

# Message broker
ACTIVEMQ_HOST=localhost
ACTIVEMQ_PORT=61616
ACTIVEMQ_USER=gca
ACTIVEMQ_PASSWORD=gca
```

#### `integration-tests/.env`

```env
# JWT secret for integration tests
JWT_SECRET=test-secret-key-for-integration-tests
```

> 💡 **Note:** These environment files work for both Docker Compose and local development. Docker Compose will override the host values automatically (e.g., `localhost` becomes `postgres`, `mongodb`, `activemq`), while local development uses the values as-is.

> 💡 **Security Note:** These secrets are for local development only. For production, use secure environment variables and never commit secrets to the repository.

---

## 🐳 Option A: Docker Compose Deployment (Recommended)

The easiest way to run the entire application is using Docker Compose.

### Step 1: Build and Start All Services

```bash
# Build and start all services in detached mode
docker compose up -d --build

# Or without rebuild (if images already exist)
docker compose up -d
```

### Step 2: Verify Services are Running

```bash
# Check status of all containers
docker compose ps

# View logs from all services
docker compose logs -f

# View logs from specific service
docker compose logs -f gca-core-service
```

### Step 3: Stop the Application

```bash
# Stop all services
docker compose down

# Stop and remove all volumes (databases will be cleared)
docker compose down -v
```

### Docker Compose Services Architecture

The `docker-compose.yml` includes the following services:

| Service            | Image/Build         | Dependencies                              | Health Check |
|--------------------|---------------------|-------------------------------------------|--------------|
| `discovery-server` | Custom (Dockerfile) | None                                      | ✅            |
| `gateway-service`  | Custom (Dockerfile) | discovery-server                          | ✅            |
| `gca-core-service` | Custom (Dockerfile) | discovery-server, postgres, activemq      | ✅            |
| `workload-service` | Custom (Dockerfile) | discovery-server, mongodb, activemq       | ✅            |
| `postgres`         | postgres:13         | None                                      | ✅            |
| `mongodb`          | mongo:6             | None                                      | ✅            |
| `activemq`         | activemq-classic    | None                                      | ✅            |

**Service Startup Order:**
1. Infrastructure Services (postgres, mongodb, activemq) start first
2. Discovery Server (Eureka) starts after infrastructure is healthy
3. Application Services (gca-core-service, workload-service, gateway-service) start after all dependencies are healthy

---

## 💻 Option B: Local Development Setup

### Step 1: Build the Project

```bash
mvn clean install
```

### Step 2: Setup PostgreSQL Database

#### Install PostgreSQL

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# macOS (using Homebrew)
brew install postgresql

# Windows 
Download from https://www.postgresql.org/download/windows/
```

#### Create Database and User

```sql
-- Connect to PostgreSQL as superuser
sudo -u postgres psql

-- Create database and user
CREATE DATABASE "gym_db";
CREATE USER gcs WITH PASSWORD 'gcs';
GRANT ALL PRIVILEGES ON DATABASE "gym_db" TO gcs;

-- Exit
```

### Step 3: Setup MongoDB

#### Option A: Install Locally

**macOS (Homebrew)**
```bash
brew tap mongodb/brew
brew install mongodb-community@6.0
brew services start mongodb-community@6.0
```

**Ubuntu/Debian**
```bash
sudo apt update
sudo apt install -y mongodb
sudo systemctl start mongodb
sudo systemctl enable mongodb
```

**Windows**
- Download the [MongoDB MSI Installer](https://www.mongodb.com/try/download/community)
- Install and select MongoDB as a Service during setup
- MongoDB will start automatically on port 27017

#### Option B: Run with Docker

```bash
docker run -d \
  --name mongo \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=gym \
  -e MONGO_INITDB_ROOT_PASSWORD=gym \
  -e MONGO_INITDB_DATABASE=workloaddb \
  mongo:6.0
```

### Step 4: Setup Apache ActiveMQ

#### Option A: Download and Run Locally

1. Download Apache ActiveMQ from [official website](https://activemq.apache.org/components/classic/download/)
2. Extract the archive to your preferred directory
3. Navigate to the ActiveMQ directory and start the broker:
   ```bash
   # Windows
   bin\activemq.bat start
   
   # Linux/macOS
   bin/activemq start
   ```
4. ActiveMQ will start on default port `61616` (broker) and `8161` (web console)
5. Access the web console at `http://localhost:8161/admin` (default credentials: admin/admin)

#### Option B: Using Docker

```bash
docker run -d \
  --name activemq \
  -p 61616:61616 \
  -p 8161:8161 \
  -e ACTIVEMQ_ADMIN_LOGIN=gca \
  -e ACTIVEMQ_ADMIN_PASSWORD=gca \
  apache/activemq-classic:latest
```

#### Configure ActiveMQ Users (if needed)

If you need to set up custom users (gca/gca), edit the ActiveMQ configuration:

1. Navigate to `conf/users.properties` and add:
   ```properties
   gca=gca
   ```
2. Navigate to `conf/groups.properties` and add:
   ```properties
   admins=admin,gca
   ```

### Step 5: Start Services in Correct Order

⚠️ **Important:** Services must be started in the following order:

#### 1. Start Infrastructure Services

```bash
# Start PostgreSQL (if not already running)
# Ubuntu/Debian
sudo systemctl start postgresql

# macOS
brew services start postgresql

# Windows - use Services panel or PostgreSQL service
```

```bash
# Start MongoDB (if not already running)
# Ubuntu/Debian
sudo systemctl start mongodb

# macOS (Homebrew)
brew services start mongodb-community@6.0

# Windows - MongoDB service should start automatically
```

ActiveMQ should already be running from Step 4.

#### 2. Start Discovery Server (MUST BE FIRST)

```bash
cd discovery-server
mvn spring-boot:run
```

Wait until you see: `Started DiscoveryServerApplication`  
Access at: `http://localhost:8761`

#### 3. Start Core Service

```bash
cd gca-core-service
mvn spring-boot:run
```

- Database migrations will run automatically via Liquibase
- Service will register with Eureka
- Access at: `http://localhost:8081`

#### 4. Start Workload Service

```bash
cd workload-service
mvn spring-boot:run
```

- Uses MongoDB database
- Service will register with Eureka
- Access at: `http://localhost:8082`

#### 5. Start Gateway Service (MUST BE LAST)

```bash
cd gateway-service
mvn spring-boot:run
```

- API Gateway available at: `http://localhost:8080`
- Routes requests to registered services

---

## 🌐 Accessing Services

Once all services are running, you can access:

| Service                | URL                          | Credentials (if needed) |
|------------------------|------------------------------|-------------------------|
| Eureka Dashboard       | http://localhost:8761        | -                       |
| API Gateway            | http://localhost:8080        | -                       |
| Core Service (Direct)  | http://localhost:8081        | -                       |
| Workload Service       | http://localhost:8082        | -                       |
| ActiveMQ Console       | http://localhost:8161        | `gca` / `gca`           |
| PostgreSQL             | `localhost:5432`             | `gcs` / `gcs`           |
| MongoDB                | `localhost:27017`            | `gym` / `gym`           |

### API Routes (via Gateway)

| Route Path             | Target Service   | Description                         |
|------------------------|------------------|-------------------------------------|
| `/gym-crm-core/api/**` | gca-core-service | Core business logic, authentication |
| `/api/workload/**`     | workload-service | Trainer workload management         |

---

## 🧪 Integration Tests

The project includes comprehensive **Cucumber-based integration tests** using **Testcontainers**.

### Prerequisites for Testing

- **Docker** must be running on your machine
- `.env` file configured in `integration-tests` directory (see Initial Setup section)

### Running Tests

```bash
cd integration-tests
mvn test
```

### What Happens Automatically

When you run integration tests:

- **Testcontainers** automatically starts all required services in Docker containers:
   - 📨 **ActiveMQ** (message broker)
   - 🗄️ **MongoDB** (database for workload)
   - ⚙️ **GCA Core Service** (business logic)
   - 🧮 **Workload Service** (workload management)

- Services are started **before tests begin** and **automatically stopped** after all tests finish
- Test data is isolated in containers and cleaned up automatically

> ⚙️ You don't need to run Docker containers manually — **Testcontainers** handles everything.

---

## ⚙️ Configuration Details

### Database Configuration

#### PostgreSQL (gca-core-service)
- **URL**: `jdbc:postgresql://localhost:5432/gym_db`
- **Username**: `gcs`
- **Password**: `gcs`
- **Migrations**: Liquibase (automatic on startup)

#### MongoDB (workload-service)
- **URL**: `mongodb://gym:gym@localhost:27017/workloaddb`
- **Username**: `gym`
- **Password**: `gym`
- **Database**: `workloaddb`

### ActiveMQ Configuration

- **Broker URL**: `tcp://localhost:61616`
- **Username**: `gca`
- **Password**: `gca`
- **Queue Name**: `trainer.workload.queue`
- **Request Timeout**: 5000ms

### Security Features

- **JWT Authentication** with refresh tokens
- **Brute Force Protection**: Max 3 attempts, 5-minute lockout
- **CORS**: Configured for all origins (customize for production)
- **Circuit Breakers**: Resilience4j for fault tolerance

---

## 🔧 Docker Build Commands (Advanced)

If you need to build individual Docker images:

```bash
# Discovery Server
docker build -t discovery-server:latest -f discovery-server/Dockerfile .

# Gateway Service
docker build -t gateway-service:latest -f gateway-service/Dockerfile .

# GCA Core Service
docker build -t gca-core-service:latest -f gca-core-service/Dockerfile .

# Workload Service
docker build -t workload-service:latest -f workload-service/Dockerfile .
```

---

## 📝 Notes

- For **production environments**, use secure secrets and store them in environment variables, not in `.env` files
- The startup order is critical for local development - always start Discovery Server first
- Docker Compose handles service dependencies automatically
- All services include health checks to ensure proper startup sequence

---

## 🆘 Troubleshooting

### Services won't start
- Check if all prerequisites are installed
- Verify databases (PostgreSQL/MongoDB) are running
- Ensure ActiveMQ is accessible
- Check logs: `docker compose logs -f` or individual service logs

### Port conflicts
- Verify ports 8080, 8081, 8082, 8761, 5432, 27017, 61616, 8161 are available
- Stop conflicting services or modify port mappings in configuration

### Database connection issues
- Verify database credentials match `.env` files
- Check database services are running
- Test connection manually using database clients

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Apache ActiveMQ Classic](https://activemq.apache.org/components/classic/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [MongoDB Documentation](https://www.mongodb.com/docs/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)