"""
Hybrid Named Entity Recognition (NER) Service.
Combines deterministic regex extraction for high-precision structured IDs
with pattern-based extraction for domain-specific context entities and temporal markers.
"""

import re
from typing import List, Dict, Any

# Structured ID and Context Entity Patterns
PATTERNS = {
    "order_id": re.compile(r"\b(?:ORD|ORDER)[-_]?[A-Z0-9]{3,12}\b", re.IGNORECASE),
    "policy_number": re.compile(r"\b(?:POL|POLICY)[-_]?[A-Z0-9]{3,12}\b", re.IGNORECASE),
    "claim_number": re.compile(r"\b(?:CLM|CLAIM)[-_]?[A-Z0-9]{3,12}\b", re.IGNORECASE),
    "transaction_id": re.compile(r"\b(?:TXN|TRANSACTION|UPI)[-_]?[A-Z0-9]{3,16}\b", re.IGNORECASE),
    "booking_id": re.compile(r"\b(?:PNR|BOOKING)[-_]?[A-Z0-9]{4,10}\b", re.IGNORECASE),
    "flight_number": re.compile(r"\b(?:6E|AI|SG|UK|QP|IX|I5)[-_ ]?\d{2,4}\b", re.IGNORECASE),
    "student_id": re.compile(r"\b(?:STU|STUDENT|ROLL)[-_]?[A-Z0-9]{3,10}\b", re.IGNORECASE),
    "account_number": re.compile(r"\b(?:ACC|ACCOUNT)[-_]?[A-Z0-9]{6,16}\b", re.IGNORECASE),
    "amount": re.compile(r"(?:(?<=\s)|(?<=^)|(?<=[(\[{]))(?:(?:Rs\.?|INR|₹|\$|USD|EUR)\s*(\d+(?:,\d+)*(?:\.\d+)?)|(\d+(?:,\d+)*(?:\.\d+)?)\s*(?:rupees|rs\.?|inr|bucks|lakh|crore))\b", re.IGNORECASE),
    "phone_number": re.compile(r"\b(?:\+?91[\-\s]?)?[6-9]\d{9}\b"),
    "fee_type": re.compile(r"\b(tuition|semester|hostel|school|college|admission|transport|mess)\s+fees?\b", re.IGNORECASE),
    "doctor_name": re.compile(r"\b(?:Dr\.?|Doctor)\s+(?!(?:ki|ka|ke|ko|se|me|mein|par|ne|kya|hai|ho|the|a|an|is|available|emergency|consultation|appointment|slot|option|visit|fee|fees|hospital|clinic|prescription|details|schedule|time|cabin|room|near|nearby)\b)([A-Za-z]{2,})(?:\s+(?!(?:tomorrow|today|yesterday|on|at|fees|bills|hospital|clinic|appointment|consultation|treatment|opd|cabin|room|now|urgent|please|so|for|to|with|ki|ka|ke|ko|se|me|mein|par|slot|option|hai|ho|bhi|tha|thi|near|nearby))[A-Za-z]{2,})?\b", re.IGNORECASE),
    "date": re.compile(r"\b(?:yesterday|today|tomorrow|last\s+week|next\s+week|\d+\s+days?\s+ago|\d+\s+weeks?|\d{1,2}[/-]\d{1,2}[/-]\d{2,4})\b", re.IGNORECASE)
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
