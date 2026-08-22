"""
Frustration Scoring and Analysis Engine.
Estimates frustration score (0-100) and maps to categorical levels:
- low (0-29)
- medium (30-59)
- high (60-84)
- critical (85-100)

Distinguishes pure sentiment from active behavioral frustration by evaluating:
- Repetition cues ("3rd time", "contacted 4 times", "again and again", "baar baar", "nobody is helping")
- Escalation & legal threats ("police", "court", "consumer forum", "fraud", "cheating")
- Emotional intensity (exclamations, full capitalization, angry/frustrated emotion)
"""

import re
from typing import Dict, Any
from app.utils.thresholds import FRUSTRATION_LOW_MAX, FRUSTRATION_MEDIUM_MAX, FRUSTRATION_HIGH_MAX

REPETITION_PATTERNS = [
    r"\b(?:\d+|two|three|four|five|six|multiple)\s*(?:times|calls|attempts|tickets|days)\b",
    r"\b(?:again\s+and\s+again|repeatedly|baar\s+baar|har\s+baar|already\s+contacted)\b",
    r"\b(?:nobody\s+is\s+helping|no\s+one\s+cares|koi\s+help\s+nahi|zero\s+help)\b",
    r"\b(?:wasting\s+my\s+time|time\s+pass)\b"
]

CRITICAL_THREAT_PATTERNS = [
    r"\b(?:police|court|legal\s+action|consumer\s+court|consumer\s+forum|lawyer|sue|cheating|scam|fraud|loot)\b",
    r"\b(?:stop\s+giving\s+excuses|talk\s+to\s+manager|shut\s+down)\b",
    r"\b(?:extremely\s+frustrating|very\s+frustrated|completely\s+unacceptable|worst\s+experience|horrible\s+service)\b"
]


class FrustrationAnalyzer:
    def __init__(self):
        self.repetition_regex = [re.compile(p, re.IGNORECASE) for p in REPETITION_PATTERNS]
        self.critical_regex = [re.compile(p, re.IGNORECASE) for p in CRITICAL_THREAT_PATTERNS]

    def analyze(
        self,
        raw_text: str,
        signals: Dict[str, Any],
        sentiment_label: str,
        emotion_label: str,
        emotion_conf: float = 0.5
    ) -> Dict[str, Any]:
        """Calculates calibrated frustration score (0-100) and categorical level."""
        score = 0.0

        # Base score from sentiment
        if sentiment_label == "negative":
            score += 25.0
        elif sentiment_label == "neutral":
            score += 5.0
        elif sentiment_label == "positive":
            score += 0.0

        # Base score from emotion
        if emotion_label == "angry":
            score += 35.0 * max(emotion_conf, 0.5)
        elif emotion_label == "frustrated":
            score += 30.0 * max(emotion_conf, 0.5)
        elif emotion_label == "concerned":
            score += 15.0 * max(emotion_conf, 0.5)
        elif emotion_label == "sad":
            score += 10.0 * max(emotion_conf, 0.5)

        # Punctuation & capitalization cues
        exclamations = signals.get("exclamation_count", 0)
        score += min(exclamations * 5.0, 15.0)

        upper_ratio = signals.get("uppercase_ratio", 0.0)
        if upper_ratio > 0.4:
            score += 15.0
        elif upper_ratio > 0.2:
            score += 8.0

        # Repetition / follow-up attempts cues
        for rgx in self.repetition_regex:
            if rgx.search(raw_text):
                score += 30.0
                break

        # Escalation & Legal threat cues
        for rgx in self.critical_regex:
            if rgx.search(raw_text):
                score += 25.0
                break

        # Bound score between 0 and 100
        final_score = int(max(0, min(100, round(score))))

        # Map to categorical level
        if final_score <= FRUSTRATION_LOW_MAX:
            level = "low"
        elif final_score <= FRUSTRATION_MEDIUM_MAX:
            level = "medium"
        elif final_score <= FRUSTRATION_HIGH_MAX:
            level = "high"
        else:
            level = "critical"

        return {
            "score": final_score,
            "level": level
        }


frustration_analyzer = FrustrationAnalyzer()
