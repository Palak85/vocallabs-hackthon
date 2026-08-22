# Conversational QnA API Documentation (`backend1`)

This document provides the complete API specification for the **AI-Powered Customer Support & Conversational QnA Backend (`backend1`)**.

---

## 1. Architectural Overview & Request Flow

The backend orchestrates customer conversations through an intelligent RAG pipeline enriched with an NLP analysis layer:

```text
Customer Input (Text / STT)
           │
           ▼
   [ POST /api/v1/chat/stream ]
           │
           ├──► 1. NLP Service (Mocked / HTTP to http://localhost:8000/api/nlp/analyze)
           │       Extracts: Language, Domain, Intent, Sentiment, Emotion, Frustration, Urgency, Entities
           │       Dispatches `event: nlp` to Frontend (Live HUD update)
           │
           ├──► 2. Query Enrichment
           │       Combines Customer Query + Intent + Domain + Entities
           │
           ├──► 3. Vector Knowledge Base Search (pgvector)
           │       Performs cosine similarity search using Google Gemini Embeddings
           │
           ├──► 4. Grounded LLM Prompt Construction
           │       Combines: Retrieved Chunks + NLP Insights (Tone/Frustration adaptation) + Query + History
           │
           ├──► 5. Gemini 2.5/Flash Stream Generation
           │       Dispatches `event: token` as text arrives
           │
           └──► 6. Finalization & Citations
                   Dispatches `event: sources` and `event: done`
```

---

## 2. Base Configuration & Global Headers

* **Base URL**: `http://localhost:8080`
* **Tenant Header (Required)**: All requests must include the `X-Tenant-Id` header (e.g. `default`, `tenant_001`, or user/org ID).
* **Content Types**:
  * `application/json` for standard requests
  * `multipart/form-data` for document uploads
  * `text/event-stream` for SSE chat streaming

---

## 3. Endpoints Specification

### 3.1. Stream Chat Message (Recommended for UI)

Streams real-time LLM tokens and live NLP analysis to the frontend via Server-Sent Events (SSE).

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

> **Note**: Field aliases are supported (`conversation_id` or `conversationId`, `text` or `question` or `message`, `customer_id` or `customerId`). If `conversation_id` is omitted, a new conversation is automatically created.

#### Server-Sent Events (SSE) Lifecycle

##### Event 1: `event: nlp`
Fires immediately after NLP analysis completes (before LLM token generation begins). Use this to populate customer emotion badges, frustration gauges, and detected entities in the UI.

```sse
event: nlp
data: {
  "success": true,
  "conversation_id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "message_id": "a1b2c3d4-0000-0000-0000-000000000001",
  "nlp": {
    "language": {
      "label": "en",
      "confidence": 0.99
    },
    "domain": {
      "label": "banking",
      "confidence": 0.96
    },
    "intent": {
      "label": "transaction_failed",
      "confidence": 0.94
    },
    "sentiment": {
      "label": "negative",
      "confidence": 0.91
    },
    "emotion": {
      "label": "frustrated",
      "confidence": 0.88
    },
    "frustration": {
      "score": 72,
      "level": "high"
    },
    "urgency": {
      "level": "medium",
      "confidence": 0.82
    },
    "entities": [
      {
        "type": "transaction_id",
        "value": "TXN12345",
        "confidence": 0.95
      }
    ]
  },
  "conversation_analysis": {
    "frustration_trend": "increasing"
  }
}
```

##### Event 2: `event: token` (Repeated)
Fires each time a text token is emitted by the LLM.

```sse
event: token
data: I understand 

event: token
data: your frustration regarding 

event: token
data: the failed UPI transaction TXN12345.
```

##### Event 3: `event: sources`
Fires when the generation completes, delivering citations from the knowledge base.

```sse
event: sources
data: [
  {
    "title": "UPI Banking FAQs.pdf",
    "pageNumber": 3,
    "similarityScore": 0.89,
    "snippet": "In case of failed UPI transactions where amount is deducted, the amount is automatically reversed within 24 to 48 banking hours."
  }
]
```

##### Event 4: `event: done`
Signals the end of the SSE stream.

```sse
event: done
data: 
```

##### Event 5: `event: error` (Optional on failure)
```sse
event: error
data: Tenant ID context missing
```

---

### 3.2. Synchronous Chat Message (Non-Streaming Fallback)

* **Method**: `POST`
* **URL**: `/api/v1/chat`
* **Headers**:
  * `Content-Type: application/json`
  * `X-Tenant-Id: default`

#### Request Body
```json
{
  "conversationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "question": "What is the return policy for electronics?",
  "customerId": "cust_123"
}
```

