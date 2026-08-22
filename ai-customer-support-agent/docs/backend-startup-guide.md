# AI Customer Support Agent — Backend Startup & Deployment Guide (`backend1`)

This guide describes how to configure, run, and test the **AI-Powered Customer Support & Live Monitoring Backend (`backend1`)** from a clean environment.

---

## 1. Prerequisites

Ensure you have the following installed:

* **Java 21 or later** (JDK 21 / JDK 25 supported)
* **Maven 3.9+** (or use the included wrapper `mvnw` / `mvnw.cmd`)
* **Docker & Docker Compose** (for PostgreSQL with `pgvector`)
* **Google Gemini API Key** (for Gemini 2.5/Flash reasoning & `gemini-embedding-001` embeddings)

---

## 2. PostgreSQL + pgvector Setup

### 2.1. Start Database via Docker Compose

```bash
docker compose -f deployment/docker-compose/docker-compose.yml up -d
```

### 2.2. Verify Database Extension

```bash
docker exec -it ai-chat-postgres psql -U postgres -d ai_chat -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

### 2.3. Database Migrations (Managed Automatically by Flyway)
* **`V1__initial_schema.sql`**: Core relational tables (`tenants`, `documents`, `document_chunks`, `conversations`, `messages`, `message_sources`).
* **`V2__gemini_embeddings.sql`**: Configures 768-dimensional Gemini vector index.
* **`V3__monitoring_and_handover.sql`**: Adds live call monitoring columns (`mode`, `assigned_agent`, `escalation_status`, `escalation_reason`, `last_frustration_score`, `last_sentiment`, etc.).

---

## 3. Environment Variables (`.env`)

Create or update `backend1/.env`:

```properties
# =========================================================
# AI MODEL CONFIGURATION (Google Gemini)
# =========================================================
GOOGLE_API_KEY=your_gemini_api_key_here

# =========================================================
# DATABASE CONFIGURATION
# =========================================================
POSTGRES_PORT=5432
POSTGRES_DB=ai_chat
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# =========================================================
# NLP MICROSERVICE
# =========================================================
NLP_MOCK_ENABLED=true
NLP_SERVICE_URL=http://localhost:8000/api/nlp/analyze
```

---

## 4. Building and Running the Backend

Always run commands inside the **`backend1`** directory.

### 4.1. Run Automated Unit Tests

**Windows (PowerShell)**:
```powershell
cd backend1
.\mvnw.cmd test "-Dtest=MockNlpServiceTest,ChatServiceTest,ChunkingTest,MonitoringServiceTest,MonitoringControllerTest"
```

**Linux / macOS**:
```bash
cd backend1
./mvnw test -Dtest=MockNlpServiceTest,ChatServiceTest,ChunkingTest,MonitoringServiceTest,MonitoringControllerTest
```

### 4.2. Start the Spring Boot Backend

**Windows (PowerShell)**:
```powershell
cd backend1
.\mvnw.cmd spring-boot:run
```

**Linux / macOS**:
```bash
cd backend1
./mvnw spring-boot:run
```

The application starts on **`http://localhost:8080`**.

---

## 5. Testing the Endpoints via `curl`

### 5.1. Customer Voice/Text Chat Stream
```bash
curl -N -X POST http://localhost:8080/api/v1/chat/stream \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -H "X-Tenant-Id: default" \
  -d '{
    "text": "My UPI transaction failed and money was debited. TXN12345",
    "customer_id": "cust_001",
    "category": "banking"
  }'
```

---

### 5.2. Live Supervisor Monitoring (`/api/v1/monitoring/*`)

#### List Monitored Calls with Live Frustration Scores
```bash
curl -X GET http://localhost:8080/api/v1/monitoring/conversations \
  -H "X-Tenant-Id: default"
```

#### Get Full Call Transcript
```bash
curl -X GET http://localhost:8080/api/v1/monitoring/conversations/{conversationId} \
  -H "X-Tenant-Id: default"
```

#### Admin Takeover Call (Switch to Human Agent)
```bash
curl -X POST http://localhost:8080/api/v1/monitoring/conversations/{conversationId}/takeover \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: default" \
  -d '{"agentName": "Sarah Supervisor"}'
```

#### Send Message as Human Agent
```bash
curl -X POST http://localhost:8080/api/v1/monitoring/conversations/{conversationId}/message \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: default" \
  -d '{"message": "Hello, I have initiated a refund check.", "agentName": "Sarah Supervisor"}'
```

#### Hand Back Call to AI
```bash
curl -X POST http://localhost:8080/api/v1/monitoring/conversations/{conversationId}/handback \
  -H "X-Tenant-Id: default"
```

#### Get Live Supervisor Stats
```bash
curl -X GET http://localhost:8080/api/v1/monitoring/stats \
  -H "X-Tenant-Id: default"
```
