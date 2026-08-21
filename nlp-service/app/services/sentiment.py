"""
Sentiment Analysis Service.
"""

import os
import joblib
from typing import Dict, Any
from app.utils.thresholds import SENTIMENT_CONFIDENCE_THRESHOLD

MODEL_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "models", "sentiment", "sentiment_model.joblib"))


class SentimentAnalyzer:
    def __init__(self, model_path: str = MODEL_PATH):
        self.model = None
        if os.path.exists(model_path):
            self.model = joblib.load(model_path)

    def predict(self, text: str) -> Dict[str, Any]:
        if not text or not self.model:
            return {"label": "neutral", "confidence": 0.50}

        probs = self.model.predict_proba([text])[0]
        max_idx = probs.argmax()
        confidence = float(probs[max_idx])
        label = str(self.model.classes_[max_idx])

        return {"label": label, "confidence": round(confidence, 2)}
