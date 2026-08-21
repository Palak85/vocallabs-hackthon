"""
Semantic Domain Detection Service.
"""

import os
import joblib
from typing import Dict, Any
from app.utils.thresholds import DOMAIN_CONFIDENCE_THRESHOLD, FALLBACK_DOMAIN, SUPPORTED_DOMAINS

MODEL_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "models", "domain", "domain_model.joblib"))


class DomainDetector:
    def __init__(self, model_path: str = MODEL_PATH):
        self.model = None
        if os.path.exists(model_path):
            self.model = joblib.load(model_path)

    def predict(self, text: str) -> Dict[str, Any]:
        if not text or not self.model:
            return {"label": FALLBACK_DOMAIN, "confidence": 0.0}

        probs = self.model.predict_proba([text])[0]
        max_idx = probs.argmax()
        confidence = float(probs[max_idx])
        label = str(self.model.classes_[max_idx])

        # If probability is below threshold or domain not in supported list
        if confidence < DOMAIN_CONFIDENCE_THRESHOLD or label not in SUPPORTED_DOMAINS:
            return {"label": FALLBACK_DOMAIN, "confidence": round(confidence, 2)}

        return {"label": label, "confidence": round(confidence, 2)}
