# AI Customer Support Agent — Backend Startup & Deployment Guide (`backend1`)

This guide explains how to configure, build, run, and test the **AI-Powered Customer Support & Conversational QnA Backend (`backend1`)** from a clean environment.

---

## 1. Prerequisites

Before starting the backend, ensure the following tools are installed:

* **Java 21 or later** (JDK 21 / JDK 25 supported)
* **Maven 3.9+** (or use the included Maven wrapper `mvnw` / `mvnw.cmd`)
* **Docker & Docker Compose** (for PostgreSQL with `pgvector`)
* **Google Gemini API Key** (for Gemini 2.5/Flash reasoning & `gemini-embedding-001` vector embeddings)

---

## 2. PostgreSQL + pgvector Setup

The backend requires PostgreSQL with the `pgvector` extension enabled for storing and searching document vector embeddings.

### 2.1. Start Database via Docker Compose

A ready-to-run Docker Compose configuration is available in `deployment/docker-compose/docker-compose.yml`:

```bash
docker compose -f deployment/docker-compose/docker-compose.yml up -d
```

### 2.2. Verify pgvector Extension

Ensure the `vector` extension is active:

```bash
docker exec -it ai-chat-postgres psql -U postgres -d ai_chat -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

> **Note on Flyway**: Database schemas and the `document_chunks` table are automatically initialized and migrated by Flyway upon application startup.

---

## 3. Environment Configuration

Create or update the `.env` file in `backend1/.env` (or supply environment variables to your shell/IDE):

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
# NLP MICROSERVICE (Default: Mock Enabled)
# =========================================================
NLP_MOCK_ENABLED=true
NLP_SERVICE_URL=http://localhost:8000/api/nlp/analyze
```

---

## 4. Building and Running the Backend

Always run Maven commands from the `backend1` directory.

### 4.1. Run Automated Unit Tests

**Windows (PowerShell)**:
```powershell
cd backend1
.\mvnw.cmd test "-Dtest=MockNlpServiceTest,ChatServiceTest,ChunkingTest"
```

**Linux / macOS**:
```bash
cd backend1
./mvnw test -Dtest=MockNlpServiceTest,ChatServiceTest,ChunkingTest
```

### 4.2. Start the Spring Boot Application

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

The application will start on **`http://localhost:8080`**.

---

## 5. Verifying the Endpoints

### 5.1. Health Check
```bash
curl http://localhost:8080/actuator/health
```
**Response**: `{"status":"UP"}`

---

### 5.2. Test SSE Chat Streaming (`/api/v1/chat/stream`)

Test the real-time token and NLP streaming endpoint:

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/stream \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -H "X-Tenant-Id: default" \
  -d '{
    "text": "My UPI transaction failed but money was deducted. Transaction ID is TXN12345.",
    "customer_id": "cust_001",
    "category": "banking"
  }'
```

**Expected Stream Output**:
```sse
event: nlp
data: {"success":true,"conversation_id":"...","nlp":{"domain":{"label":"banking"},"intent":{"label":"transaction_failed"},"frustration":{"score":72,"level":"high"},"entities":[{"type":"transaction_id","value":"TXN12345"}]}}

event: token
data: I understand your concern 

event: token
data: regarding the failed UPI transaction TXN12345.

event: sources
data: []

event: done
data: 
```

---

### 5.3. Test Document Ingestion (`/api/v1/documents`)

Upload a knowledge base document (`.pdf`, `.docx`, `.txt`, `.md`):

```bash
curl -X POST http://localhost:8080/api/v1/documents \
  -H "X-Tenant-Id: default" \
  -F "file=@/path/to/faq.pdf" \
  -F "category=banking"
```

**Response**:
```json
{
  "id": "c3a9f024-5717-4562-b3fc-2c963f66afb2",
  "status": "PROCESSING"
}
```

Check ingestion status:
```bash
curl -X GET http://localhost:8080/api/v1/documents/c3a9f024-5717-4562-b3fc-2c963f66afb2 \
  -H "X-Tenant-Id: default"
```

---

## 6. Switching from Mock NLP to External Python Microservice

1. Ensure your Python NLP microservice is running and accessible at `http://localhost:8000/api/nlp/analyze`.
2. In your `.env` or system environment, set:
   ```properties
   NLP_MOCK_ENABLED=false
   NLP_SERVICE_URL=http://localhost:8000/api/nlp/analyze
   ```
3. Restart the Spring Boot backend (`.\mvnw.cmd spring-boot:run`).
4. The backend will automatically send `NlpAnalysisRequest` JSON payloads to the microservice. If the microservice temporarily goes offline, the backend automatically falls back to `MockNlpService` to ensure uninterrupted customer support.
