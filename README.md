# Gym CRM System

A distributed backend system for managing gym members, trainers, trainees, and training sessions. Built as a microservices architecture using Spring Boot, the system handles core gym operations in the main service while tracking trainer workload in a separate microservice communicating via Apache ActiveMQ messaging.

---

## Architecture Overview

```
┌─────────────────────┐     ┌─────────────────┐      ┌──────────────────────────┐
│   gym-main-service  │────▶│ Apache ActiveMQ │────▶│ trainer-workload-service │
│   (port 8080)       │     │   (port 61616)  │      │ (port 8081)              │
└─────────────────────┘     └─────────────────┘      └──────────────────────────┘
          │                                                      │
          │                                                      │
          ▼                                                      ▼
┌─────────────────────┐                            ┌──────────────────────────┐
│   Eureka Server     │                            │        MongoDB           │
│   (port 8761)       │                            │   (port 27017)           │
└─────────────────────┘                            └──────────────────────────┘
```

---

## Tech Stack

### gym-main-service
- Java 17
- Spring Boot 4.0.5
- Spring Security + JWT
- Spring Data JPA + Hibernate
- H2 Database (local profile)
- PostgreSQL (prod profile)
- Apache ActiveMQ (JMS messaging)
- Spring Cloud Netflix Eureka Client
- Spring Boot Actuator + Micrometer + Prometheus
- Springdoc OpenAPI (Swagger UI)
- Lombok, Jackson, Jakarta Validation
- SLF4J + Logback

### trainer-workload-service
- Java 17
- Spring Boot 4.0.5
- Spring Security + JWT
- Spring Data MongoDB
- Apache ActiveMQ (JMS messaging)
- Spring Cloud Netflix Eureka Client
- Lombok, Jackson
- SLF4J + Logback

### eureka-server
- Java 17
- Spring Boot 4.0.5
- Spring Cloud Netflix Eureka Server

### Infrastructure
- Apache ActiveMQ Classic
- MongoDB 8.x

---

## Prerequisites

Make sure the following are installed on your machine:

- Java 17+
- Maven 3.8+
- Apache ActiveMQ Classic — [Download here](https://activemq.apache.org/components/classic/download/)
- MongoDB Community Server — [Download here](https://www.mongodb.com/try/download/community)
- MongoDB Compass (optional, for visual DB inspection) — [Download here](https://www.mongodb.com/try/download/compass)

---

## Running the Application

Follow these steps **in order** to start the full system.

### Step 1 — Start MongoDB

MongoDB should start automatically as a service after installation. Verify it is running:

```bash
mongosh
```

You should see it connect to `mongodb://127.0.0.1:27017`. If not, start it manually:

```bash
# Windows
net start MongoDB

# Mac/Linux
sudo systemctl start mongod
```

### Step 2 — Start ActiveMQ

Navigate to the ActiveMQ installation folder and run:

```bash
# Windows
bin\activemq.bat start

# Mac/Linux
bin/activemq start
```

Verify ActiveMQ is running by opening the web console:
```
http://localhost:8161
Username: admin
Password: admin
```

### Step 3 — Start Eureka Server

```bash
cd eureka-server
mvn spring-boot:run
```

Verify Eureka is running:
```
http://localhost:8761
```

### Step 4 — Start Trainer Workload Service

```bash
cd trainer-workload-service
mvn spring-boot:run
```

Service starts on port `8081`. After startup, verify it appears in Eureka dashboard.

### Step 5 — Start Gym Main Service

```bash
cd gym-main-service
mvn spring-boot:run
```

Service starts on port `8080`. After startup, verify it appears in Eureka dashboard.

---

## Useful Endpoints

### Gym Main Service (port 8080)

| Tool | URL |
|------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |
| Actuator Health | http://localhost:8080/actuator/health |
| Actuator Metrics | http://localhost:8080/actuator/metrics |
| Actuator Prometheus | http://localhost:8080/actuator/prometheus |

#### H2 Console Configuration
```
JDBC URL:  jdbc:h2:mem:gymdb
Username:  sa
Password:  (leave empty)
```

### Trainer Workload Service (port 8081)

| Tool | URL |
|------|-----|
| Get Trainer Workload | http://localhost:8081/api/trainer-workload/{username} |

### Eureka Server (port 8761)

| Tool | URL |
|------|-----|
| Eureka Dashboard | http://localhost:8761 |

### ActiveMQ (port 8161)

| Tool | URL |
|------|-----|
| ActiveMQ Console | http://localhost:8161 |

### MongoDB

| Tool | Connection |
|------|------------|
| MongoDB Compass | mongodb://localhost:27017 |
| Database Name | trainer-workload-db |
| Collection | trainer_workload |

---

## API Overview

### Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

### Key Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/trainee/register | No | Register a new trainee |
| POST | /api/trainer/register | No | Register a new trainer |
| POST | /api/auth/login | No | Login and receive JWT token |
| POST | /api/auth/logout | Yes | Logout and invalidate token |
| PUT | /api/auth/password | Yes | Change password |
| GET | /api/trainee | Yes | Get trainee profile |
| PUT | /api/trainee | Yes | Update trainee profile |
| DELETE | /api/trainee | Yes | Delete trainee |
| GET | /api/trainer | Yes | Get trainer profile |
| PUT | /api/trainer | Yes | Update trainer profile |
| POST | /api/trainings | Yes | Add a training session |
| DELETE | /api/trainings/{id} | Yes | Delete a training session |
| GET | /api/training-types | No | Get all training types |

Full API documentation is available via Swagger UI at `http://localhost:8080/swagger-ui.html`

---

## Messaging

When a training is added or deleted, `gym-main-service` publishes a message to ActiveMQ queue `trainer-workload-queue`. The `trainer-workload-service` consumes this message and updates the trainer's monthly workload summary in MongoDB.

Invalid messages (missing required fields) are automatically routed to the Dead Letter Queue `trainer-workload-dlq`.

---

## Running Tests

```bash
# Run tests for main service
cd gym-main-service
mvn test

# Run tests for workload service
cd trainer-workload-service
mvn test
```

---

## Project Structure

```
gym-crm/
├── gym-main-service/          # Core gym management service
│   └── src/main/java/com/gym/
│       ├── config/            # Security, JMS configuration
│       ├── controller/        # REST controllers
│       ├── dto/               # Request/Response DTOs
│       ├── exception/         # Exception handling
│       ├── filter/            # Transaction logging filter
│       ├── health/            # Custom health indicators
│       ├── mapper/            # Entity mappers
│       ├── messaging/         # JMS message producer
│       ├── metrics/           # Custom Micrometer metrics
│       ├── model/             # JPA entities
│       ├── repository/        # Spring Data JPA repositories
│       ├── security/          # JWT authentication
│       └── service/           # Business logic
├── trainer-workload-service/  # Trainer workload tracking service
│   └── src/main/java/com/workload/
│       ├── config/            # JMS, Security configuration
│       ├── controller/        # REST controllers
│       ├── dto/               # Request DTOs
│       ├── exception/         # Exception handling
│       ├── filter/            # Transaction logging filter
│       ├── messaging/         # JMS message listener
│       ├── model/             # MongoDB documents
│       ├── repository/        # MongoDB repositories
│       └── service/           # Business logic
└── eureka-server/             # Service discovery server
```
