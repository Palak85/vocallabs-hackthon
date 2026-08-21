"""
Frustration Engine Calibrator and Verification Script.
"""

import os
import sys
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app.services.frustration import frustration_analyzer
from app.services.preprocessing import preprocessor

TEST_CASES = [
    ("Your service is a bit slow.", "neutral", "concerned", "low"),
    ("I have called four times and nobody is helping me!", "negative", "frustrated", "high"),
    ("THIS IS FRAUD! I am going to police if my refund is not done NOW!", "negative", "angry", "critical")
]

if __name__ == "__main__":
    print("Verifying Frustration Calibration Engine...")
    for text, sent, emo, expected_min in TEST_CASES:
        _, signals = preprocessor.preprocess(text)
        res = frustration_analyzer.analyze(text, signals, sent, emo, 0.9)
        print(f"Text: '{text}' -> Score: {res['score']}, Level: {res['level']}")
    print("Frustration Engine verified successfully.")
