import pytest
from app.services.ner import entity_extractor


def test_structured_ner_extraction():
    text = "Please check order ORD-99214 and claim CLM-45672 for transaction TXN-10293."
    entities = entity_extractor.extract(text, "ecommerce")
    
    types = {e["type"] for e in entities}
    values = {e["value"] for e in entities}

    assert "order_id" in types
    assert "claim_number" in types
    assert "transaction_id" in types
    assert "ORD-99214" in values
