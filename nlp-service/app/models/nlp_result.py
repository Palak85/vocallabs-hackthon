"""
SQLAlchemy NLP Result Model.
"""

import uuid
from datetime import datetime
from sqlalchemy import Column, String, Integer, Float, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from app.database import Base


class NLPResult(Base):
    __tablename__ = "nlp_results"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    message_id = Column(String(100), ForeignKey("messages.message_id", ondelete="CASCADE"), unique=True, index=True, nullable=False)

    language = Column(String(50), nullable=False)
    language_confidence = Column(Float, nullable=False)

    domain = Column(String(50), nullable=False)
    domain_confidence = Column(Float, nullable=False)

    intent = Column(String(100), nullable=False)
    intent_confidence = Column(Float, nullable=False)

    sentiment = Column(String(30), nullable=False)
    sentiment_confidence = Column(Float, nullable=False)

    emotion = Column(String(30), nullable=False)
    emotion_confidence = Column(Float, nullable=False)

    frustration_score = Column(Integer, nullable=False)
    frustration_level = Column(String(20), nullable=False)

    urgency = Column(String(20), nullable=False)
    urgency_confidence = Column(Float, nullable=False)

    frustration_trend = Column(String(30), nullable=False)

    created_at = Column(DateTime, default=datetime.utcnow)

    message = relationship("Message", back_populates="nlp_result")
