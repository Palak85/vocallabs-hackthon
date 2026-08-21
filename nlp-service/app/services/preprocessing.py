"""
Text Preprocessing Module for Multi-Domain NLP Service.
Handles normalization, Indian English / Hinglish normalization, contraction expansion,
while preserving vital emotional signals (punctuation, capitalization, emojis).
"""

import re
import html
from typing import Dict, Any, Tuple

CONTRACTION_MAP = {
    "hasn't": "has not",
    "haven't": "have not",
    "didn't": "did not",
    "doesn't": "does not",
    "don't": "do not",
    "isn't": "is not",
    "aren't": "are not",
    "wasn't": "was not",
    "weren't": "were not",
    "can't": "cannot",
    "won't": "will not",
    "couldn't": "could not",
    "shouldn't": "should not",
    "wouldn't": "would not",
    "'re": " are",
    "'s": " is",
    "'d": " would",
    "'ll": " will",
    "'ve": " have",
    "'m": " am",
    "pls": "please",
    "plz": "please",
    "thx": "thanks",
    "thanx": "thanks",
    "u": "you",
    "ur": "your",
    "r": "are"
}

URL_REGEX = re.compile(r"https?://\S+|www\.\S+")
EMAIL_REGEX = re.compile(r"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b")
WHITESPACE_REGEX = re.compile(r"\s+")
REPEATED_CHAR_REGEX = re.compile(r"(.)\1{2,}")


class TextPreprocessor:
    def __init__(self):
        pass

    def extract_signals(self, text: str) -> Dict[str, Any]:
        """Extracts emotional and behavioral cues prior to destructive normalization."""
        if not text:
            return {
                "exclamation_count": 0,
                "question_count": 0,
                "uppercase_ratio": 0.0,
                "has_repeated_chars": False,
                "char_length": 0,
                "word_count": 0
            }

        exclamation_count = text.count("!")
        question_count = text.count("?")
        total_chars = len(text)
        alpha_chars = [c for c in text if c.isalpha()]
        uppercase_chars = [c for c in alpha_chars if c.isupper()]
        
        uppercase_ratio = len(uppercase_chars) / len(alpha_chars) if alpha_chars else 0.0
        has_repeated_chars = bool(REPEATED_CHAR_REGEX.search(text))
        words = text.split()

        return {
            "exclamation_count": exclamation_count,
            "question_count": question_count,
            "uppercase_ratio": round(uppercase_ratio, 3),
            "has_repeated_chars": has_repeated_chars,
            "char_length": total_chars,
            "word_count": len(words)
        }

    def clean_text(self, text: str, lower: bool = True) -> str:
        """Cleans and standardizes text for classification models."""
        if not text:
            return ""

        # Unescape HTML entities
        cleaned = html.unescape(text)

        # Replace URLs and emails
        cleaned = URL_REGEX.sub(" ", cleaned)
        cleaned = EMAIL_REGEX.sub(" ", cleaned)

        # Expand contractions
        for contraction, replacement in CONTRACTION_MAP.items():
            cleaned = re.sub(r"\b" + re.escape(contraction) + r"\b", replacement, cleaned, flags=re.IGNORECASE)

        # Standardize excessive repeated characters (e.g., 'sooooo' -> 'soo')
        cleaned = REPEATED_CHAR_REGEX.sub(r"\1\1", cleaned)

        # Normalize whitespace
        cleaned = WHITESPACE_REGEX.sub(" ", cleaned).strip()

        if lower:
            cleaned = cleaned.lower()

        return cleaned

    def preprocess(self, text: str) -> Tuple[str, Dict[str, Any]]:
        """Unified entry point returning cleaned text and extracted emotional signals."""
        signals = self.extract_signals(text)
        cleaned = self.clean_text(text, lower=True)
        return cleaned, signals


# Default singleton instance
preprocessor = TextPreprocessor()
