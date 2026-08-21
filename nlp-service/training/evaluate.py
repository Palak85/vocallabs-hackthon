"""
Comprehensive Model Evaluation Script.
Evaluates all serialized models on the held-out test splits.
Reports Accuracy, Precision, Recall, Macro F1, and Per-Class metrics.
Saves genuine evaluation results to data/metadata/evaluation_summary.json.
"""

import os
import sys
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

import json
import joblib
import pandas as pd
from datetime import datetime
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, classification_report

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
TEST_DIR = os.path.join(BASE_DIR, "data", "splits", "test")
MODELS_DIR = os.path.join(BASE_DIR, "models")
METADATA_DIR = os.path.join(BASE_DIR, "data", "metadata")


def evaluate_model(model_path: str, test_csv_path: str):
    if not os.path.exists(model_path) or not os.path.exists(test_csv_path):
        return None

    model = joblib.load(model_path)
    df = pd.read_csv(test_csv_path)

    preds = model.predict(df["text"])
    labels = df["label"]

    acc = float(accuracy_score(labels, preds))
    prec_macro = float(precision_score(labels, preds, average="macro", zero_division=0))
    rec_macro = float(recall_score(labels, preds, average="macro", zero_division=0))
    f1_macro = float(f1_score(labels, preds, average="macro", zero_division=0))
    
    report = classification_report(labels, preds, output_dict=True, zero_division=0)

    return {
        "samples": len(df),
        "accuracy": round(acc, 4),
        "macro_precision": round(prec_macro, 4),
        "macro_recall": round(rec_macro, 4),
        "macro_f1": round(f1_macro, 4),
        "per_class": {k: v for k, v in report.items() if k not in ["accuracy", "macro avg", "weighted avg"]}
    }


def run_full_evaluation():
    print("=" * 60)
    print("STARTING HELD-OUT TEST SPLIT EVALUATION")
    print("=" * 60)

    summary = {
        "evaluation_timestamp": datetime.utcnow().isoformat() + "Z",
        "device": "CPU",
        "models": {}
    }

    # 1. Language
    summary["models"]["language"] = evaluate_model(
        os.path.join(MODELS_DIR, "language", "language_model.joblib"),
        os.path.join(TEST_DIR, "language.csv")
    )
    print(f"Language Model -> Acc: {summary['models']['language']['accuracy']} | Macro F1: {summary['models']['language']['macro_f1']}")

    # 2. Domain
    summary["models"]["domain"] = evaluate_model(
        os.path.join(MODELS_DIR, "domain", "domain_model.joblib"),
        os.path.join(TEST_DIR, "domain.csv")
    )
    print(f"Domain Model   -> Acc: {summary['models']['domain']['accuracy']} | Macro F1: {summary['models']['domain']['macro_f1']}")

    # 3. Sentiment
    summary["models"]["sentiment"] = evaluate_model(
        os.path.join(MODELS_DIR, "sentiment", "sentiment_model.joblib"),
        os.path.join(TEST_DIR, "sentiment.csv")
    )
    print(f"Sentiment Model-> Acc: {summary['models']['sentiment']['accuracy']} | Macro F1: {summary['models']['sentiment']['macro_f1']}")

    # 4. Emotion
    summary["models"]["emotion"] = evaluate_model(
        os.path.join(MODELS_DIR, "emotion", "emotion_model.joblib"),
        os.path.join(TEST_DIR, "emotion.csv")
    )
    print(f"Emotion Model  -> Acc: {summary['models']['emotion']['accuracy']} | Macro F1: {summary['models']['emotion']['macro_f1']}")

    # 5. Urgency
    summary["models"]["urgency"] = evaluate_model(
        os.path.join(MODELS_DIR, "urgency", "urgency_model.joblib"),
        os.path.join(TEST_DIR, "urgency.csv")
    )
    print(f"Urgency Model  -> Acc: {summary['models']['urgency']['accuracy']} | Macro F1: {summary['models']['urgency']['macro_f1']}")

    # 6. Intent Models
    summary["models"]["intents"] = {}
    domains = ["ecommerce", "education", "insurance", "banking", "telecom", "travel", "healthcare"]
    for d in domains:
        res = evaluate_model(
            os.path.join(MODELS_DIR, "intent", f"intent_{d}.joblib"),
            os.path.join(TEST_DIR, f"intent_{d}.csv")
        )
        summary["models"]["intents"][d] = res
        print(f"Intent [{d.ljust(11)}] -> Acc: {res['accuracy']} | Macro F1: {res['macro_f1']}")

    out_file = os.path.join(METADATA_DIR, "evaluation_summary.json")
    with open(out_file, "w", encoding="utf-8") as f:
        json.dump(summary, f, indent=2)
    print(f"\nSaved evaluation summary to: {out_file}")


if __name__ == "__main__":
    run_full_evaluation()
