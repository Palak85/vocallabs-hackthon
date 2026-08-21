"""
NLP Analysis API Route.
"""

import uuid
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from datetime import datetime

from app.schemas.nlp_schema import NLPAnalyzeRequest, NLPAnalyzeResponse
from app.database import get_db
from app.models import Conversation, Message, NLPResult, Entity
from app.services.inference import pipeline
from app.utils.logger import get_logger

logger = get_logger("nlp_route")
router = APIRouter(prefix="/api/nlp", tags=["NLP"])


@router.post("/analyze", response_model=NLPAnalyzeResponse)
def analyze_message(req: NLPAnalyzeRequest, db: Session = Depends(get_db)):
    if not req.text.strip():
        raise HTTPException(status_code=400, detail="Customer text cannot be empty.")

    try:
        # Retrieve previous frustration scores from conversation history in DB
        prev_scores = []
        existing_conv = db.query(Conversation).filter(Conversation.conversation_id == req.conversation_id).first()
        if existing_conv:
            prev_results = (
                db.query(NLPResult)
                .join(Message, Message.message_id == NLPResult.message_id)
                .filter(Message.conversation_id == req.conversation_id)
                .order_by(Message.created_at.asc())
                .all()
            )
            prev_scores = [r.frustration_score for r in prev_results]

        # Execute NLP Inference Pipeline
        result = pipeline.analyze(
            raw_text=req.text,
            user_provided_lang=req.language,
            history_scores=prev_scores
        )

        nlp_data = result["nlp"]
        conv_data = result["conversation_analysis"]

        # Persist / Update Conversation
        if not existing_conv:
            conv_record = Conversation(
                conversation_id=req.conversation_id,
                customer_id=req.customer_id or "anonymous",
                started_at=datetime.utcnow(),
                last_message_at=datetime.utcnow()
            )
            db.add(conv_record)
        else:
            existing_conv.last_message_at = datetime.utcnow()

        # Check for existing message_id to support idempotent calls
        existing_msg = db.query(Message).filter(Message.message_id == req.message_id).first()
        if not existing_msg:
            msg_record = Message(
                conversation_id=req.conversation_id,
                message_id=req.message_id,
                sender="customer",
                text=req.text,
                timestamp=datetime.utcnow()
            )
            db.add(msg_record)

            nlp_record = NLPResult(
                message_id=req.message_id,
                language=nlp_data["language"]["label"],
                language_confidence=nlp_data["language"]["confidence"],
                domain=nlp_data["domain"]["label"],
                domain_confidence=nlp_data["domain"]["confidence"],
                intent=nlp_data["intent"]["label"],
                intent_confidence=nlp_data["intent"]["confidence"],
                sentiment=nlp_data["sentiment"]["label"],
                sentiment_confidence=nlp_data["sentiment"]["confidence"],
                emotion=nlp_data["emotion"]["label"],
                emotion_confidence=nlp_data["emotion"]["confidence"],
                frustration_score=nlp_data["frustration"]["score"],
                frustration_level=nlp_data["frustration"]["level"],
                urgency=nlp_data["urgency"]["level"],
                urgency_confidence=nlp_data["urgency"]["confidence"],
                frustration_trend=conv_data["frustration_trend"]
            )
            db.add(nlp_record)

            for ent in nlp_data["entities"]:
                ent_record = Entity(
                    message_id=req.message_id,
                    entity_type=ent["type"],
                    entity_value=ent["value"],
                    confidence=ent["confidence"]
                )
                db.add(ent_record)

        db.commit()

        return NLPAnalyzeResponse(
            success=True,
            conversation_id=req.conversation_id,
            message_id=req.message_id,
            nlp=nlp_data,
            conversation_analysis=conv_data
        )

    except Exception as e:
        db.rollback()
        logger.error(f"Error processing NLP request: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Internal NLP processing error: {str(e)}")
