# Conversational QnA & Live Monitoring API Documentation (`backend1`)

This document provides the complete API specification for the **AI Customer Support & Live Monitoring System (`backend1`)**.

---

## 1. Architectural Overview & Dual-Dashboard Model

The backend supports two core client dashboards:

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│  DASHBOARD 1: CUSTOMER DASHBOARD (Consumer Text & Voice QnA)                           │
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
│  DASHBOARD 2: MONITORING & SUPERVISOR DASHBOARD (Live Call/Chat Control)              │
│  • Real-time call monitor with live sentiment, emotion & frustration gauges            │
│  • Auto-alerts when customer is frustrated: "Recommend switching to live agent"        │
│  • One-click Human Takeover Button (/api/v1/monitoring/conversations/{id}/takeover)    │
│  • Direct Human Agent messaging console (/api/v1/monitoring/conversations/{id}/message)│
│  • One-click Hand Back to AI (/api/v1/monitoring/conversations/{id}/handback)          │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Base Configuration & Global Headers

* **Base URL**: `http://localhost:8080`
* **Tenant Header (Required)**: All requests must include the `X-Tenant-Id` header (e.g., `X-Tenant-Id: default`).
* **Content Types**:
  * `application/json` for standard requests
  * `multipart/form-data` for file uploads
  * `text/event-stream` for SSE streaming

---

## 3. Customer Chat & Voice Endpoints

### 3.1. Stream Chat & Voice Message (`/api/v1/chat/stream`)

Streams real-time tokens, live NLP analytics, and escalation alerts via Server-Sent Events (SSE).

* **Method**: `POST`
* **URL**: `/api/v1/chat/stream`
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
Fires immediately after NLP analysis:
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

##### Event 2: `event: escalation_alert` (Fires if Frustration >= 70 or Escalation Triggered)
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

##### Event 3: `event: token` (Emitted sequentially during AI generation)
```sse
event: token
data: I understand your frustration regarding transaction TXN12345.
```

##### Event 4: `event: human_agent_active` (Fires instead of LLM tokens if Human Takeover is active)
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

### 3.2. Synchronous Chat Message (`/api/v1/chat`)

* **Method**: `POST`
* **URL**: `/api/v1/chat`

#### Response Body (HTTP 200 OK)
```json
{
  "answer": "Your deducted funds for transaction TXN12345 will automatically reverse within 24 to 48 hours.",
  "conversationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "sources": [
    {
      "title": "UPI FAQs.pdf",
      "pageNumber": 3,
      "similarityScore": 0.89,
      "snippet": "In case of failed UPI transactions..."
    }
  ],
  "nlp": { ... }
}
```

---

## 4. Live Call & Monitoring Endpoints (Supervisor Dashboard)

### 4.1. List Monitored Conversations

Retrieves all active and past calls/chats with real-time frustration scores, emotion badges, escalation status, and current mode (`AI` vs `HUMAN`).

* **Method**: `GET`
* **URL**: `/api/v1/monitoring/conversations`
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
    "escalationReason": "Customer frustration score is 72 (high) on intent 'transaction_failed'. AI recommends switching to a live agent.",
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

### 4.2. Get Conversation Monitoring Detail & Transcript

Fetches complete live transcript with all `USER`, `ASSISTANT`, `AGENT`, and `SYSTEM` messages.

* **Method**: `GET`
* **URL**: `/api/v1/monitoring/conversations/{id}`
* **Headers**: `X-Tenant-Id: default`

#### Response Body (HTTP 200 OK)
```json
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
  "createdAt": "2026-08-22T10:00:00Z",
  "updatedAt": "2026-08-22T10:05:00Z",
  "messages": [
    {
      "id": "msg_001",
      "role": "USER",
      "content": "My UPI transaction failed but money was deducted.",
      "tokenCount": 12,
      "model": null,
      "latencyMs": null,
      "createdAt": "2026-08-22T10:00:00Z",
      "sources": []
    },
    {
      "id": "msg_002",
      "role": "ASSISTANT",
      "content": "I understand your concern regarding the failed transaction...",
      "tokenCount": 28,
      "model": "gemini",
      "latencyMs": 850,
      "createdAt": "2026-08-22T10:00:02Z",
      "sources": [
        {
          "title": "UPI FAQs.pdf",
          "pageNumber": 3,
          "similarityScore": 0.89,
          "snippet": "In case of failed UPI transactions..."
        }
      ]
    }
  ]
}
```

---

### 4.3. Human Agent Takeover

Transfers the conversation/call from AI to a human supervisor. The AI halts automatic responses, and future customer messages route to the human agent queue.

* **Method**: `POST`
* **URL**: `/api/v1/monitoring/conversations/{id}/takeover`
* **Headers**:
  * `Content-Type: application/json`
  * `X-Tenant-Id: default`

#### Request Body
```json
{
  "agentName": "Sarah Supervisor",
  "notes": "Taking over due to high frustration on banking transaction"
}
```

#### Response Body (HTTP 200 OK)
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "title": "UPI transaction issue",
  "mode": "HUMAN",
  "assignedAgent": "Sarah Supervisor",
  "escalationStatus": "ESCALATED",
  "callStatus": "ACTIVE",
  "updatedAt": "2026-08-22T10:06:00Z"
}
```

---

### 4.4. Send Message as Human Agent

Allows the supervisor to post a direct reply into the conversation as `AGENT`.

* **Method**: `POST`
* **URL**: `/api/v1/monitoring/conversations/{id}/message`
* **Headers**:
  * `Content-Type: application/json`
  * `X-Tenant-Id: default`

#### Request Body
```json
{
  "message": "Hello, I am Sarah from customer support. I have initiated an instant status check for TXN12345.",
  "agentName": "Sarah Supervisor"
}
```

#### Response Body (HTTP 200 OK)
```json
{
  "id": "msg_agent_001",
  "role": "AGENT",
  "content": "Hello, I am Sarah from customer support. I have initiated an instant status check for TXN12345.",
  "tokenCount": 22,
  "model": "human:Sarah Supervisor",
  "createdAt": "2026-08-22T10:06:15Z",
  "sources": []
}
```

---

### 4.5. Hand Back Call/Chat to AI

Returns the conversation to AI auto-pilot mode.

* **Method**: `POST`
* **URL**: `/api/v1/monitoring/conversations/{id}/handback`
* **Headers**: `X-Tenant-Id: default`

#### Response Body (HTTP 200 OK)
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "title": "UPI transaction issue",
  "mode": "AI",
  "assignedAgent": null,
  "escalationStatus": "RESOLVED",
  "callStatus": "ACTIVE",
  "updatedAt": "2026-08-22T10:08:00Z"
}
```

---

### 4.6. Live Supervisor Stats

* **Method**: `GET`
* **URL**: `/api/v1/monitoring/stats`
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

## 5. Knowledge Base Management Endpoints

* `POST /api/v1/documents`: Upload file (multipart form data). Returns `202 Accepted` with `{ "id": "...", "status": "PROCESSING" }`.
* `GET /api/v1/documents`: List all documents.
* `GET /api/v1/documents/{id}`: Check document ingestion status (`PROCESSING`, `COMPLETED`, `FAILED`).
* `DELETE /api/v1/documents/{id}`: Remove document & delete vector chunks.
