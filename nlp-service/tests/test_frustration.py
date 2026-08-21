import pytest
from app.services.frustration import frustration_analyzer
from app.services.conversation import conversation_analyzer
from app.services.preprocessing import preprocessor


def test_frustration_distinguishes_sentiment_from_frustration():
    # Moderate disappointment
    _, sig1 = preprocessor.preprocess("Your service was disappointing.")
    res1 = frustration_analyzer.analyze("Your service was disappointing.", sig1, "negative", "concerned", 0.8)
    assert res1["level"] in ["low", "medium"]

    # Critical angry escalation with repetition
    text2 = "THIS IS RIDICULOUS! I called four times and nobody is helping me!"
    _, sig2 = preprocessor.preprocess(text2)
    res2 = frustration_analyzer.analyze(text2, sig2, "negative", "angry", 0.9)
    assert res2["level"] in ["high", "critical"]
    assert res2["score"] > res1["score"]


def test_conversation_frustration_trend():
    # Rapidly increasing
    res1 = conversation_analyzer.compute_trend(92, [15, 30, 50])
    assert res1["frustration_trend"] == "rapidly_increasing"
    assert res1["previous_frustration_score"] == 50

    # Decreasing
    res2 = conversation_analyzer.compute_trend(20, [80, 55])
    assert res2["frustration_trend"] == "decreasing"
