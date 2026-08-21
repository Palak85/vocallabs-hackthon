"""
Hybrid Named Entity Recognition (NER) Service.
Combines deterministic regex extraction for high-precision structured IDs
with pattern-based extraction for domain-specific context entities.
"""

import re
from typing import List, Dict, Any

# Structured ID Patterns
PATTERNS = {
    "order_id": re.compile(r"\b(?:ORD|ORDER)[-_]?\d{3,10}\b", re.IGNORECASE),
    "policy_number": re.compile(r"\b(?:POL|POLICY)[-_]?\d{3,10}\b", re.IGNORECASE),
    "claim_number": re.compile(r"\b(?:CLM|CLAIM)[-_]?\d{3,10}\b", re.IGNORECASE),
    "transaction_id": re.compile(r"\b(?:TXN|TRANSACTION)[-_]?\d{3,10}\b", re.IGNORECASE),
    "booking_id": re.compile(r"\b(?:PNR|BOOKING)[-_]?[A-Z0-9]{4,8}\b", re.IGNORECASE),
    "flight_number": re.compile(r"\b(?:6E|AI|SG|UK|QP|IX|I5)[-_ ]?\d{2,4}\b", re.IGNORECASE),
    "amount": re.compile(r"\b(?:Rs\.?|INR|₹|\$)\s*(\d+(?:,\d+)*(?:\.\d+)?)\b|\b(\d+(?:,\d+)*)\s*(?:rupees|rs|bucks)\b", re.IGNORECASE),
    "phone_number": re.compile(r"\b(?:\+?91[\-\s]?)?[6-9]\d{9}\b"),
    "fee_type": re.compile(r"\b(tuition|semester|hostel|school|college|admission|transport|mess)\s+fees?\b", re.IGNORECASE),
    "doctor_name": re.compile(r"\bDr\.?\s+[A-Z][a-z]+(?:\s+[A-Z][a-z]+)?\b")
}


class EntityExtractor:
    def __init__(self):
        pass

    def extract(self, text: str, domain: str = "unknown") -> List[Dict[str, Any]]:
        if not text:
            return []

        entities = []
        seen = set()

        for entity_type, regex in PATTERNS.items():
            for match in regex.finditer(text):
                val = match.group(0).strip()
                key = (entity_type, val.lower())
                if key not in seen:
                    seen.add(key)
                    # Normalize fee type
                    if entity_type == "fee_type":
                        clean_val = val.lower().replace(" ", "_")
                    else:
                        clean_val = val

                    entities.append({
                        "type": entity_type,
                        "value": clean_val,
                        "confidence": 0.95
                    })

        return entities


entity_extractor = EntityExtractor()
