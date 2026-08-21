"""
Language Detection Service.
"""

import os
import joblib
from typing import Dict, Any
from app.utils.thresholds import LANGUAGE_CONFIDENCE_THRESHOLD, FALLBACK_LANGUAGE

MODEL_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "models", "language", "language_model.joblib"))


class LanguageDetector:
    def __init__(self, model_path: str = MODEL_PATH):
        self.model = None
        if os.path.exists(model_path):
            self.model = joblib.load(model_path)

    def predict(self, text: str) -> Dict[str, Any]:
        if not text or not self.model:
            return {"label": FALLBACK_LANGUAGE, "confidence": 0.0}

        probs = self.model.predict_proba([text])[0]
        max_idx = probs.argmax()
        confidence = float(probs[max_idx])
        label = str(self.model.classes_[max_idx])

        if confidence < LANGUAGE_CONFIDENCE_THRESHOLD:
            return {"label": FALLBACK_LANGUAGE, "confidence": round(confidence, 2)}

        return {"label": label, "confidence": round(confidence, 2)}
