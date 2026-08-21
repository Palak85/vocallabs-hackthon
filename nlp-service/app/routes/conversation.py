"""
Conversation History and Inspection Routes.
"""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Dict, Any

from app.database import get_db
from app.models import Conversation, Message, NLPResult, Entity

router = APIRouter(prefix="/api/conversations", tags=["Conversations"])


@router.get("/{conversation_id}")
def get_conversation_history(conversation_id: str, db: Session = Depends(get_db)):
    conv = db.query(Conversation).filter(Conversation.conversation_id == conversation_id).first()
    if not conv:
        raise HTTPException(status_code=404, detail="Conversation not found")

    messages = (
        db.query(Message)
        .filter(Message.conversation_id == conversation_id)
        .order_by(Message.timestamp.asc())
        .all()
    )

    history = []
    for msg in messages:
        nlp_res = msg.nlp_result
        ents = [{"type": e.entity_type, "value": e.entity_value, "confidence": e.confidence} for e in msg.entities]
        history.append({
            "message_id": msg.message_id,
            "text": msg.text,
            "timestamp": msg.timestamp.isoformat() if msg.timestamp else None,
            "nlp": {
                "language": nlp_res.language if nlp_res else None,
                "domain": nlp_res.domain if nlp_res else None,
                "intent": nlp_res.intent if nlp_res else None,
                "sentiment": nlp_res.sentiment if nlp_res else None,
                "emotion": nlp_res.emotion if nlp_res else None,
                "frustration_score": nlp_res.frustration_score if nlp_res else None,
                "frustration_level": nlp_res.frustration_level if nlp_res else None,
                "urgency": nlp_res.urgency if nlp_res else None,
                "frustration_trend": nlp_res.frustration_trend if nlp_res else None,
                "entities": ents
            }
        })

    return {
        "conversation_id": conversation_id,
        "customer_id": conv.customer_id,
        "started_at": conv.started_at.isoformat() if conv.started_at else None,
        "last_message_at": conv.last_message_at.isoformat() if conv.last_message_at else None,
        "total_messages": len(history),
        "messages": history
    }


@router.get("/{conversation_id}/latest")
def get_latest_conversation_signal(conversation_id: str, db: Session = Depends(get_db)):
    conv = db.query(Conversation).filter(Conversation.conversation_id == conversation_id).first()
    if not conv:
        raise HTTPException(status_code=404, detail="Conversation not found")

    latest_msg = (
        db.query(Message)
        .filter(Message.conversation_id == conversation_id)
        .order_by(Message.timestamp.desc())
        .first()
    )

    if not latest_msg or not latest_msg.nlp_result:
        raise HTTPException(status_code=404, detail="No NLP results found for this conversation")

    nlp_res = latest_msg.nlp_result
    return {
        "conversation_id": conversation_id,
        "latest_message_id": latest_msg.message_id,
        "domain": nlp_res.domain,
        "intent": nlp_res.intent,
        "frustration_score": nlp_res.frustration_score,
        "frustration_level": nlp_res.frustration_level,
        "frustration_trend": nlp_res.frustration_trend,
        "urgency": nlp_res.urgency
    }
