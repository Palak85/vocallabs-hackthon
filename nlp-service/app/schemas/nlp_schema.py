"""
Pydantic Schemas for Request & Response validation.
Strictly adheres to integration contracts without human escalation decision fields.
"""

from typing import List, Optional, Any, Dict
from pydantic import BaseModel, Field


class NLPAnalyzeRequest(BaseModel):
    conversation_id: Optional[str] = Field(None, description="Unique conversation identifier")
    customer_id: Optional[str] = Field(None, description="Optional customer identifier")
    message_id: Optional[str] = Field(None, description="Unique message identifier")
    timestamp: Optional[str] = Field(None, description="ISO-8601 message timestamp")
    text: str = Field(..., description="Raw customer message text", min_length=1)
    language: Optional[str] = Field(None, description="Optional upstream language code")


class LabelConfidence(BaseModel):
    label: str
    confidence: float


class FrustrationOutput(BaseModel):
    score: int
    level: str


class UrgencyOutput(BaseModel):
    level: str
    confidence: float


class EntityOutput(BaseModel):
    type: str
    value: str
    confidence: float


class NLPOutput(BaseModel):
    language: LabelConfidence
    domain: LabelConfidence
    intent: LabelConfidence
    sentiment: LabelConfidence
    emotion: LabelConfidence
    frustration: FrustrationOutput
    urgency: UrgencyOutput
    entities: List[EntityOutput]


class ConversationAnalysisOutput(BaseModel):
    frustration_trend: str
    previous_frustration_score: Optional[int] = None
    current_frustration_score: int


class NLPAnalyzeResponse(BaseModel):
    success: bool
    conversation_id: str
    message_id: str
    nlp: NLPOutput
    conversation_analysis: ConversationAnalysisOutput


class HealthCheckResponse(BaseModel):
    status: str
    service: str
    model_loaded: bool
    database: str


class ModelInfoResponse(BaseModel):
    service: str
    version: str
    models: Dict[str, str]
    supported_domains: List[str]
    device: str
