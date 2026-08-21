"""
Hierarchical Intent Classification Service.
"""

import os
import joblib
from typing import Dict, Any
from app.utils.thresholds import INTENT_CONFIDENCE_THRESHOLD, FALLBACK_INTENT

INTENTS_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "models", "intent"))


class HierarchicalIntentClassifier:
    def __init__(self, models_dir: str = INTENTS_DIR):
        self.models: Dict[str, Any] = {}
        if os.path.exists(models_dir):
            for fname in os.listdir(models_dir):
                if fname.endswith(".joblib"):
                    domain_name = fname.replace("intent_", "").replace(".joblib", "")
                    self.models[domain_name] = joblib.load(os.path.join(models_dir, fname))

    def predict(self, text: str, domain: str) -> Dict[str, Any]:
        if not text:
            return {"label": FALLBACK_INTENT, "confidence": 0.0}

        if domain not in self.models:
            return {"label": "general_query", "confidence": 0.50}

        model = self.models[domain]
        probs = model.predict_proba([text])[0]
        max_idx = probs.argmax()
        confidence = float(probs[max_idx])
        label = str(model.classes_[max_idx])

        if confidence < INTENT_CONFIDENCE_THRESHOLD:
            return {"label": FALLBACK_INTENT, "confidence": round(confidence, 2)}

        return {"label": label, "confidence": round(confidence, 2)}
