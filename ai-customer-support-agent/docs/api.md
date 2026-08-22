# Conversational QnA & Live Monitoring API Documentation (`backend1`)

This document provides the complete API specification for the **AI Customer Support & Live Monitoring System (`backend1`)**.

---

## 1. Architectural Overview & Dual-Dashboard Model

The backend supports two core client dashboards:

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│  DASHBOARD 1: CUSTOMER DASHBOARD (Consumer Text & Voice QnA)                           │
│  • Pure consumer interface — NO document upload or ingestion controls                  │
│  • Text Mode: Sends user questions to /api/v1/chat/stream                              │
│  • Voice Mode: Speech-to-Text (STT) -> /api/v1/chat/stream -> Text-to-Speech (TTS)     │
│  • Displays live transcript and maintains full session history                         │
└────────────────────────────────────────────────────────────────────────────────────────┘
                                            │
                                            ▼
                    [ AI Chat & RAG Engine + NLP Service ]
                    • Computes Frustration, Emotion, Intent, Urgency
                    • Enriches Vector Query & Streams Grounded LLM Tokens
                    • Evaluates Escalation Rules (Frustration Score >= 70)
                                            │
                                            ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│  DASHBOARD 2: MONITORING & SUPERVISOR DASHBOARD (Live Call/Chat Control & Docs)       │
│  • Knowledge Base Management: Upload, index status & delete documents                  │
│  • Real-time call monitor with live sentiment, emotion & frustration gauges            │
│  • Auto-alerts when customer is frustrated: "Recommend switching to live agent"        │
│  • One-click Human Takeover Button (/api/v1/monitoring/conversations/{id}/takeover)    │
│  • Direct Human Agent messaging console (/api/v1/monitoring/conversations/{id}/message)│
│  • One-click Hand Back to AI (/api/v1/monitoring/conversations/{id}/handback)          │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Base Configuration, Global Headers & Route Aliases

* **Base URL**: `http://localhost:8080`
* **Route Aliases**: Both versioned (`/api/v1/...`) and unversioned (`/api/...`) paths are supported seamlessly across all controllers:
  * `/api/v1/chat` and `/api/chat`
  * `/api/v1/monitoring` and `/api/monitoring`
  * `/api/v1/documents` and `/api/documents`
  * `/api/v1/conversations` and `/api/conversations`
* **Tenant Header (Required)**: All requests must include the `X-Tenant-Id` header (e.g., `X-Tenant-Id: default`).
* **CORS**: Cross-Origin Resource Sharing (`@CrossOrigin`) is enabled for all origins.
* **Content Types**:
  * `application/json` for standard requests
  * `multipart/form-data` for file uploads
  * `text/event-stream` for SSE streaming

---

## 3. Customer Chat & Voice Endpoints

### 3.1. Stream Chat & Voice Message (`POST /api/v1/chat/stream` or `POST /api/chat/stream`)

Streams real-time tokens, live NLP analytics, and escalation alerts via Server-Sent Events (SSE).

* **Method**: `POST`
* **URL**: `/api/v1/chat/stream` (or `/api/chat/stream`)
* **Headers**:
  * `Content-Type: application/json`
  * `Accept: text/event-stream`
  * `X-Tenant-Id: default`

#### Request Body
```json
{
  "conversation_id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "text": "My UPI transaction failed but money was deducted. Transaction ID is TXN12345.",
  "customer_id": "cust_987",
  "category": "banking"
}
```

#### SSE Events Lifecycle

##### Event 1: `event: nlp`
```sse
event: nlp
data: {
  "success": true,
  "conversation_id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "message_id": "msg_001",
  "nlp": {
    "language": { "label": "en", "confidence": 0.99 },
    "domain": { "label": "banking", "confidence": 0.96 },
    "intent": { "label": "transaction_failed", "confidence": 0.94 },
    "sentiment": { "label": "negative", "confidence": 0.91 },
    "emotion": { "label": "frustrated", "confidence": 0.88 },
    "frustration": { "score": 72, "level": "high" },
    "urgency": { "level": "medium", "confidence": 0.82 },
    "entities": [{ "type": "transaction_id", "value": "TXN12345", "confidence": 0.95 }]
  },
  "conversation_analysis": { "frustration_trend": "increasing" }
}
```

##### Event 2: `event: escalation_alert` (Emitted when Frustration Score >= 70)
```sse
event: escalation_alert
data: {
  "recommended": true,
  "reason": "Customer frustration score is 72 (high) on intent 'transaction_failed'. AI recommends switching to a live agent.",
  "frustrationScore": 72,
  "frustrationLevel": "high",
  "emotion": "frustrated",
  "intent": "transaction_failed"
}
```

