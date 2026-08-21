"""
Standalone Sentiment Analysis Training Script.
"""

import os
import sys
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from training.train_all import train_classifier

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
SPLITS_DIR = os.path.join(BASE_DIR, "data", "splits")
MODELS_DIR = os.path.join(BASE_DIR, "models", "sentiment")

os.makedirs(MODELS_DIR, exist_ok=True)

if __name__ == "__main__":
    print("Training Sentiment Analysis Model...")
    train_classifier(
        os.path.join(SPLITS_DIR, "train", "sentiment.csv"),
        os.path.join(SPLITS_DIR, "validation", "sentiment.csv"),
        os.path.join(SPLITS_DIR, "test", "sentiment.csv"),
        os.path.join(MODELS_DIR, "sentiment_model.joblib"),
        ngram_range=(1, 3)
    )
    print("Sentiment model training complete.")
