"""
Urgency Detection Service.
Classifies urgency levels (low, medium, high, critical) with confidence scores.
Combines ML classifier with fast safety heuristics for life-safety/fraud emergencies.
"""

import os
import re
import joblib
from typing import Dict, Any

MODEL_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "models", "urgency", "urgency_model.joblib"))

EMERGENCY_CRITICAL_PATTERNS = [
    r"\b(?:emergency|ambulance|icu|life\s+threatening|oxygen|stolen\s+card|fraud\s+happening|hacking|breach)\b"
]

HIGH_URGENCY_PATTERNS = [
    r"\b(?:immediately|urgent|urgently|right\s+now|asap|within\s+\d+\s+hours?|flight\s+departs|portal\s+closes)\b"
]


class UrgencyDetector:
    def __init__(self, model_path: str = MODEL_PATH):
        self.model = None
        if os.path.exists(model_path):
            self.model = joblib.load(model_path)
        self.critical_regex = [re.compile(p, re.IGNORECASE) for p in EMERGENCY_CRITICAL_PATTERNS]
        self.high_regex = [re.compile(p, re.IGNORECASE) for p in HIGH_URGENCY_PATTERNS]

    def predict(self, text: str) -> Dict[str, Any]:
        if not text:
            return {"level": "low", "confidence": 0.50}

        # Check critical emergency override
        for rgx in self.critical_regex:
            if rgx.search(text):
                return {"level": "critical", "confidence": 0.98}

        # Check high urgency pattern
        for rgx in self.high_regex:
            if rgx.search(text):
                return {"level": "high", "confidence": 0.92}

        if not self.model:
            return {"level": "medium", "confidence": 0.50}

        probs = self.model.predict_proba([text])[0]
        max_idx = probs.argmax()
        confidence = float(probs[max_idx])
        level = str(self.model.classes_[max_idx])

        return {"level": level, "confidence": round(confidence, 2)}
