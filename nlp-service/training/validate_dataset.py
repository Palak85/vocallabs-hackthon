"""
Dataset Validation and Stratified Split Script.
Performs checks on all datasets in data/:
1. Missing / null value checks
2. Exact duplicate detection
3. Train/Val/Test stratified splits (70% / 15% / 15%)
4. Data leakage verification (verifies train vs val vs test disjointness)
5. Class distribution & imbalance reporting
6. Generates comprehensive validation report in data/metadata/validation_report.json
"""

import os
import glob
import json
import pandas as pd
from sklearn.model_selection import train_test_split

DATA_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "data"))
SPLITS_DIR = os.path.join(DATA_DIR, "splits")
METADATA_DIR = os.path.join(DATA_DIR, "metadata")

for folder in ["train", "validation", "test"]:
    os.makedirs(os.path.join(SPLITS_DIR, folder), exist_ok=True)
os.makedirs(METADATA_DIR, exist_ok=True)


def validate_and_split():
    csv_files = glob.glob(os.path.join(DATA_DIR, "*.csv"))
    if not csv_files:
        print("No CSV files found in data/. Run create_datasets.py first.")
        return

    report = {"datasets": {}, "summary": {"total_files_checked": len(csv_files), "leakage_detected": False}}

    for file_path in csv_files:
        fname = os.path.basename(file_path)
        df = pd.read_csv(file_path)

        # Check nulls
        null_count = int(df.isnull().sum().sum())
        if null_count > 0:
            df = df.dropna().reset_index(drop=True)

        # Check duplicates
        initial_count = len(df)
        df = df.drop_duplicates(subset=["text"]).reset_index(drop=True)
        dropped_duplicates = initial_count - len(df)

        # Class counts
        class_dist = df["label"].value_counts().to_dict()
        min_class_samples = min(class_dist.values()) if class_dist else 0

        # Check if stratified split is feasible
        num_classes = len(class_dist)
        can_stratify_1 = (min_class_samples >= 2) and (int(len(df) * 0.30) >= num_classes)
        stratify_1 = df["label"] if can_stratify_1 else None

        train_df, test_val_df = train_test_split(
            df,
            test_size=0.30,
            random_state=42,
            stratify=stratify_1
        )

        test_val_class_dist = test_val_df["label"].value_counts().to_dict()
        min_test_val_samples = min(test_val_class_dist.values()) if test_val_class_dist else 0
        num_test_val_classes = len(test_val_class_dist)
        can_stratify_2 = (min_test_val_samples >= 2) and (int(len(test_val_df) * 0.50) >= num_test_val_classes)
        stratify_2 = test_val_df["label"] if can_stratify_2 else None

        val_df, test_df = train_test_split(
            test_val_df,
            test_size=0.50,
            random_state=42,
            stratify=stratify_2
        )

        # Leakage check: Train / Val / Test sets must have 0 text overlaps
        train_texts = set(train_df["text"].str.strip().str.lower())
        val_texts = set(val_df["text"].str.strip().str.lower())
        test_texts = set(test_df["text"].str.strip().str.lower())

        train_val_leak = len(train_texts.intersection(val_texts))
        train_test_leak = len(train_texts.intersection(test_texts))
        val_test_leak = len(val_texts.intersection(test_texts))

        has_leak = (train_val_leak + train_test_leak + val_test_leak) > 0
        if has_leak:
            report["summary"]["leakage_detected"] = True

        # Save splits
        train_df.to_csv(os.path.join(SPLITS_DIR, "train", fname), index=False)
        val_df.to_csv(os.path.join(SPLITS_DIR, "validation", fname), index=False)
        test_df.to_csv(os.path.join(SPLITS_DIR, "test", fname), index=False)

        report["datasets"][fname] = {
            "total_samples": len(df),
            "nulls_removed": null_count,
            "duplicates_dropped": dropped_duplicates,
            "train_samples": len(train_df),
            "val_samples": len(val_df),
            "test_samples": len(test_df),
            "class_distribution": class_dist,
            "data_leakage": {
                "train_val_overlap": train_val_leak,
                "train_test_overlap": train_test_leak,
                "val_test_overlap": val_test_leak
            }
        }
        print(f"Validated {fname}: {len(train_df)} train, {len(val_df)} val, {len(test_df)} test (No leaks: {not has_leak})")

    report_path = os.path.join(METADATA_DIR, "validation_report.json")
    with open(report_path, "w", encoding="utf-8") as f:
        json.dump(report, f, indent=2)
    print(f"Validation Report saved to: {report_path}")


if __name__ == "__main__":
    validate_and_split()
