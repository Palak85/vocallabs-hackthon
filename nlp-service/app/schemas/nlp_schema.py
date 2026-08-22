"""
Pydantic Schemas for Request & Response validation.
Adheres strictly to the NLP intelligence layer specifications.
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
    trend: Optional[str] = "unknown"


class UrgencyOutput(BaseModel):
    level: str
    confidence: float
    trend: Optional[str] = "unknown"


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
    nlp_confidence: int = Field(85, description="Composite NLP confidence score 0-100")
    risk_signals: List[str] = Field(default_factory=list, description="Extracted financial or medical risk indicators")
    clarification_required: bool = Field(False, description="True if ambiguity or low confidence warrants clarification")
    escalation_signals: List[str] = Field(default_factory=list, description="Escalation indicators forwarded to Decision Engine")
    recommended_status: str = Field("IN_PROGRESS", description="Operational recommended case status")


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
