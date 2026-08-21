"""
SQLAlchemy Entity Model.
"""

import uuid
from datetime import datetime
from sqlalchemy import Column, String, Text, Float, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from app.database import Base


class Entity(Base):
    __tablename__ = "entities"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    message_id = Column(String(100), ForeignKey("messages.message_id", ondelete="CASCADE"), index=True, nullable=False)
    entity_type = Column(String(50), nullable=False)
    entity_value = Column(Text, nullable=False)
    confidence = Column(Float, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    message = relationship("Message", back_populates="entities")