#### Response Body (HTTP 200 OK)
```json
{
  "answer": "Electronics can be returned within 14 days of delivery provided they are in original packaging and undamaged.",
  "conversationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "sources": [
    {
      "title": "Return_Policy_2026.pdf",
      "pageNumber": 2,
      "similarityScore": 0.88,
      "snippet": "Electronics Category: 14-day return window with receipt."
    }
  ],
  "nlp": {
    "success": true,
    "conversation_id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "message_id": "msg_001",
    "nlp": {
      "language": { "label": "en", "confidence": 0.99 },
      "domain": { "label": "e-commerce", "confidence": 0.95 },
      "intent": { "label": "refund_request", "confidence": 0.93 },
      "sentiment": { "label": "neutral", "confidence": 0.85 },
      "emotion": { "label": "neutral", "confidence": 0.85 },
      "frustration": { "score": 20, "level": "low" },
      "urgency": { "level": "low", "confidence": 0.70 },
      "entities": []
    },
    "conversation_analysis": {
      "frustration_trend": "stable"
    }
  }
}
```

---

### 3.3. Upload Document to Knowledge Base

Uploads and asynchronously ingests a document (PDF, DOCX, TXT, MD) into `pgvector`.

* **Method**: `POST`
* **URL**: `/api/v1/documents`
* **Headers**:
  * `Content-Type: multipart/form-data`
  * `X-Tenant-Id: default`

#### Form Parameters
| Parameter | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `file` | `File` | **Yes** | File binary (Max 20MB: PDF, DOCX, TXT, MD) |
| `title` | `String` | No | Custom document title |
| `category` | `String` | No | Category tag (e.g., `banking`, `policies`, `technical`) |

#### Response Body (HTTP 202 Accepted)
```json
{
  "id": "c3a9f024-5717-4562-b3fc-2c963f66afb2",
  "status": "PROCESSING"
}
```

---

### 3.4. List Documents

* **Method**: `GET`
* **URL**: `/api/v1/documents?page=0&size=20`
* **Headers**: `X-Tenant-Id: default`

#### Response Body (HTTP 200 OK)
```json
[
  {
    "id": "c3a9f024-5717-4562-b3fc-2c963f66afb2",
    "tenantId": "default",
    "title": "UPI Banking FAQs",
    "category": "banking",
    "filename": "upi_faq.pdf",
    "contentType": "application/pdf",
    "sizeBytes": 524288,
    "status": "COMPLETED",
    "errorMessage": null,
    "createdAt": "2026-08-22T10:00:00Z",
    "updatedAt": "2026-08-22T10:00:05Z"
  }
]
```

---

### 3.5. Get Document Details & Status

* **Method**: `GET`
* **URL**: `/api/v1/documents/{id}`
* **Headers**: `X-Tenant-Id: default`

#### Response Body (HTTP 200 OK)
```json
{
  "id": "c3a9f024-5717-4562-b3fc-2c963f66afb2",
  "tenantId": "default",
  "title": "UPI Banking FAQs",
  "category": "banking",
  "filename": "upi_faq.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 524288,
  "status": "COMPLETED",
  "errorMessage": null,
  "createdAt": "2026-08-22T10:00:00Z",
  "updatedAt": "2026-08-22T10:00:05Z"
}
```

---

### 3.6. Delete Document

Removes the document metadata and its vector embeddings from pgvector.

* **Method**: `DELETE`
* **URL**: `/api/v1/documents/{id}`
* **Headers**: `X-Tenant-Id: default`
* **Response**: `204 No Content`

---

### 3.7. Conversation History Management

#### List Conversations
* **Method**: `GET`
* **URL**: `/api/v1/conversations`
* **Headers**: `X-Tenant-Id: default`

#### Get Conversation & Full Message History
* **Method**: `GET`
* **URL**: `/api/v1/conversations/{id}`
* **Headers**: `X-Tenant-Id: default`

#### Response Body (HTTP 200 OK)
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "title": "UPI transaction issue",
  "createdAt": "2026-08-22T10:00:00Z",
  "updatedAt": "2026-08-22T10:05:00Z",
  "messages": [
    {
      "id": "a1b2c3d4-0000-0000-0000-000000000001",
      "role": "USER",
      "content": "My UPI transaction failed but money was deducted.",
      "createdAt": "2026-08-22T10:00:00Z"
    },
    {
      "id": "a1b2c3d4-0000-0000-0000-000000000002",
      "role": "ASSISTANT",
      "content": "Your deducted funds will automatically reverse within 24 to 48 hours.",
      "createdAt": "2026-08-22T10:00:03Z"
    }
  ]
}
```

---

## 4. NLP Service Configuration & Toggle

The backend is configured to use the internal high-accuracy `MockNlpService` by default.

When your external Python NLP Microservice is deployed at `http://localhost:8000/api/nlp/analyze`:
1. Set `NLP_MOCK_ENABLED=false` in environment / properties.
2. The backend automatically switches to `HttpNlpService` and sends requests using the exact schema.
