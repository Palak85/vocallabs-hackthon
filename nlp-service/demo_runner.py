"""
Hackathon Demonstration Script for Multi-Domain Customer Support NLP Service.
Demonstrates:
1. Multi-Domain Semantic Generalization (brand-agnostic vs brand queries)
2. DEMO 1: Escalation Signals (Multi-turn insurance claim with increasing frustration)
3. DEMO 2: Education Fee payment query (Concerned emotion, medium frustration)
4. DEMO 3: E-commerce late delivery (delivery_delay intent)
5. DEMO 4: Banking failed transaction (transaction_failed intent)
6. DEMO 5: Hinglish Telecom issue (Hinglish language, telecom domain)
7. Out-of-Domain / Ambiguous handling
"""

import sys
import os
import json
sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))

from app.services.inference import pipeline


def print_json(data):
    print(json.dumps(data, indent=2))


def run_demo():
    print("=" * 70)
    print("AI-POWERED MULTI-DOMAIN CUSTOMER SUPPORT NLP SERVICE - HACKATHON DEMO")
    print("=" * 70)

    # 1. SEMANTIC GENERALIZATION DEMO
    print("\n" + "#" * 70)
    print("PART 1: SEMANTIC GENERALIZATION ACROSS BRANDS (NO BRAND MEMORIZATION)")
    print("#" * 70)
    
    brand_tests = [
        "My Amazon package is late.",
        "My Blinkit delivery is late.",
        "My package is late, where is it?",
        "I need to pay my college fees.",
        "My school payment isn't showing.",
        "My insurance claim is delayed.",
        "My UPI payment failed.",
        "My mobile data isn't working.",
        "My flight was cancelled."
    ]
    for text in brand_tests:
        res = pipeline.analyze(text)
        domain = res["nlp"]["domain"]["label"]
        conf = res["nlp"]["domain"]["confidence"]
        intent = res["nlp"]["intent"]["label"]
        print(f"Text: '{text}'")
        print(f"   -> Domain: [{domain.upper()}] (conf: {conf}) | Intent: [{intent}]\n")

    # 2. HACKATHON DEMOS 1 - 5
    print("\n" + "#" * 70)
    print("PART 2: OFFICIAL HACKATHON DEMOS (STRUCTURED SIGNAL RESPONSES)")
    print("#" * 70)

    demos = [
        {
            "title": "DEMO 1: Escalating Frustrated Customer (Multi-turn Context)",
            "conversation_id": "demo_conv_001",
            "turns": [
                ("I paid my insurance premium last week.", []),
                ("My insurance claim CLM-45672 is still pending and nobody is helping me.", [10]),
                ("I have already contacted support three times. My insurance claim is still pending and nobody is helping me! Fix this immediately!", [10, 45])
            ]
        },
        {
            "title": "DEMO 2: Concerned Parent / Education Fee Payment",
            "conversation_id": "demo_conv_002",
            "turns": [
                ("I paid my daughter's school fees yesterday but the portal still shows unpaid.", [])
            ]
        },
        {
            "title": "DEMO 3: E-commerce Delivery Delay",
            "conversation_id": "demo_conv_003",
            "turns": [
                ("My Amazon package is late.", [])
            ]
        },
        {
            "title": "DEMO 4: Banking Failed Transaction",
            "conversation_id": "demo_conv_004",
            "turns": [
                ("My UPI payment failed but money was deducted from account for TXN-88123.", [])
            ]
        },
        {
            "title": "DEMO 5: Hinglish Telecom Issue",
            "conversation_id": "demo_conv_005",
            "turns": [
                ("Mera recharge ho gaya lekin internet data nahi chal raha hai.", [])
            ]
        },
        {
            "title": "DEMO 6: Ambiguous / Out-of-Domain Query",
            "conversation_id": "demo_conv_006",
            "turns": [
                ("Tell me a funny joke about space aliens.", [])
            ]
        }
    ]

    for demo in demos:
        print("\n" + "=" * 60)
        print(f"RUNNING {demo['title']}")
        print("=" * 60)
        for i, (turn_text, history) in enumerate(demo["turns"], start=1):
            print(f"\n[Turn {i}] Customer Input: \"{turn_text}\"")
            res = pipeline.analyze(turn_text, history_scores=history)
            
            output_contract = {
                "success": True,
                "conversation_id": demo["conversation_id"],
                "message_id": f"msg_turn_{i}",
                "nlp": res["nlp"],
                "conversation_analysis": res["conversation_analysis"],
                "latency_ms": res["latency_ms"]
            }
            print_json(output_contract)

    print("\n" + "=" * 70)
    print("HACKATHON DEMO COMPLETED SUCCESSFULLY!")
    print("=" * 70)


if __name__ == "__main__":
    run_demo()
