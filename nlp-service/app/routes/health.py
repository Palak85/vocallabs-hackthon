"""
Health Check Route.
"""

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text
from app.schemas.nlp_schema import HealthCheckResponse
from app.database import get_db
from app.services.inference import pipeline

router = APIRouter(tags=["Health"])


@router.get("/api/health", response_model=HealthCheckResponse)
def health_check(db: Session = Depends(get_db)):
    db_status = "connected"
    try:
        db.execute(text("SELECT 1"))
    except Exception:
        db_status = "disconnected"

    model_loaded = bool(pipeline.domain_detector.model is not None)

    return HealthCheckResponse(
        status="healthy" if (db_status == "connected" and model_loaded) else "degraded",
        service="nlp-service",
        model_loaded=model_loaded,
        database=db_status
    )
