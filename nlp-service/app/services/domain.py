"""
Semantic Domain Detection Service.
Combines ML probability distribution with domain anchor disambiguation for complex cross-domain queries.
"""

import os
import re
import joblib
from typing import Dict, Any
from app.utils.thresholds import DOMAIN_CONFIDENCE_THRESHOLD, FALLBACK_DOMAIN, SUPPORTED_DOMAINS

MODEL_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "models", "domain", "domain_model.joblib"))

DOMAIN_ANCHORS = {
    "telecom": re.compile(r"\b(?:recharge|telecom|data\s+benefit|sms\s+benefit|calling\s+benefit|validity|mobile\s+plan|prepaid\s+plan|postpaid|sim\s+card|esim|telecom\s+account|mobile\s+number|network\s+pack)\b", re.IGNORECASE),
    "healthcare": re.compile(r"\b(?:appointment|dr\.?\s+|doctor|consultation|prescription|clinic|physician|opd|patient\s+portal)\b", re.IGNORECASE),
    "travel": re.compile(r"\b(?:flight|airline|pnr|boarding|flight\s+number|ticket\s+fare|airport|flight\s+cancellation)\b", re.IGNORECASE),
    "ecommerce": re.compile(r"\b(?:order\s+id|order\s+tracking|courier|shipment|parcel|delivered|package|delivery\s+delay)\b", re.IGNORECASE),
    "insurance": re.compile(r"\b(?:insurance|policy\s+number|claim\s+number|policy\s+expiry|premium\s+due|cashless|underwriting)\b", re.IGNORECASE),
    "education": re.compile(r"\b(?:school\s+fee|tuition\s+fee|college\s+fee|semester\s+fee|student\s+id|admission|scholarship|curriculum)\b", re.IGNORECASE),
}


class DomainDetector:
    def __init__(self, model_path: str = MODEL_PATH):
        self.model = None
        if os.path.exists(model_path):
            self.model = joblib.load(model_path)

    def predict(self, text: str) -> Dict[str, Any]:
        if not text or not self.model:
            return {"label": FALLBACK_DOMAIN, "confidence": 0.0}

        probs = self.model.predict_proba([text])[0]
        classes = list(self.model.classes_)

        # Build initial score map
        score_map = {cls: float(probs[i]) for i, cls in enumerate(classes)}

        # Apply anchor boost for explicit service-target mentions
        has_anchor = False
        for dom, pattern in DOMAIN_ANCHORS.items():
            matches = pattern.findall(text)
            if matches and dom in score_map:
                has_anchor = True
                score_map[dom] += len(matches) * 0.35

        best_domain = max(score_map, key=score_map.get)
        raw_prob = float(probs[classes.index(best_domain)]) if best_domain in classes else 0.5
        
        # If no anchor matched and raw model confidence is below threshold -> unknown fallback
        if not has_anchor and raw_prob < DOMAIN_CONFIDENCE_THRESHOLD:
            return {"label": FALLBACK_DOMAIN, "confidence": round(raw_prob, 2)}

        adjusted_confidence = min(0.95, max(raw_prob, 0.65 if has_anchor else raw_prob))

        if best_domain not in SUPPORTED_DOMAINS:
            return {"label": FALLBACK_DOMAIN, "confidence": round(raw_prob, 2)}

        return {"label": best_domain, "confidence": round(adjusted_confidence, 2)}
