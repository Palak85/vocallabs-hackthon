# NLP Microservice — Startup & Developer Guide

This guide provides step-by-step instructions to set up, train, run, and test the **NLP Microservice** for the AI-Powered Customer Support System.

---

## 📌 1. Overview

The **NLP Microservice** is a high-performance, real-time Natural Language Processing service built with **FastAPI** and **scikit-learn**. It provides multi-task text classification and entity extraction for customer support conversations:

* **Language Detection**: Identifies language code (e.g., `en`, `hi`, `es`, `fr`, `de`, `hinglish`).
* **Multi-Domain Intent Classification**: Hierarchical classification across domains (`ecommerce`, `banking`, `telecom`, `travel`, `tech_support`, `general`).
* **Sentiment & Emotion Analysis**: Sentiment polarity (`positive`, `neutral`, `negative`) and fine-grained emotions (`anger`, `frustration`, `sadness`, `joy`, `neutral`).
* **Frustration & Urgency Scoring**: Real-time frustration score (0–100), frustration level (`low`, `medium`, `high`, `critical`), and conversation frustration trend tracking across turns.
* **Hybrid Entity Extraction**: Regex-based and rule-based NER for order IDs, tracking numbers, transaction IDs, monetary amounts, phone numbers, emails, and dates.

---

## 🛠️ 2. Prerequisites

Ensure you have the following installed on your machine:

* **Python 3.10+** (Python 3.10, 3.11, or 3.12 recommended)
* **pip** (Python package installer)
* **Git**
* *(Optional)* **Docker & Docker Compose** (for containerized deployment)
* *(Optional)* **PostgreSQL** (defaults to lightweight embedded SQLite `nlp_local.db` if PostgreSQL is not configured)

---

## 🚀 3. Quick Start (Local Setup)

### 3.1. Navigate to the Service Directory

```bash
cd nlp-service
```

### 3.2. Create & Activate a Virtual Environment

**On Linux / macOS:**
```bash
python3 -m venv venv
source venv/bin/activate
```

**On Windows (PowerShell):**
```powershell
python -m venv venv
.\venv\Scripts\Activate.ps1
```

**On Windows (Command Prompt):**
```cmd
python -m venv venv
.\venv\Scripts\activate.bat
```

### 3.3. Install Dependencies

```bash
pip install --upgrade pip
pip install -r requirements.txt
```

---

## ⚙️ 4. Configuration & Environment Variables

Copy the example environment file:

```bash
cp .env.example .env
```

### Key Configuration Variables (`.env`)

| Variable | Default | Description |
| :--- | :--- | :--- |
| `DATABASE_URL` | `sqlite:///./nlp_local.db` | PostgreSQL connection string or local SQLite database. |
| `DOMAIN_CONFIDENCE_THRESHOLD` | `0.28` | Minimum threshold for domain classification. |
| `INTENT_CONFIDENCE_THRESHOLD` | `0.15` | Minimum threshold for intent classification. |
| `LANGUAGE_CONFIDENCE_THRESHOLD` | `0.40` | Minimum threshold for language identification. |
| `STORE_RAW_TEXT` | `true` | Whether to persist raw text in database records. |
| `LOG_LEVEL` | `INFO` | Logging level (`DEBUG`, `INFO`, `WARNING`, `ERROR`). |

> **Note**: If `DATABASE_URL` is omitted, the service automatically uses `sqlite:///./nlp_local.db`, requiring zero external database configuration for local testing.

---

## 🧠 5. Machine Learning Models

Pre-trained pipeline models are serialized and located in the `models/` directory:

```text
models/
├── domain/        # Domain classifier (TF-IDF + LogisticRegression / CalibratedClassifier)
├── intent/        # Intent classifier per domain
├── sentiment/     # Sentiment polarity model
├── emotion/       # Fine-grained emotion model
├── frustration/   # Frustration scoring model
├── urgency/       # Urgency scoring model
├── language/      # Language detector
└── model_evaluation_metrics.json
```

### Retraining / Evaluating Models (Optional)

If you modify datasets in `data/`, you can retrain and evaluate all models with:

```bash
# Train all classifiers
python training/train_all.py

# Evaluate models and generate metrics report
python training/evaluate_all.py
```

---

## ▶️ 6. Running the NLP Service

### 6.1. Start with Uvicorn (Hot-Reload Enabled)

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Or run via Python directly:

