"""
NLP Analysis API Route.
Handles incoming customer messages, executes inference, logs metrics,
and persists conversations, messages, and entities to the database.
"""

import uuid
from datetime import datetime, timezone
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.schemas.nlp_schema import NLPAnalyzeRequest, NLPAnalyzeResponse
from app.database import get_db
from app.models import Conversation, Message, NLPResult, Entity
from app.services.inference import pipeline
from app.utils.logger import get_logger

logger = get_logger("nlp_route")
router = APIRouter(prefix="/api/nlp", tags=["NLP"])


@router.post("/analyze", response_model=NLPAnalyzeResponse)
def analyze_message(req: NLPAnalyzeRequest, db: Session = Depends(get_db)):
    if not req.text or not req.text.strip():
        raise HTTPException(status_code=400, detail="Customer text cannot be empty.")

    conv_id = req.conversation_id.strip() if req.conversation_id and req.conversation_id.strip() else f"conv_{uuid.uuid4().hex[:10]}"
    msg_id = req.message_id.strip() if req.message_id and req.message_id.strip() else f"msg_{uuid.uuid4().hex[:10]}"

    try:
        # Retrieve previous frustration scores from conversation history in DB
        prev_scores = []
        existing_conv = db.query(Conversation).filter(Conversation.conversation_id == conv_id).first()
        if existing_conv:
            prev_results = (
                db.query(NLPResult)
                .join(Message, Message.message_id == NLPResult.message_id)
                .filter(Message.conversation_id == conv_id)
                .order_by(Message.created_at.asc())
                .all()
            )
            prev_scores = [r.frustration_score for r in prev_results if r.frustration_score is not None]

        # Execute NLP Inference Pipeline
        result = pipeline.analyze(
            raw_text=req.text.strip(),
            user_provided_lang=req.language,
            history_scores=prev_scores
        )

        nlp_data = result.get("nlp", {})
        conv_data = result.get("conversation_analysis", {})
        now = datetime.now(timezone.utc)

        # Persist / Update Conversation
        if not existing_conv:
            conv_record = Conversation(
                conversation_id=conv_id,
                customer_id=req.customer_id.strip() if req.customer_id and req.customer_id.strip() else "anonymous",
                started_at=now,
                last_message_at=now
            )
            db.add(conv_record)
        else:
            existing_conv.last_message_at = now

        # Check for existing message_id to support idempotent calls
        existing_msg = db.query(Message).filter(Message.message_id == msg_id).first()
        if not existing_msg:
            msg_record = Message(
                conversation_id=conv_id,
                message_id=msg_id,
                sender="customer",
                text=req.text.strip(),
                timestamp=now,
                created_at=now
            )
            db.add(msg_record)

            nlp_record = NLPResult(
                message_id=msg_id,
                language=nlp_data.get("language", {}).get("label", "en"),
                language_confidence=float(nlp_data.get("language", {}).get("confidence", 0.95)),
                domain=nlp_data.get("domain", {}).get("label", "unknown"),
                domain_confidence=float(nlp_data.get("domain", {}).get("confidence", 0.95)),
                intent=nlp_data.get("intent", {}).get("label", "other"),
                intent_confidence=float(nlp_data.get("intent", {}).get("confidence", 0.95)),
                sentiment=nlp_data.get("sentiment", {}).get("label", "neutral"),
                sentiment_confidence=float(nlp_data.get("sentiment", {}).get("confidence", 0.95)),
                emotion=nlp_data.get("emotion", {}).get("label", "neutral"),
                emotion_confidence=float(nlp_data.get("emotion", {}).get("confidence", 0.95)),
                frustration_score=int(nlp_data.get("frustration", {}).get("score", 20)),
                frustration_level=nlp_data.get("frustration", {}).get("level", "low"),
                urgency=nlp_data.get("urgency", {}).get("level", "low"),
                urgency_confidence=float(nlp_data.get("urgency", {}).get("confidence", 0.95)),
                frustration_trend=conv_data.get("frustration_trend", "stable"),
                created_at=now
            )
            db.add(nlp_record)

            for ent in nlp_data.get("entities", []):
                ent_record = Entity(
                    message_id=msg_id,
                    entity_type=ent.get("type", "generic"),
                    entity_value=str(ent.get("value", "")),
                    confidence=float(ent.get("confidence", 0.95)),
                    created_at=now
                )
                db.add(ent_record)

        db.commit()

        return NLPAnalyzeResponse(
            success=True,
            conversation_id=conv_id,
            message_id=msg_id,
            nlp=nlp_data,
            conversation_analysis=conv_data
        )

    except HTTPException:
        db.rollback()
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"Error processing NLP request: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Internal NLP processing error: {str(e)}")
