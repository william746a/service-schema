from __future__ import annotations
from datetime import datetime
from typing import Optional, List, Any
from pydantic import BaseModel, EmailStr, Field


class UserCreatedEventDTO(BaseModel):
    userId: str
    email: EmailStr
    displayName: Optional[str] = None

    model_config = {
        "frozen": True,
        "extra": "forbid",
    }


class SubscriptionResponseDTO(BaseModel):
    subId: str
    status: str
    planId: str
    expiresAt: datetime

    model_config = {"frozen": True}


class CustomerDTO(BaseModel):
    id: str
    email: EmailStr
    stripeId: Optional[str] = None

    model_config = {"frozen": True}


class StripeEventDTO(BaseModel):
    id: str
    type: str
    data: Any

    model_config = {"frozen": True}
