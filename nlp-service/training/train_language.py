"""
Standalone Language Detection Training Script.
"""

import os
import sys
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from training.train_all import train_classifier

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
SPLITS_DIR = os.path.join(BASE_DIR, "data", "splits")
MODELS_DIR = os.path.join(BASE_DIR, "models", "language")

os.makedirs(MODELS_DIR, exist_ok=True)

if __name__ == "__main__":
    print("Training Language Detection Model...")
    train_classifier(
        os.path.join(SPLITS_DIR, "train", "language.csv"),
        os.path.join(SPLITS_DIR, "validation", "language.csv"),
        os.path.join(SPLITS_DIR, "test", "language.csv"),
        os.path.join(MODELS_DIR, "language_model.joblib"),
        ngram_range=(1, 2)
    )
    print("Language model training complete.")
