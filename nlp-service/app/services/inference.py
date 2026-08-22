"""
Unified NLP Inference Pipeline.
Loads all ML models into memory once at application lifespan initialization
and executes the complete NLP analysis sequentially.
"""

import time
from typing import Dict, Any, List
from app.services.preprocessing import preprocessor
from app.services.language import LanguageDetector
from app.services.domain import DomainDetector
from app.services.intent import HierarchicalIntentClassifier
from app.services.sentiment import SentimentAnalyzer
from app.services.emotion import EmotionDetector
from app.services.frustration import frustration_analyzer
from app.services.urgency import UrgencyDetector
from app.services.ner import entity_extractor
from app.services.conversation import conversation_analyzer
from app.services.risk import risk_detector
from app.utils.logger import get_logger

logger = get_logger("nlp_inference")


class NLPInferencePipeline:
    def __init__(self):
        logger.info("Initializing NLP Inference Pipeline and loading models...")
        self.language_detector = LanguageDetector()
        self.domain_detector = DomainDetector()
        self.intent_classifier = HierarchicalIntentClassifier()
        self.sentiment_analyzer = SentimentAnalyzer()
        self.emotion_detector = EmotionDetector()
        self.urgency_detector = UrgencyDetector()
        logger.info("All NLP models loaded successfully into memory.")

    def analyze(
        self,
        raw_text: str,
        user_provided_lang: str = None,
        history_scores: List[int] = None
    ) -> Dict[str, Any]:
        """Runs end-to-end NLP pipeline and returns structured intelligence."""
        start_time = time.perf_counter()

        # 1. Preprocessing
        cleaned_text, signals = preprocessor.preprocess(raw_text)

        # 2. Language Detection
        if user_provided_lang:
            lang_result = {"label": user_provided_lang, "confidence": 1.0}
        else:
            lang_result = self.language_detector.predict(cleaned_text)

        # 3. Domain Detection
        domain_result = self.domain_detector.predict(cleaned_text)
        detected_domain = domain_result["label"]

        # 4. Intent Classification
        intent_result = self.intent_classifier.predict(cleaned_text, detected_domain)

        # 5. Sentiment Analysis
        sentiment_result = self.sentiment_analyzer.predict(cleaned_text)

        # 6. Emotion Detection
        emotion_result = self.emotion_detector.predict(cleaned_text)

        # 7. Frustration Scoring
        frustration_result = frustration_analyzer.analyze(
            raw_text=raw_text,
            signals=signals,
            sentiment_label=sentiment_result["label"],
            emotion_label=emotion_result["label"],
            emotion_conf=emotion_result["confidence"]
        )

        # 8. Urgency Detection
        urgency_result = self.urgency_detector.predict(cleaned_text)

        # 9. Hybrid NER Entity Extraction
        entities = entity_extractor.extract(raw_text, detected_domain)

        # 10. Conversation Trend Analysis
        conv_analysis = conversation_analyzer.compute_trend(
            current_score=frustration_result["score"],
            history_scores=history_scores or []
        )
        trend = conv_analysis["frustration_trend"]
        frustration_result["trend"] = trend
        urgency_result["trend"] = trend

        # 11. Risk & Escalation Signal Detection
        risk_signals = risk_detector.detect_risk_signals(raw_text, detected_domain)
        escalation_signals = risk_detector.detect_escalation_signals(
            domain=detected_domain,
            urgency_level=urgency_result["level"],
            frustration_score=frustration_result["score"],
            risk_signals=risk_signals
        )
        recommended_status = risk_detector.recommend_status(
            escalation_signals=escalation_signals,
            urgency_level=urgency_result["level"],
            frustration_level=frustration_result["level"]
        )

        # 12. Composite NLP Confidence
        dom_conf = domain_result.get("confidence", 0.70)
        int_conf = intent_result.get("confidence", 0.70)
        composite_conf = int(min(98, max(45, (dom_conf * 0.5 + int_conf * 0.5) * 100)))
        clarification_required = composite_conf < 60 or intent_result.get("label") == "other"

        elapsed_ms = (time.perf_counter() - start_time) * 1000.0

        return {
            "nlp": {
                "language": lang_result,
                "domain": domain_result,
                "intent": intent_result,
                "sentiment": sentiment_result,
                "emotion": emotion_result,
                "frustration": frustration_result,
                "urgency": urgency_result,
                "entities": entities,
                "nlp_confidence": composite_conf,
                "risk_signals": risk_signals,
                "clarification_required": clarification_required,
                "escalation_signals": escalation_signals,
                "recommended_status": recommended_status
            },
            "conversation_analysis": conv_analysis,
            "latency_ms": round(elapsed_ms, 2)
        }


# Global pipeline instance
pipeline = NLPInferencePipeline()
