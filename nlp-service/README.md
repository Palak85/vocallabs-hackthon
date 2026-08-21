# Multi-Domain Customer Support NLP Microservice

> **AI-Powered Multi-Domain Customer Support NLP Microservice with Semantic Domain Detection and Intelligent Conversation Analysis**

---

## 📌 Architecture & Scope Overview

```
                         CUSTOMER
                            │
                     Voice / Chat
                            │
                            ▼
                     Speech-to-Text
                            │
                            ▼
              ┌─────────────────────────┐
              │     NLP MICROSERVICE    │
              │       (OUR MODULE)      │
              └─────────────────────────┘
                            │
                            ▼
                  Text Preprocessing
                            │
                            ▼
                  Language Detection (en, hi, hinglish, unknown)
                            │
                            ▼
                  Semantic Domain Detection (7 Domains + Unknown)
                            │
                            ▼
                  Hierarchical Intent Classification (15 per domain)
                            │
             ┌──────────────┼──────────────┐
             ▼              ▼              ▼
        Sentiment        Emotion       Frustration (0-100 Score & Level)
             │              │              │
             └──────────────┼──────────────┘
                            │
                            ▼
                    Urgency Detection (low, medium, high, critical)
                            │
                            ▼
                    Hybrid NER (Regex IDs + Context Entities)
                            │
                            ▼
              Conversation Trajectory Analysis (stable, increasing, rapidly_increasing)
                            │
                            ▼
                   Structured JSON Signals
                       /        \
                      /          \
                     ▼            ▼
              PostgreSQL        FastAPI
                                  │
                                  ▼
                              AI AGENT (Other Team)
                                  │
                       ┌──────────┴──────────┐
                       ▼                     ▼
                      RAG                   APIs
                       │                     │
                       └──────────┬──────────┘
                                  │
                                  ▼
                            AI RESPONSE
                                  │
                                  ▼
                         CONTINUOUS MONITOR (Other Team)
                                  │
                         ┌────────┴────────┐
                         ▼                 ▼
                    AI CONTINUES     HUMAN ESCALATION
```

> [!IMPORTANT]
> **Strict Responsibility Boundary**: This NLP service produces raw and derived semantic, emotional, behavioral, and conversational intelligence. It **never** returns `human_escalation: true/false`. The final escalation decision is made downstream by the Continuous Monitor.

---

## 🚀 Key Features

1. **Brand-Agnostic Semantic Domain Detection**: Detects domain based on semantic context rather than memorizing brand names.
2. **Hierarchical Intent Classification**: Routes messages to domain-specific models across 7 business domains (15 intents per domain).
3. **Calibrated Frustration Engine**: Multi-factor scoring (0-100) factoring in repeated complaint keywords, uppercase ratios, exclamation marks, and emotional intensity.
4. **Conversation Trajectory Tracker**: Evaluates previous turns stored in PostgreSQL to calculate trajectory (`stable`, `decreasing`, `increasing`, `rapidly_increasing`).
5. **Hinglish & Indian English Support**: First-class support for romanized Hindi and code-mixed inputs.
6. **Sub-3ms Real-Time Latency on CPU**: Measured average CPU inference latency of **2.47 ms** per request.
7. **Hybrid NER Extraction**: Zero-latency regex extraction for structured IDs (`ORD-*`, `CLM-*`, `TXN-*`, `POL-*`, `PNR-*`) + context entities.

---

## 📊 Measured Performance & Benchmarks (Actual on CPU)

```text
Total benchmark runs : 100
Mean Latency         : 2.47 ms
Median Latency       : 2.40 ms
95th Percentile (p95): 3.26 ms
Min Latency          : 1.91 ms
Max Latency          : 3.59 ms
```

---

## 📡 API Contract

### Main Inference Endpoint: `POST /api/nlp/analyze`

#### Request Payload:
```json
{
  "conversation_id": "conv_001",
  "customer_id": "cust_001",
  "message_id": "msg_001",
  "timestamp": "2026-08-21T16:30:15Z",
  "text": "I paid my school fees two days ago but the portal still says unpaid.",
  "language": null
}
```

#### Response Payload:
```json
{
  "success": true,
  "conversation_id": "conv_001",
  "message_id": "msg_001",
  "nlp": {
    "language": {
      "label": "en",
      "confidence": 0.99
    },
    "domain": {
      "label": "education",
      "confidence": 0.96
    },
    "intent": {
      "label": "fee_payment_not_updated",
      "confidence": 0.94
    },
    "sentiment": {
      "label": "negative",
      "confidence": 0.91
    },
    "emotion": {
      "label": "concerned",
      "confidence": 0.88
    },
    "frustration": {
      "score": 54,
      "level": "medium"
    },
    "urgency": {
      "level": "medium",
      "confidence": 0.82
    },
    "entities": [
      {
        "type": "fee_type",
        "value": "school_fee",
        "confidence": 0.94
      }
    ]
  },
  "conversation_analysis": {
    "frustration_trend": "stable",
    "previous_frustration_score": null,
    "current_frustration_score": 54
  }
}
```

---

## 🛠️ Quick Start & Local Execution

### 1. Install Dependencies
```bash
pip install -r requirements.txt
```

### 2. Generate Datasets & Train Models
```bash
python training/create_datasets.py
python training/validate_dataset.py
python training/train_all.py
```

### 3. Run Test Suite
```bash
python -m pytest -v
```

### 4. Run Hackathon Demonstrations
```bash
python demo_runner.py
```

### 5. Launch FastAPI Microservice
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

---

## 🐳 Docker Deployment

To launch the microservice with PostgreSQL:
```bash
docker-compose up --build
```
Access the interactive OpenAPI Swagger docs at `http://localhost:8000/docs`.
