# Gym CRM

Gym CRM System is a microservices-based application for managing gyms, trainers, trainees, and workouts.  
The system is built with **Java 17**, **Spring Boot**, **PostgreSQL**, **MongoDB**, **ActiveMQ**, and **Resilience4j**.

---

## Services

| Service            | Port | Description                                                | Database   |
|--------------------|------|------------------------------------------------------------|------------|
| `discovery-server` | 8761 | Eureka Server for service discovery                        | -          |
| `gateway-service`  | 8080 | API Gateway with routing and circuit breakers              | -          |
| `gca-core-service` | 8081 | CRUD operations, authentication, JWT, Liquibase migrations | PostgreSQL |
| `workload-service` | 8082 | Trainer workload management via JMS and REST               | MongoDB    |

---

## Prerequisites

- **Java Development Kit (JDK) 17**
- **Maven**
- **PostgreSQL 13+** (for `gca-core-service`)
- **MongoDB 6+** (for `workload-service`)
- **Apache ActiveMQ** (for message queuing between services)
- **Docker**
- **Git**

---

## Setup and Installation

### 1. Clone & Build

```bash
git clone https://github.com/Opanasenko-Mykhailo/gym-crm.git
cd gym-crm
mvn clean install
```

### 2. Setup PostgreSQL Database

#### Install PostgreSQL

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# macOS (using Homebrew)
brew install postgresql

# Windows - Download from https://www.postgresql.org/download/windows/
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
\
q
```

# 3. MongoDB Setup (for workload-service)

The `workload-service` uses **MongoDB** to store trainer workload summaries.

---

## Option A: Install Locally

### macOS (Homebrew)

```bash
brew tap mongodb/brew
brew install mongodb-community@6.0
brew services start mongodb-community@6.0
```

### Ubuntu/Debian

```bash
sudo apt update
sudo apt install -y mongodb
sudo systemctl start mongodb
sudo systemctl enable mongodb
```

### Windows

Download the [MongoDB MSI Installer](https://www.mongodb.com/try/download/community)

Install and select MongoDB as a Service during setup.

MongoDB will start automatically on port 27017.

---

### Option B: Run with Docker

```bash
docker run -d \
  --name mongo \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=gym \
  -e MONGO_INITDB_ROOT_PASSWORD=gym \
  -e MONGO_INITDB_DATABASE=workloaddb \
  mongo:6.0
```

### 4. Install and Start Apache ActiveMQ

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
# Run ActiveMQ in Docker container
docker run -d \
  --name activemq \
  -p 61616:61616 \
  -p 8161:8161 \
  -e ACTIVEMQ_ADMIN_LOGIN=gca \
  -e ACTIVEMQ_ADMIN_PASSWORD=gca \
  apache/activemq-classic:latest
```

### Configure ActiveMQ Users (if needed)

If you need to set up custom users (gca/gca as shown in config), edit the ActiveMQ configuration:

1. Navigate to `conf/users.properties` and add:
   ```properties
   gca=gca
   ```
2. Navigate to `conf/groups.properties` and add:
   ```properties
   admins=admin,gca
   ```

### 5. Start the Services

#### Service Startup Order (Important!)

1. **Start PostgreSQL** (if not already running):
   ```bash
   # Ubuntu/Debian
   sudo systemctl start postgresql
   
   # macOS
   brew services start postgresql
   
   # Windows - use Services panel or PostgreSQL service
   ```
2. **Start MongoDB** (if not already running):

   ```bash
   # Ubuntu/Debian
   sudo systemctl start mongodb
   
   # macOS (Homebrew)
   brew services start mongodb-community@6.0

   # Windows - MongoDB service should start automatically
   ```

3. **Start ActiveMQ** (see step 4 above)

4. **Start Discovery Server** (Eureka Server) - **MUST BE FIRST**:
   ```bash
   cd discovery-server
   mvn spring-boot:run
   ```
   Access at: `http://localhost:8761`

5. **Start Core Service**:
   ```bash
   cd gca-core-service
   mvn spring-boot:run
   ```
    - Database migrations will run automatically via Liquibase
    - Service will register with Eureka
    - Access at: `http://localhost:8081`

6. **Start Workload Service**:
   ```bash
   cd workload-service
   mvn spring-boot:run
   ```
    - Uses MongoDB database
    - Service will register with Eureka

7. **Start Gateway Service** - **MUST BE LAST**:
   ```bash
   cd gateway-service
   mvn spring-boot:run
   ```
    - API Gateway available at: `http://localhost:8080`
    - Routes requests to registered services

---

## Docker Build for Microservices

You can build all the microservices using Docker with the following commands:

### Discovery Server

```bash
docker build -t discovery-server:latest -f discovery-server/Dockerfile .
```

### Gateway Service

```bash
docker build -t gateway-service:latest -f gateway-service/Dockerfile .
```

### GCA Core Service

```bash
docker build -t gca-core-service:latest -f gca-core-service/Dockerfile .
```

### Workload Service

```bash
docker build -t workload-service:latest -f workload-service/Dockerfile .
```

## Configuration

### Local Development Setup

For local development, you need to create `.env` files in each microservice directory:

#### gca-core-service/.env

```env
# JWT secret for local development  
JWT_SECRET=gym-crm-secret-key-1234567890XXABCD
```

#### gateway-service/.env

```env
# JWT secret for local development (must match core service)
JWT_SECRET=gym-crm-secret-key-1234567890XXABCD
```

#### workload-service/.env

```env
# JWT secret for local development (must match core service)
JWT_SECRET=gym-crm-secret-key-1234567890XXABCD
```

#### integration-tests/.env

```env
# JWT secret for integration tests
JWT_SECRET=test-secret-key-for-integration-tests
```

> 💡 **Note:** This secret is used only for local development and can be safely committed to the repository.
> For production environments, secrets should be stored in environment variables and **not** committed.

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
- **Database**: workloaddb

### ActiveMQ Configuration

The system uses the following ActiveMQ settings:

- **Broker URL**: `tcp://localhost:61616`
- **Username**: `gca`
- **Password**: `gca`
- **Queue Name**: `trainer.workload.queue`
- **Request Timeout**: 5000ms

### API Routes (via Gateway)

The gateway service routes requests to the appropriate microservices:

| Route Path             | Target Service   | Description                         |
|------------------------|------------------|-------------------------------------|
| `/gym-crm-core/api/**` | gca-core-service | Core business logic, authentication |
| `/api/workload/**`     | workload-service | Trainer workload management         |

### Security Features

- **JWT Authentication** with refresh tokens
- **Brute Force Protection**: Max 3 attempts, 5-minute lockout
- **CORS**: Configured for all origins (customize for production)
- **Circuit Breakers**: Resilience4j for fault tolerance

---

## Integration Tests

The project includes comprehensive **Cucumber-based integration tests** using **Testcontainers**.

### Prerequisites for Testing

- **Docker** must be running on your machine
- `.env` file configured in `integration-tests` directory (see Configuration section above)

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