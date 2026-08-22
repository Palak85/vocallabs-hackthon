"""
Risk and Escalation Signal Detection Service.
Extracts financial, medical, and operational risk signals and provides escalation recommendations
to the Continuous Monitoring and Decision Engine without making autonomous final actions.
"""

import re
from typing import List, Dict, Any

class RiskSignalDetector:
    def __init__(self):
        self.financial_patterns = {
            "duplicate_debit": re.compile(r"\b(?:deducted\s+twice|do\s+baar\s+debit|double\s+deduction|charged\s+twice|duplicate\s+payment|two\s+times|twice)\b", re.IGNORECASE),
            "unauthorized_debit": re.compile(r"\b(?:unauthorized|fraud|fraudulent|hacked|scam|stolen\s+card|without\s+my\s+permission|unapproved\s+debit)\b", re.IGNORECASE),
            "money_missing": re.compile(r"\b(?:money\s+deducted|amount\s+deducted|paise\s+cut\s+gaye|balance\s+missing|debited|amount\s+withdrawn)\b", re.IGNORECASE),
            "refund_pending": re.compile(r"\b(?:refund\s+pending|refund\s+nahi\s+aaya|refund\s+not\s+received|waiting\s+for\s+refund|reversal\s+pending)\b", re.IGNORECASE),
            "receiver_not_received": re.compile(r"\b(?:merchant\s+ko\s+sirf\s+ek|receiver\s+didn'?t\s+get|receiver\s+not\s+received|merchant\s+did\s+not\s+receive|receiver\s+ko\s+nahi\s+mile)\b", re.IGNORECASE),
            "duplicate_payment_risk": re.compile(r"\b(?:don'?t\s+want\s+to\s+pay\s+again|baar-baar\s+booking|duplicate\s+payment\s+nahi|risk\s+being\s+charged\s+twice|pay\s+again)\b", re.IGNORECASE),
        }

        self.medical_patterns = {
            "medical_emergency": re.compile(r"\b(?:medical\s+emergency|serious\s+condition|condition\s+serious|life\s+threatening|critical\s+patient|icu|ambulance|cardiac|heart\s+attack|unconscious)\b", re.IGNORECASE),
            "serious_condition": re.compile(r"\b(?:condition\s+suddenly\s+serious|serious\s+ho\s+gayi|severely\s+ill|critical\s+condition|high\s+fever|unresponsive)\b", re.IGNORECASE),
            "immediate_hospital_need": re.compile(r"\b(?:urgently\s+hospital|hospital\s+le\s+jaana|need\s+hospital\s+now|immediate\s+hospital|emergency\s+ward)\b", re.IGNORECASE),
            "urgent_medical_attention": re.compile(r"\b(?:urgent\s+medical|immediate\s+doctor|emergency\s+doctor|doctor\s+immediately)\b", re.IGNORECASE),
            "appointment_blocking_emergency": re.compile(r"\b(?:emergency\s+appointment\s+pending|emergency\s+slot\s+reserve|appointment\s+status\s+pending\s+emergency)\b", re.IGNORECASE),
            "payment_blocking_emergency": re.compile(r"\b(?:payment\s+deduct\s+.*emergency|booking\s+pending\s+.*hospital)\b", re.IGNORECASE)
        }

    def detect_risk_signals(self, raw_text: str, domain: str, amount_val: float = None) -> List[str]:
        signals = []
        if not raw_text:
            return signals

        # Financial risks
        for signal_name, pattern in self.financial_patterns.items():
            if pattern.search(raw_text):
                signals.append(signal_name)

        if amount_val and amount_val >= 25000:
            signals.append("large_amount")
        elif re.search(r"\b(?:₹|rs\.?|inr)?\s*(?:[2-9]\d|\d{3,}),\d{3,}\b|\b(?:50000|75000|100000|lakh|crore)\b", raw_text, re.IGNORECASE):
            signals.append("large_amount")

        # Telecom specific risks
        if domain == "telecom" and ("money_missing" in signals or re.search(r"\b(?:deducted|recharge|payment)\b", raw_text, re.IGNORECASE)):
            if re.search(r"\b(?:not\s+updated|plan\s+not\s+active|purana\s+plan|old\s+plan|service\s+stopped)\b", raw_text, re.IGNORECASE):
                signals.append("service_not_activated")

        # Medical risks
        for signal_name, pattern in self.medical_patterns.items():
            if pattern.search(raw_text):
                signals.append(signal_name)

        return list(dict.fromkeys(signals))

    def detect_escalation_signals(
        self,
        domain: str,
        urgency_level: str,
        frustration_score: int,
        risk_signals: List[str]
    ) -> List[str]:
        escalation_signals = []

        if "medical_emergency" in risk_signals or "immediate_hospital_need" in risk_signals or urgency_level == "critical":
            if domain == "healthcare":
                escalation_signals.append("critical_medical_emergency")

        if "duplicate_debit" in risk_signals and "large_amount" in risk_signals:
            escalation_signals.append("high_financial_risk")
            escalation_signals.append("duplicate_high_value_transaction")
        elif "unauthorized_debit" in risk_signals or "large_amount" in risk_signals:
            escalation_signals.append("high_financial_risk")

        if frustration_score >= 80:
            escalation_signals.append("severe_frustration")

        if "appointment_blocking_emergency" in risk_signals or "payment_blocking_emergency" in risk_signals:
            escalation_signals.append("payment_blocking_critical_service")

        return list(dict.fromkeys(escalation_signals))

    def recommend_status(self, escalation_signals: List[str], urgency_level: str, frustration_level: str) -> str:
        if "critical_medical_emergency" in escalation_signals or urgency_level == "critical":
            return "CRITICAL"
        if "high_financial_risk" in escalation_signals or "duplicate_high_value_transaction" in escalation_signals:
            return "HIGH_PRIORITY"
        if frustration_level in ["high", "critical"] or urgency_level == "high":
            return "HIGH_PRIORITY"
        return "IN_PROGRESS"

risk_detector = RiskSignalDetector()
