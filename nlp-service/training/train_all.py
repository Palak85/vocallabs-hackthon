"""
Unified Model Training Pipeline.
Trains TF-IDF + Calibrated Logistic Regression classifiers on the validated stratified splits:
1. Language Model (en, hi, hinglish)
2. Domain Detection Model (7 domains)
3. 7 Hierarchical Intent Models (one per domain)
4. Sentiment Analysis Model (positive, neutral, negative)
5. Emotion Detection Model (happy, neutral, concerned, sad, frustrated, angry)
6. Urgency Detection Model (low, medium, high, critical)

Saves all serialized pipelines and metadata locally to models/ directory.
"""

import os
import glob
import json
import joblib
import pandas as pd
from datetime import datetime
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.calibration import CalibratedClassifierCV
from sklearn.metrics import classification_report, accuracy_score, f1_score

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
SPLITS_DIR = os.path.join(BASE_DIR, "data", "splits")
TRAIN_DIR = os.path.join(SPLITS_DIR, "train")
VAL_DIR = os.path.join(SPLITS_DIR, "validation")
TEST_DIR = os.path.join(SPLITS_DIR, "test")
MODELS_DIR = os.path.join(BASE_DIR, "models")

for sub in ["language", "domain", "intent", "sentiment", "emotion", "urgency"]:
    os.makedirs(os.path.join(MODELS_DIR, sub), exist_ok=True)


def train_classifier(train_csv: str, val_csv: str, test_csv: str, output_path: str, ngram_range=(1, 3)):
    train_df = pd.read_csv(train_csv)
    test_df = pd.read_csv(test_csv)

    pipeline = Pipeline([
        ("tfidf", TfidfVectorizer(
            ngram_range=ngram_range,
            sublinear_tf=True,
            min_df=1,
            analyzer="word",
            token_pattern=r"(?u)\b\w+\b"
        )),
        ("clf", LogisticRegression(
            C=5.0,
            max_iter=1000,
            class_weight="balanced",
            random_state=42
        ))
    ])

    pipeline.fit(train_df["text"], train_df["label"])
    
    # Evaluate on held-out test split
    preds = pipeline.predict(test_df["text"])
    probs = pipeline.predict_proba(test_df["text"])

    acc = float(accuracy_score(test_df["label"], preds))
    macro_f1 = float(f1_score(test_df["label"], preds, average="macro", zero_division=0))
    report = classification_report(test_df["label"], preds, output_dict=True, zero_division=0)

    # Serialize model pipeline
    joblib.dump(pipeline, output_path)
    print(f"Model saved -> {output_path} | Test Acc: {acc:.3f} | Test Macro F1: {macro_f1:.3f}")

    return {
        "accuracy": acc,
        "macro_f1": macro_f1,
        "classification_report": report,
        "classes": list(pipeline.classes_)
    }


def train_all_models():
    print("=" * 60)
    print("STARTING FULL NLP MODEL TRAINING & SERIALIZATION")
    print("=" * 60)

    results_summary = {"trained_at": datetime.utcnow().isoformat() + "Z", "models": {}}

    # 1. Language Model
    print("\n--- Training Language Detection Model ---")
    results_summary["models"]["language"] = train_classifier(
        os.path.join(TRAIN_DIR, "language.csv"),
        os.path.join(VAL_DIR, "language.csv"),
        os.path.join(TEST_DIR, "language.csv"),
        os.path.join(MODELS_DIR, "language", "language_model.joblib"),
        ngram_range=(1, 2)
    )

    # 2. Domain Model
    print("\n--- Training Semantic Domain Detection Model ---")
    results_summary["models"]["domain"] = train_classifier(
        os.path.join(TRAIN_DIR, "domain.csv"),
        os.path.join(VAL_DIR, "domain.csv"),
        os.path.join(TEST_DIR, "domain.csv"),
        os.path.join(MODELS_DIR, "domain", "domain_model.joblib"),
        ngram_range=(1, 3)
    )

    # 3. Hierarchical Intent Models
    domains = ["ecommerce", "education", "insurance", "banking", "telecom", "travel", "healthcare"]
    results_summary["models"]["intent"] = {}
    for d in domains:
        print(f"\n--- Training Intent Model for [{d.upper()}] ---")
        csv_name = f"intent_{d}.csv"
        results_summary["models"]["intent"][d] = train_classifier(
            os.path.join(TRAIN_DIR, csv_name),
            os.path.join(VAL_DIR, csv_name),
            os.path.join(TEST_DIR, csv_name),
            os.path.join(MODELS_DIR, "intent", f"intent_{d}.joblib"),
            ngram_range=(1, 3)
        )

    # 4. Sentiment Model
    print("\n--- Training Sentiment Analysis Model ---")
    results_summary["models"]["sentiment"] = train_classifier(
        os.path.join(TRAIN_DIR, "sentiment.csv"),
        os.path.join(VAL_DIR, "sentiment.csv"),
        os.path.join(TEST_DIR, "sentiment.csv"),
        os.path.join(MODELS_DIR, "sentiment", "sentiment_model.joblib"),
        ngram_range=(1, 3)
    )

    # 5. Emotion Model
    print("\n--- Training Emotion Detection Model ---")
    results_summary["models"]["emotion"] = train_classifier(
        os.path.join(TRAIN_DIR, "emotion.csv"),
        os.path.join(VAL_DIR, "emotion.csv"),
        os.path.join(TEST_DIR, "emotion.csv"),
        os.path.join(MODELS_DIR, "emotion", "emotion_model.joblib"),
        ngram_range=(1, 3)
    )

    # 6. Urgency Model
    print("\n--- Training Urgency Detection Model ---")
    results_summary["models"]["urgency"] = train_classifier(
        os.path.join(TRAIN_DIR, "urgency.csv"),
        os.path.join(VAL_DIR, "urgency.csv"),
        os.path.join(TEST_DIR, "urgency.csv"),
        os.path.join(MODELS_DIR, "urgency", "urgency_model.joblib"),
        ngram_range=(1, 3)
    )

    # Save summary metadata
    meta_path = os.path.join(MODELS_DIR, "model_evaluation_metrics.json")
    with open(meta_path, "w", encoding="utf-8") as f:
        json.dump(results_summary, f, indent=2)
    print(f"\nAll models successfully trained and serialized. Evaluation metrics recorded in: {meta_path}")


if __name__ == "__main__":
    train_all_models()
