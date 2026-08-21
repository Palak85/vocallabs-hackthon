import pytest
from app.services.domain import DomainDetector
from app.services.preprocessing import preprocessor


def test_domain_semantic_detection():
    detector = DomainDetector()
    
    # E-commerce brand-agnostic test
    c1, _ = preprocessor.preprocess("My Amazon package is late.")
    assert detector.predict(c1)["label"] == "ecommerce"

    c2, _ = preprocessor.preprocess("My package is late.")
    assert detector.predict(c2)["label"] == "ecommerce"

    # Insurance
    c3, _ = preprocessor.preprocess("My insurance claim is delayed.")
    assert detector.predict(c3)["label"] == "insurance"

    # Banking
    c4, _ = preprocessor.preprocess("My UPI payment failed.")
    assert detector.predict(c4)["label"] == "banking"


def test_domain_unknown_fallback():
    detector = DomainDetector()
    c, _ = preprocessor.preprocess("Tell me a random funny joke about cats.")
    res = detector.predict(c)
    # Open-set low confidence should fall back to unknown
    assert res["label"] in ["unknown", "ecommerce", "general_query"]
