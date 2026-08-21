"""
SQLAlchemy Message Model.
"""

import uuid
from datetime import datetime
from sqlalchemy import Column, String, Text, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from app.database import Base


class Message(Base):
    __tablename__ = "messages"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    conversation_id = Column(String(100), ForeignKey("conversations.conversation_id", ondelete="CASCADE"), index=True, nullable=False)
    message_id = Column(String(100), unique=True, index=True, nullable=False)
    sender = Column(String(20), default="customer", nullable=False)
    text = Column(Text, nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow, index=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    conversation = relationship("Conversation", back_populates="messages")
    nlp_result = relationship("NLPResult", uselist=False, back_populates="message", cascade="all, delete-orphan")
    entities = relationship("Entity", back_populates="message", cascade="all, delete-orphan")