```bash
python -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

Once running:
* **Service Root**: [http://127.0.0.1:8000/](http://127.0.0.1:8000/)
* **Health Check**: [http://127.0.0.1:8000/api/health](http://127.0.0.1:8000/api/health)
* **Interactive Swagger Documentation**: [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)
* **ReDoc Documentation**: [http://127.0.0.1:8000/redoc](http://127.0.0.1:8000/redoc)

---

## 🐳 7. Running with Docker

### Using Docker Compose

```bash
# Start NLP service + Postgres
docker compose up -d

# View logs
docker compose logs -f nlp-service
```

### Build & Run Standalone Docker Container

```bash
docker build -t nlp-service .
docker run -p 8000:8000 --env-file .env nlp-service
```

---

## 📡 8. API Reference & Usage Examples

### 8.1. Health Check
* **Endpoint**: `GET /api/health`
* **Response**:
```json
{
  "status": "healthy",
  "service": "nlp-service",
  "model_loaded": true,
  "database": "connected"
}
```

---

### 8.2. Analyze Customer Message
* **Endpoint**: `POST /api/nlp/analyze`
* **Headers**: `Content-Type: application/json`

#### Request Body:
```json
{
  "conversation_id": "conv_12345",
  "customer_id": "cust_99",
  "message_id": "msg_001",
  "text": "My package order #ORD-98765 was supposed to arrive yesterday but I still haven't received it! Please help, I need it urgently."
}
```

#### Example `curl` command:
```bash
curl -X POST "http://127.0.0.1:8000/api/nlp/analyze" \
     -H "Content-Type: application/json" \
     -d '{
       "text": "My package order #ORD-98765 was supposed to arrive yesterday but I still haven'\''t received it! Please help, I need it urgently."
     }'
```

#### Response:
```json
{
  "success": true,
  "conversation_id": "conv_12345",
  "message_id": "msg_001",
  "nlp": {
    "language": {
      "label": "en",
      "confidence": 0.95
    },
    "domain": {
      "label": "ecommerce",
      "confidence": 0.89
    },
    "intent": {
      "label": "track_order",
      "confidence": 0.84
    },
    "sentiment": {
      "label": "negative",
      "confidence": 0.72
    },
    "emotion": {
      "label": "frustration",
      "confidence": 0.68
    },
    "frustration": {
      "score": 68,
      "level": "high"
    },
    "urgency": {
      "level": "high",
      "confidence": 0.85
    },
    "entities": [
      {
        "type": "order_id",
        "value": "ORD-98765",
        "confidence": 0.98
      },
      {
        "type": "date",
        "value": "yesterday",
        "confidence": 0.95
      }
    ]
  },
  "conversation_analysis": {
    "frustration_trend": "increasing",
    "previous_frustration_score": 45,
    "current_frustration_score": 68
  }
}
```

---

### 8.3. Model Metadata & Evaluation
* **Endpoint**: `GET /api/model-info` (or `GET /api/models`)
* **Response**: Returns metrics, training metadata, intent mappings, and accuracy scores for loaded models.

---

### 8.4. Conversation History & Frustration Tracking
* **Endpoint**: `GET /api/conversations/{conversation_id}`
* **Response**: Returns full message history, per-turn sentiment, and frustration progression over the conversation lifecycle.

---

## 🧪 9. Running Tests & Demo

### 9.1. Run Automated Unit and Integration Tests

```bash
pytest
```

### 9.2. Interactive Multi-turn CLI Demo

Test conversation tracking and dynamic escalation metrics via the interactive CLI demo:

```bash
python demo_runner.py
```

---

## ❓ 10. Troubleshooting & Common Issues

| Issue | Cause | Solution |
| :--- | :--- | :--- |
| **Port 8000 already in use** | Another process is using port 8000 | Specify a different port: `uvicorn app.main:app --port 8001` or terminate conflicting process. |
| **`ModuleNotFoundError`** | Virtual environment not activated or packages missing | Run `pip install -r requirements.txt` within active virtualenv. |
| **Database Connection Error** | PostgreSQL server not reachable | Ensure `.env` specifies a valid `DATABASE_URL` or remove it to use the built-in SQLite database (`nlp_local.db`). |
| **Model files missing in `models/`** | Repository cloned without model artifacts | Run `python training/train_all.py` to regenerate all model artifacts in `models/`. |