##### Event 3: `event: token` (AI LLM Token Stream)
```sse
event: token
data: I understand your frustration regarding transaction TXN12345.
```

##### Event 4: `event: human_agent_active` (Emitted when Human Takeover is Active)
```sse
event: human_agent_active
data: A live support specialist is currently handling this session. Your message has been delivered to the agent.
```

##### Event 5: `event: sources`
```sse
event: sources
data: [
  {
    "title": "UPI FAQs.pdf",
    "pageNumber": 3,
    "similarityScore": 0.89,
    "snippet": "In case of failed UPI transactions where amount is deducted..."
  }
]
```

##### Event 6: `event: done`
```sse
event: done
data: 
```

---

## 4. Live Call & Monitoring Endpoints (Supervisor Dashboard)

### 4.1. Live Supervisor Stats (`GET /api/v1/monitoring/stats` or `GET /api/monitoring/stats`)

* **Method**: `GET`
* **URL**: `/api/v1/monitoring/stats` (or `/api/monitoring/stats`)
* **Headers**: `X-Tenant-Id: default`

#### Response Body (HTTP 200 OK)
```json
{
  "totalConversations": 24,
  "activeAiConversations": 19,
  "activeHumanConversations": 5,
  "escalationRecommendedCount": 3,
  "escalatedCount": 2,
  "averageFrustrationScore": 42.6
}
```

---

### 4.2. List Monitored Calls (`GET /api/v1/monitoring/conversations` or `GET /api/monitoring/conversations`)

* **Method**: `GET`
* **URL**: `/api/v1/monitoring/conversations` (or `/api/monitoring/conversations`)
* **Headers**: `X-Tenant-Id: default`

#### Response Body (HTTP 200 OK)
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "title": "UPI transaction issue",
    "mode": "AI",
    "assignedAgent": null,
    "escalationStatus": "RECOMMENDED",
    "escalationReason": "Customer frustration score is 72 (high)...",
    "frustrationScore": 72,
    "frustrationLevel": "high",
    "sentiment": "negative",
    "emotion": "frustrated",
    "intent": "transaction_failed",
    "domain": "banking",
    "callStatus": "ACTIVE",
    "messageCount": 4,
    "lastMessageSnippet": "My UPI transaction failed and money was debited.",
    "createdAt": "2026-08-22T10:00:00Z",
    "updatedAt": "2026-08-22T10:05:00Z"
  }
]
```

---

### 4.3. Get Conversation Detail (`GET /api/v1/monitoring/conversations/{id}`)

* **Method**: `GET`
* **URL**: `/api/v1/monitoring/conversations/{id}`
* **Headers**: `X-Tenant-Id: default`

---

### 4.4. Human Agent Takeover (`POST /api/v1/monitoring/conversations/{id}/takeover`)

* **Method**: `POST`
* **URL**: `/api/v1/monitoring/conversations/{id}/takeover`
* **Body**: `{"agentName": "Sarah Supervisor"}`

---

### 4.5. Send Human Agent Message (`POST /api/v1/monitoring/conversations/{id}/message`)

* **Method**: `POST`
* **URL**: `/api/v1/monitoring/conversations/{id}/message`
* **Body**: `{"message": "Hello, I am looking into this now.", "agentName": "Sarah Supervisor"}`

---

### 4.6. Hand Back Call to AI (`POST /api/v1/monitoring/conversations/{id}/handback`)

* **Method**: `POST`
* **URL**: `/api/v1/monitoring/conversations/{id}/handback`

---

## 5. Knowledge Base Document Ingestion (Monitoring Dashboard)

### 5.1. Upload Document (`POST /api/v1/documents` or `POST /api/documents`)
* **Method**: `POST`
* **Body**: `multipart/form-data` with `file` and optional `category`, `title`.
* **Response (HTTP 202 Accepted)**:
```json
{
  "id": "c3a9f024-5717-4562-b3fc-2c963f66afb2",
  "status": "PROCESSING"
}
```

### 5.2. List Documents (`GET /api/v1/documents` or `GET /api/documents`)
* **Method**: `GET`
* **Response (HTTP 200 OK)**: Array of document objects with `id`, `filename`, `category`, `status`, `chunkCount`.

### 5.3. Get Document Status (`GET /api/v1/documents/{id}`)
* **Method**: `GET`

### 5.4. Delete Document (`DELETE /api/v1/documents/{id}` or `DELETE /api/documents/{id}`)
* **Method**: `DELETE`
* **URL**: `/api/v1/documents/{id}` (or `/api/documents/{id}`)
* **Response (HTTP 204 No Content)**
