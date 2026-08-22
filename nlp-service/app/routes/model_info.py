"""
Model Info Endpoint.
"""

from fastapi import APIRouter
from app.schemas.nlp_schema import ModelInfoResponse
from app.utils.thresholds import SUPPORTED_DOMAINS

router = APIRouter(tags=["Model Info"])


@router.get("/api/model-info", response_model=ModelInfoResponse)
@router.get("/api/models", response_model=ModelInfoResponse)
def get_model_info():
    return ModelInfoResponse(
        service="nlp-service",
        version="1.0.0",
        models={
            "domain": "TFIDF-LogisticRegression-v1",
            "intent": "Hierarchical-Domain-Classifiers-v1",
            "sentiment": "TFIDF-LogisticRegression-v1",
            "emotion": "TFIDF-LogisticRegression-v1",
            "frustration": "Calibrated-MultiFactor-Engine-v1",
            "urgency": "TFIDF-LogisticRegression-Heuristic-v1",
            "language": "TFIDF-Ngram-v1"
        },
        supported_domains=SUPPORTED_DOMAINS,
        device="cpu"
    )
