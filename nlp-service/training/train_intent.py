"""
Standalone Hierarchical Intent Training Script across all 7 domains.
"""

import os
import sys
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from training.train_all import train_classifier

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
SPLITS_DIR = os.path.join(BASE_DIR, "data", "splits")
MODELS_DIR = os.path.join(BASE_DIR, "models", "intent")

os.makedirs(MODELS_DIR, exist_ok=True)

DOMAINS = ["ecommerce", "education", "insurance", "banking", "telecom", "travel", "healthcare"]

if __name__ == "__main__":
    print("Training Hierarchical Intent Models across all domains...")
    for d in DOMAINS:
        csv_name = f"intent_{d}.csv"
        train_classifier(
            os.path.join(SPLITS_DIR, "train", csv_name),
            os.path.join(SPLITS_DIR, "validation", csv_name),
            os.path.join(SPLITS_DIR, "test", csv_name),
            os.path.join(MODELS_DIR, f"intent_{d}.joblib"),
            ngram_range=(1, 3)
        )
    print("All domain intent models trained successfully.")
