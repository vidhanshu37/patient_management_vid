# 🏥 Patient Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![gRPC](https://img.shields.io/badge/gRPC-244c5a?style=for-the-badge&logo=google&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white)

**A production-grade, cloud-native Healthcare Patient Management System built on a microservices architecture — leveraging Spring Boot, Apache Kafka, gRPC, Docker, and AWS CDK with ECS Fargate.**

[View Repository](https://github.com/vidhanshu37/patient_management_vid) · [Report Bug](https://github.com/vidhanshu37/patient_management_vid/issues) · [Request Feature](https://github.com/vidhanshu37/patient_management_vid/issues)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Setup & Installation](#-setup--installation)
- [Environment Variables](#-environment-variables)
- [API Endpoints](#-api-endpoints)
- [Kafka Events](#-kafka-events)
- [gRPC Communication](#-grpc-communication)
- [Infrastructure (AWS CDK)](#-infrastructure-aws-cdk)
- [Deployment](#-deployment)
- [Troubleshooting](#-troubleshooting)
- [Future Improvements](#-future-improvements)
- [Contributing](#-contributing)
- [License](#-license)
- [Author](#-author)

---

## 🔍 Overview

The **Patient Management System** is a fully distributed, event-driven healthcare backend platform designed for real-world scalability and cloud deployment. The system is composed of five independently deployable microservices — each with its own database — communicating asynchronously via **Apache Kafka** and synchronously via **gRPC**.

All external traffic is routed through a central **API Gateway** which validates **JWT tokens** issued by a dedicated **Auth Service**. The infrastructure is fully codified using **AWS CDK**, deploying services as Docker containers on **ECS Fargate** with **MSK (Managed Kafka)** in production, and **LocalStack** for local AWS simulation.

> Built to demonstrate senior-level proficiency in distributed systems, cloud infrastructure, inter-service communication patterns, and enterprise Java development.

---

## ✨ Features

- 🔐 **JWT Authentication** — Stateless token-based auth handled at the API Gateway level; tokens validated before requests reach downstream services
- 🧑‍⚕️ **Patient CRUD Operations** — Full lifecycle management of patient records including registration, updates, retrieval, and deletion
- 💳 **Billing Integration** — Automated billing account creation triggered synchronously via gRPC whenever a new patient is registered
- 📊 **Event-Driven Analytics** — Patient lifecycle events published to Kafka topics, consumed asynchronously by the Analytics Service for downstream processing
- 🌐 **Centralized API Gateway** — Single ingress point for all client requests with JWT validation and intelligent routing to upstream services
- 🐳 **Fully Dockerized** — Each service ships as a Docker image; the entire stack is orchestrated locally via Docker Compose
- ☁️ **AWS CDK Infrastructure** — Production cloud deployment codified in TypeScript using AWS CDK — ECS Fargate, MSK, VPC, security groups, and load balancers all as code
- 🧪 **Integration Tests** — Dedicated integration-tests module for end-to-end API validation against a running stack
- 🏗️ **LocalStack Support** — Full AWS service emulation locally (MSK, ECS) using LocalStack, enabling cloud-parity development without an AWS account
- 📡 **gRPC Inter-Service Communication** — High-performance, strongly-typed binary RPC between Patient Service and Billing Service using Protocol Buffers

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x |
| **API Gateway** | Spring Cloud Gateway |
| **Messaging** | Apache Kafka / AWS MSK |
| **RPC** | gRPC + Protocol Buffers |
| **Database** | PostgreSQL (per-service) |
| **Auth** | JWT (JSON Web Tokens) |
| **Containerization** | Docker, Docker Compose |
| **Cloud Provider** | AWS (ECS Fargate, MSK, VPC, ALB) |
| **IaC** | AWS CDK |
| **Local AWS Emulation** | LocalStack |
| **Build Tool** | Maven |
| **IDE** | IntelliJ IDEA |
| **API Testing** | HTTP request files (`.http`) |

---

## 🏗️ Architecture

### System Architecture Diagram


```mermaid
graph TB
    Client(["🌐 Client<br/>(Browser / Mobile / Postman)"])

    subgraph Gateway["API Gateway Layer"]
        GW["🔀 API Gateway<br/>(Spring Cloud Gateway)<br/>:4004"]
    end

    subgraph Auth["Auth Service"]
        AS["🔐 Auth Service<br/>(Spring Boot)<br/>:4005"]
        ASDB[(PostgreSQL<br/>auth_db)]
    end

    subgraph Patients["Patient Service"]
        PS["🧑‍⚕️ Patient Service<br/>(Spring Boot)<br/>:4000"]
        PSDB[(PostgreSQL<br/>patient_db)]
    end

    subgraph Billing["Billing Service"]
        BS["💳 Billing Service<br/>(Spring Boot + gRPC)<br/>:4001 / :9001"]
        BSDB[(PostgreSQL<br/>billing_db)]
    end

    subgraph Analytics["Analytics Service"]
        ANS["📊 Analytics Service<br/>(Spring Boot)<br/>:4002"]
    end

    subgraph Messaging["Event Streaming"]
        KAFKA[["Apache Kafka / AWS MSK<br/>patient.events topic"]]
    end

    Client -->|"HTTP Request"| GW
    GW -->|"Validate JWT"| AS
    AS --- ASDB
    GW -->|"Route (JWT valid)"| PS
    GW -->|"Route (JWT valid)"| BS
    PS --- PSDB
    BS --- BSDB
    PS -->|"gRPC CreateBillingAccount"| BS
    PS -->|"Publish PatientCreated / PatientUpdated"| KAFKA
    KAFKA -->|"Consume Events"| ANS
```

### Service Communication Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant GW as API Gateway
    participant AUTH as Auth Service
    participant PS as Patient Service
    participant BS as Billing Service
    participant KAFKA as Apache Kafka
    participant ANS as Analytics Service

    C->>GW: POST /api/patients (+ JWT)
    GW->>AUTH: Validate JWT Token
    AUTH-->>GW: Token Valid ✅
    GW->>PS: Forward Request
    PS->>PS: Persist Patient to DB
    PS->>BS: gRPC: CreateBillingAccount(patientId, name, email)
    BS-->>PS: BillingAccountResponse ✅
    PS->>KAFKA: Publish: patient.events (PatientCreated)
    KAFKA->>ANS: Consume: PatientCreated Event
    ANS->>ANS: Process & Store Analytics
    PS-->>GW: Patient Created Response
    GW-->>C: 201 Created
```

### Deployment Architecture

```mermaid
graph TB
    subgraph AWS["☁️ AWS Cloud"]
        subgraph VPC["Virtual Private Cloud (VPC)"]
            ALB["Application Load Balancer"]

            subgraph ECS["ECS Fargate Cluster"]
                GW_T["API Gateway Task"]
                AUTH_T["Auth Service Task"]
                PS_T["Patient Service Task"]
                BS_T["Billing Service Task"]
                ANS_T["Analytics Service Task"]
            end

            MSK["Amazon MSK<br/>(Managed Kafka)"]
            RDS["Amazon RDS PostgreSQL<br/>(per service)"]
        end
    end

    subgraph LOCAL["💻 Local Development"]
        DC["Docker Compose"]
        LS["LocalStack<br/>(AWS Emulation)"]
        CDK_LOCAL["CDK Local Deploy"]
    end

    ALB --> ECS
    ECS --> MSK
    ECS --> RDS
    CDK_LOCAL --> LS
    DC --> LS
```

---

## 📁 Project Structure

```
patient_management_vid/
│
├── api_gateway/                  # Spring Cloud Gateway — JWT validation & routing
│   ├── src/main/java/
│   │   └── com/pm/apigateway/
│   │       ├── filter/           # JWT Authentication Filter
│   │       └── config/           # Route configuration
│   └── Dockerfile
│
├── auth_service/                 # Authentication — login, token issuance
│   ├── src/main/java/
│   │   └── com/pm/authservice/
│   │       ├── controller/       # Auth REST endpoints
│   │       ├── service/          # Token generation & validation
│   │       └── model/            # User entity
│   └── Dockerfile
│
├── patient_service/              # Core patient CRUD + event publishing
│   ├── src/main/java/
│   │   └── com/pm/patientservice/
│   │       ├── controller/       # Patient REST endpoints
│   │       ├── service/          # Business logic
│   │       ├── kafka/            # Kafka producer
│   │       ├── grpc/             # gRPC client (calls Billing Service)
│   │       └── model/            # Patient entity
│   └── Dockerfile
│
├── billing_service/              # Billing account management via gRPC
│   ├── src/main/java/
│   │   └── com/pm/billingservice/
│   │       ├── grpc/             # gRPC server implementation
│   │       ├── service/          # Billing logic
│   │       └── model/            # Billing entity
│   └── Dockerfile
│
├── analytics_service/            # Consumes Kafka events for reporting
│   ├── src/main/java/
│   │   └── com/pm/analyticsservice/
│   │       └── kafka/            # Kafka consumer
│   └── Dockerfile
│
├── integration-tests/            # End-to-end integration tests
│   └── src/test/java/
│
├── grpc-requests/                # .proto test request files (grpcurl / BloomRPC)
│   └── billing-service/
│
├── api-requests/                 # HTTP request files for API testing
│
└── .idea/                        # IntelliJ IDEA project config
```

---

## 🚀 Setup & Installation

### Prerequisites

Ensure the following tools are installed:

| Tool | Version | Purpose |
|---|---|---|
| JDK | 17+ | Java runtime |
| Maven | 3.8+ | Build tool |
| Docker | 24+ | Containerization |
| Docker Compose | 2.x | Local orchestration |
| AWS CLI | 2.x | AWS interaction |
| Node.js | 18+ | CDK runtime |
| AWS CDK | 2.x | Infrastructure as Code |
| LocalStack CLI | Latest | Local AWS emulation |

---

### 1. Clone the Repository

```bash
git clone https://github.com/vidhanshu37/patient_management_vid.git
cd patient_management_vid
```

---

### 2. Build All Services

```bash
# Build each service with Maven
cd auth_service && mvn clean package -DskipTests && cd ..
cd patient_service && mvn clean package -DskipTests && cd ..
cd billing_service && mvn clean package -DskipTests && cd ..
cd analytics_service && mvn clean package -DskipTests && cd ..
cd api_gateway && mvn clean package -DskipTests && cd ..
```

---

### 3. Start the Stack with Docker Compose

```bash
# Start all services, databases, and Kafka
docker compose up --build -d

# Check running containers
docker compose ps

# View logs for a specific service
docker compose logs -f patient_service
```

**Services will be available at:**

| Service | Port |
|---|---|
| API Gateway | `http://localhost:4004` |
| Auth Service | `http://localhost:4005` |
| Patient Service | `http://localhost:4000` |
| Billing Service (HTTP) | `http://localhost:4001` |
| Billing Service (gRPC) | `localhost:9001` |
| Analytics Service | `http://localhost:4002` |

---

### 4. LocalStack Setup (Local AWS Emulation)

```bash
# Install LocalStack CLI
pip install localstack

# Start LocalStack
localstack start -d

# Verify LocalStack is running
localstack status services

# Configure AWS CLI to point to LocalStack
aws configure set aws_access_key_id test
aws configure set aws_secret_access_key test
aws configure set region us-east-1

# Create Kafka topic in LocalStack MSK
aws --endpoint-url=http://localhost:4566 kafka create-cluster \
  --cluster-name patient-management-cluster \
  --kafka-version 2.8.0 \
  --number-of-broker-nodes 1 \
  --broker-node-group-info '{"InstanceType":"kafka.m5.large","ClientSubnets":["subnet-12345"]}'
```

---

### 5. AWS CDK Infrastructure

```bash
# Navigate to the CDK project
cd infrastructure   # or the CDK module directory

# Install CDK dependencies
npm install

# Bootstrap CDK (first-time only)
cdk bootstrap

# Synthesize CloudFormation templates
cdk synth

# Deploy to LocalStack (local testing)
cdklocal deploy --all

# Deploy to AWS (production)
cdk deploy --all
```

---

### 6. Running Services Locally (Without Docker)

```bash
# Start PostgreSQL and Kafka via Docker Compose only
docker compose up postgres-auth postgres-patient postgres-billing kafka zookeeper -d

# Run each Spring Boot service
cd auth_service && mvn spring-boot:run &
cd patient_service && mvn spring-boot:run &
cd billing_service && mvn spring-boot:run &
cd analytics_service && mvn spring-boot:run &
cd api_gateway && mvn spring-boot:run &
```

---

## 🔧 Environment Variables

### API Gateway

```properties
# application.properties
SPRING_APPLICATION_NAME=api-gateway
SERVER_PORT=4004

# Auth service JWT validation endpoint
AUTH_SERVICE_URL=http://auth-service:4005

# Route targets
PATIENT_SERVICE_URL=http://patient-service:4000
BILLING_SERVICE_URL=http://billing-service:4001
```

### Auth Service

```properties
SERVER_PORT=4005
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-auth:5432/auth_db
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=password
JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRATION_MS=86400000
```

### Patient Service

```properties
SERVER_PORT=4000
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-patient:5432/patient_db
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=password

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
KAFKA_TOPIC_PATIENT_EVENTS=patient.events

# gRPC - Billing Service
BILLING_SERVICE_GRPC_HOST=billing-service
BILLING_SERVICE_GRPC_PORT=9001
```

### Billing Service

```properties
SERVER_PORT=4001
GRPC_SERVER_PORT=9001
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-billing:5432/billing_db
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=password
```

### Analytics Service

```properties
SERVER_PORT=4002
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
KAFKA_TOPIC_PATIENT_EVENTS=patient.events
SPRING_KAFKA_CONSUMER_GROUP_ID=analytics-consumer-group
```

---

## 📡 API Endpoints

> All endpoints (except `/auth/**`) require a valid **JWT Bearer Token** in the `Authorization` header.

### Authentication

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/auth/login` | Login and receive JWT token | ❌ |
| `POST` | `/auth/register` | Register a new user | ❌ |
| `GET` | `/auth/validate` | Validate a JWT token | ✅ |

### Patients

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/patients` | List all patients | ✅ |
| `GET` | `/api/patients/{id}` | Get patient by ID | ✅ |
| `POST` | `/api/patients` | Create a new patient | ✅ |
| `PUT` | `/api/patients/{id}` | Update patient details | ✅ |
| `DELETE` | `/api/patients/{id}` | Delete a patient | ✅ |

### Billing

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/billing/{patientId}` | Get billing account for patient | ✅ |

### Sample Request

```bash
# 1. Login to get JWT
curl -X POST http://localhost:4004/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@hospital.com", "password": "admin123"}'

# Response: { "token": "eyJhbGciOiJIUzI1NiIs..." }

# 2. Create a patient using the JWT
curl -X POST http://localhost:4004/api/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "dateOfBirth": "1990-05-15",
    "address": "123 Main St, Springfield"
  }'
```

---

## 📨 Kafka Events

The **Patient Service** publishes domain events to Kafka whenever patient data changes.

### Topic: `patient.events`

| Event Type | Trigger | Payload Fields |
|---|---|---|
| `PATIENT_CREATED` | New patient registered | `patientId`, `name`, `email`, `dateOfBirth`, `timestamp` |
| `PATIENT_UPDATED` | Patient record modified | `patientId`, `name`, `email`, `updatedFields`, `timestamp` |
| `PATIENT_DELETED` | Patient record removed | `patientId`, `timestamp` |

### Event Schema Example

```json
{
  "eventType": "PATIENT_CREATED",
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "dateOfBirth": "1990-05-15",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Consumer Groups

| Service | Group ID | Topics Consumed |
|---|---|---|
| Analytics Service | `analytics-consumer-group` | `patient.events` |

---

## 🔌 gRPC Communication

The **Patient Service** calls the **Billing Service** synchronously via gRPC immediately upon patient creation, ensuring a billing account is always provisioned before the patient creation response is returned.

### Proto Definition (Conceptual)

```protobuf
syntax = "proto3";

package billing;

service BillingService {
  rpc CreateBillingAccount (BillingRequest) returns (BillingResponse);
}

message BillingRequest {
  string patient_id = 1;
  string name      = 2;
  string email     = 3;
}

message BillingResponse {
  string account_id = 1;
  string status     = 2;
}
```

### Configuration

```properties
# Patient Service — gRPC client
grpc.client.billing-service.address=static://billing-service:9001
grpc.client.billing-service.negotiation-type=plaintext

# Billing Service — gRPC server
grpc.server.port=9001
```

### Testing gRPC with grpcurl

```bash
grpcurl -plaintext \
  -d '{"patient_id": "123", "name": "John Doe", "email": "john@example.com"}' \
  localhost:9001 billing.BillingService/CreateBillingAccount
```

---

## ☁️ Infrastructure (AWS CDK)

The infrastructure is fully defined as code using **AWS CDK (TypeScript)**.

### Provisioned Resources

| Resource | Details |
|---|---|
| **VPC** | Custom VPC with public and private subnets |
| **ECS Cluster** | Fargate cluster hosting all microservice tasks |
| **ECS Services** | One Fargate service per microservice |
| **Task Definitions** | CPU/memory specs, environment variables, image URIs |
| **Application Load Balancer** | Routes external traffic to API Gateway service |
| **Amazon MSK** | Managed Kafka cluster (replaces local Kafka in prod) |
| **Security Groups** | Fine-grained inbound/outbound rules per service |
| **IAM Roles** | Task execution roles with least-privilege policies |
| **ECR Repositories** | Container registries for each service image |

### CDK Stack Layout

```
infrastructure/
├── bin/
│   └── app.ts              # CDK App entry point
├── lib/
│   ├── network-stack.ts    # VPC, subnets, security groups
│   ├── ecs-stack.ts        # ECS Cluster, Task Definitions, Services
│   ├── msk-stack.ts        # MSK (Kafka) cluster
│   └── alb-stack.ts        # Application Load Balancer
└── cdk.json
```

---

## 🚢 Deployment

### Local Development (Docker Compose)

```bash
docker compose up --build -d
```

### Deploy to LocalStack

```bash
# Start LocalStack
localstack start -d

# Deploy CDK stacks to LocalStack
cdklocal deploy --all --require-approval never
```

### Deploy to AWS (Production)

```bash
# Authenticate Docker with ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Build and push all Docker images
docker build -t patient-service ./patient_service
docker tag patient-service:latest <ecr-uri>/patient-service:latest
docker push <ecr-uri>/patient-service:latest
# (Repeat for each service)

# Deploy infrastructure
cdk deploy --all --require-approval never
```

### Deployment Workflow

```mermaid
flowchart LR
    A[Developer Push] --> B[Maven Build]
    B --> C[Docker Build Images]
    C --> D{Target?}
    D -->|Local| E[Docker Compose Up]
    D -->|LocalStack| F[cdklocal deploy]
    D -->|AWS Prod| G[Push to ECR]
    G --> H[cdk deploy]
    H --> I[ECS Rolling Update]
    I --> J[Health Checks Pass]
    J --> K[Traffic Shifted ✅]
```

---

## 🛠️ Troubleshooting

### Docker Compose services fail to start

```bash
# Check logs for a specific service
docker compose logs -f patient_service

# Ensure ports are not already in use
lsof -i :4000   # Check Patient Service port
```

### Kafka connection refused

```bash
# Confirm Kafka container is healthy
docker compose ps kafka

# Verify topic exists
docker exec -it kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 --list
```

### gRPC connection errors

```bash
# Confirm Billing Service gRPC port is exposed
docker compose ps billing_service

# Test gRPC connectivity
grpcurl -plaintext localhost:9001 list
```

### JWT validation fails at API Gateway

- Ensure the JWT secret in `auth_service` and `api_gateway` matches exactly
- Check token expiry (`JWT_EXPIRATION_MS`)
- Confirm the `Authorization: Bearer <token>` header format

### CDK deploy fails with LocalStack

```bash
# Restart LocalStack and redeploy
localstack stop && localstack start -d
cdklocal bootstrap
cdklocal deploy --all
```

---

## 🔮 Future Improvements

- [ ] **CI/CD Pipeline** — GitHub Actions workflow for automated build, test, and deploy to AWS on every push to `main`
- [ ] **Service Discovery** — Integrate AWS Cloud Map or Consul for dynamic service registration and discovery
- [ ] **Distributed Tracing** — Add OpenTelemetry + AWS X-Ray for end-to-end request tracing across services
- [ ] **Centralized Logging** — Ship all container logs to AWS CloudWatch Logs with structured JSON format
- [ ] **API Rate Limiting** — Add rate limiting at the API Gateway layer using Redis-backed token buckets
- [ ] **Schema Registry** — Introduce Confluent Schema Registry (or AWS Glue) for Kafka Avro/Protobuf schema governance
- [ ] **Health Dashboard** — Implement Spring Boot Actuator + AWS CloudWatch dashboards for real-time service health
- [ ] **Secrets Management** — Migrate environment variable secrets to AWS Secrets Manager / Parameter Store
- [ ] **Horizontal Scaling** — Configure ECS service auto-scaling policies based on CPU/memory and Kafka consumer lag

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m 'feat: add your feature'`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

Please follow [Conventional Commits](https://www.conventionalcommits.org/) for commit messages and ensure all services build cleanly before submitting a PR.

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

<div align="center">

**Vidhanshu Borade**

[![GitHub](https://img.shields.io/badge/GitHub-vidhanshu37-181717?style=for-the-badge&logo=github)](https://github.com/vidhanshu37)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0077B5?style=for-the-badge&logo=linkedin)](https://linkedin.com/in/vidhanshu37)

_Built with ☕ Java, ❤️ Spring Boot, and ☁️ AWS_

</div>

---

<div align="center">

⭐ If you found this project useful, please consider giving it a star!

</div>