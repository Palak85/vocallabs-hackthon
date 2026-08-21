"""
Data Privacy Masking Utility.
Masks sensitive credentials, passwords, card numbers, OTPs, and personal secrets.
"""

import re

CREDIT_CARD_REGEX = re.compile(r"\b(?:\d[ -]*?){13,16}\b")
OTP_REGEX = re.compile(r"\b(?:otp|code|pin)[\s:=]+(\d{4,8})\b", re.IGNORECASE)
PASSWORD_REGEX = re.compile(r"\b(?:password|passwd|pwd)[\s:=]+(\S+)\b", re.IGNORECASE)
PHONE_REGEX = re.compile(r"\b(?:\+?91[\-\s]?)?[6-9]\d{9}\b")

def mask_sensitive_text(text: str) -> str:
    if not text:
        return text
    masked = CREDIT_CARD_REGEX.sub("[CARD_MASKED]", text)
    masked = OTP_REGEX.sub("OTP: [MASKED]", masked)
    masked = PASSWORD_REGEX.sub("PASSWORD: [MASKED]", masked)
    return masked
