import uuid
import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.database import init_db

init_db()
client = TestClient(app)


def test_health_endpoint():
    response = client.get("/api/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] in ["healthy", "degraded"]
    assert data["service"] == "nlp-service"


def test_model_info_endpoint():
    response = client.get("/api/model-info")
    assert response.status_code == 200
    data = response.json()
    assert "ecommerce" in data["supported_domains"]


def test_nlp_analyze_contract():
    conv_id = f"test_conv_{uuid.uuid4().hex[:8]}"
    msg_id = f"msg_{uuid.uuid4().hex[:8]}"

    payload = {
        "conversation_id": conv_id,
        "customer_id": "cust_123",
        "message_id": msg_id,
        "timestamp": "2026-08-21T16:30:15Z",
        "text": "I paid my school fees two days ago but the portal still says unpaid."
    }
    response = client.post("/api/nlp/analyze", json=payload)
    assert response.status_code == 200
    data = response.json()

    # Verify Response Contract
    assert data["success"] is True
    assert data["conversation_id"] == conv_id
    assert data["message_id"] == msg_id
    assert "nlp" in data
    assert "language" in data["nlp"]
    assert "domain" in data["nlp"]
    assert "intent" in data["nlp"]
    assert "sentiment" in data["nlp"]
    assert "emotion" in data["nlp"]
    assert "frustration" in data["nlp"]
    assert "urgency" in data["nlp"]
    assert "entities" in data["nlp"]
    assert "conversation_analysis" in data
    assert "frustration_trend" in data["conversation_analysis"]

    # CRITICAL CONTRACT CHECK: MUST NOT HAVE ESCALATION DECISION FIELDS
    assert "human_escalation" not in data
    assert "escalate_to_human" not in data
    assert "ai_continue" not in data


def test_conversation_history_endpoints():
    conv_id = f"test_conv_history_{uuid.uuid4().hex[:8]}"
    msg1_id = f"msg_{uuid.uuid4().hex[:8]}"
    msg2_id = f"msg_{uuid.uuid4().hex[:8]}"

    # Message 1
    client.post("/api/nlp/analyze", json={
        "conversation_id": conv_id,
        "customer_id": "cust_123",
        "message_id": msg1_id,
        "timestamp": "2026-08-21T16:30:15Z",
        "text": "Where is my order?"
    })

    # Message 2 (escalation tone)
    resp2 = client.post("/api/nlp/analyze", json={
        "conversation_id": conv_id,
        "customer_id": "cust_123",
        "message_id": msg2_id,
        "timestamp": "2026-08-21T16:35:15Z",
        "text": "THIS IS RIDICULOUS! I called four times and nobody is helping me!"
    })
    assert resp2.status_code == 200
    data2 = resp2.json()
    assert data2["conversation_analysis"]["frustration_trend"] in ["increasing", "rapidly_increasing", "stable"]

    # Query full conversation history
    hist_resp = client.get(f"/api/conversations/{conv_id}")
    assert hist_resp.status_code == 200
    hist_data = hist_resp.json()
    assert hist_data["total_messages"] == 2

    # Query latest endpoint
    latest_resp = client.get(f"/api/conversations/{conv_id}/latest")
    assert latest_resp.status_code == 200
    latest_data = latest_resp.json()
    assert latest_data["latest_message_id"] == msg2_id
