"""
Confidence Thresholds and Classification Constants for NLP Microservice.
"""

import os
from dotenv import load_dotenv

load_dotenv()

# Configurable confidence thresholds (calibrated for multi-class open-set fallbacks)
DOMAIN_CONFIDENCE_THRESHOLD = float(os.getenv("DOMAIN_CONFIDENCE_THRESHOLD", "0.28"))
INTENT_CONFIDENCE_THRESHOLD = float(os.getenv("INTENT_CONFIDENCE_THRESHOLD", "0.15"))
LANGUAGE_CONFIDENCE_THRESHOLD = float(os.getenv("LANGUAGE_CONFIDENCE_THRESHOLD", "0.40"))
SENTIMENT_CONFIDENCE_THRESHOLD = float(os.getenv("SENTIMENT_CONFIDENCE_THRESHOLD", "0.35"))
EMOTION_CONFIDENCE_THRESHOLD = float(os.getenv("EMOTION_CONFIDENCE_THRESHOLD", "0.20"))
URGENCY_CONFIDENCE_THRESHOLD = float(os.getenv("URGENCY_CONFIDENCE_THRESHOLD", "0.25"))

# Supported Domains
SUPPORTED_DOMAINS = [
    "ecommerce",
    "education",
    "insurance",
    "banking",
    "telecom",
    "travel",
    "healthcare"
]

FALLBACK_DOMAIN = "unknown"
FALLBACK_INTENT = "other"
FALLBACK_LANGUAGE = "unknown"

# Frustration Level Cutoffs
FRUSTRATION_LOW_MAX = 29
FRUSTRATION_MEDIUM_MAX = 59
FRUSTRATION_HIGH_MAX = 84

# Frustration Trend Definitions
TREND_STABLE = "stable"
TREND_DECREASING = "decreasing"
TREND_INCREASING = "increasing"
TREND_RAPIDLY_INCREASING = "rapidly_increasing"
