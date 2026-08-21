import pytest
from app.services.preprocessing import preprocessor


def test_preprocessing_normalization():
    raw = "My   order   hasn't    arrived   yet...   https://tracking.com/123"
    cleaned, signals = preprocessor.preprocess(raw)
    assert "https" not in cleaned
    assert "not" in cleaned
    assert signals["word_count"] > 0


def test_preprocessing_preserves_emotion_signals():
    raw = "THIS IS ABSOLUTELY TERRIBLE!!!"
    cleaned, signals = preprocessor.preprocess(raw)
    assert signals["exclamation_count"] == 3
    assert signals["uppercase_ratio"] > 0.8
