"""
Conversation-Level NLP & Frustration Trend Analysis Service.
Tracks conversation trajectory and determines emotional progression:
- stable
- decreasing
- increasing
- rapidly_increasing
"""

from typing import List, Dict, Any, Optional
from app.utils.thresholds import TREND_STABLE, TREND_DECREASING, TREND_INCREASING, TREND_RAPIDLY_INCREASING


class ConversationAnalyzer:
    def __init__(self):
        pass

    def compute_trend(
        self,
        current_score: int,
        history_scores: List[int]
    ) -> Dict[str, Any]:
        """Calculates trajectory from previous message scores in conversation."""
        if not history_scores:
            return {
                "frustration_trend": TREND_STABLE,
                "previous_frustration_score": None,
                "current_frustration_score": current_score
            }

        prev_score = history_scores[-1]
        delta = current_score - prev_score

        if delta >= 30:
            trend = TREND_RAPIDLY_INCREASING
        elif delta >= 10:
            trend = TREND_INCREASING
        elif delta <= -10:
            trend = TREND_DECREASING
        else:
            trend = TREND_STABLE

        return {
            "frustration_trend": trend,
            "previous_frustration_score": prev_score,
            "current_frustration_score": current_score
        }


conversation_analyzer = ConversationAnalyzer()
