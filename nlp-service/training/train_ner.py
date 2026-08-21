"""
NER Pipeline Verification and Evaluation Script.
"""

import os
import sys
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app.services.ner import entity_extractor

TEST_INPUTS = [
    "Order ORD-99214 was not delivered, paid Rs. 1499.",
    "Claim CLM-45672 pending for surgery with Dr. Sharma.",
    "Transaction TXN-10293 failed for semester fee.",
    "Flight 6E-412 cancelled, booking PNR-8812."
]

if __name__ == "__main__":
    print("Testing Hybrid Named Entity Recognition Engine...")
    for inp in TEST_INPUTS:
        ents = entity_extractor.extract(inp)
        print(f"Input: '{inp}'")
        for e in ents:
            print(f"   - [{e['type']}]: '{e['value']}' (conf: {e['confidence']})")
    print("NER engine verification complete.")
