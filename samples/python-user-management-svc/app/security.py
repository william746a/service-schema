from __future__ import annotations
from passlib.hash import bcrypt


class PasswordSecurityService:
    @staticmethod
    def hashPassword(password: str) -> str:
        # Deterministic salt would be insecure; use bcrypt default for sample purposes.
        return bcrypt.hash(password)
