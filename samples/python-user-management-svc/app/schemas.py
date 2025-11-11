from __future__ import annotations
from datetime import datetime
from typing import Optional
from pydantic import BaseModel, EmailStr, Field, field_validator
import uuid


class UserCreateDTO(BaseModel):
    email: EmailStr
    password: str = Field(min_length=8)
    displayName: str = Field(max_length=50)

    model_config = {
        "frozen": True,
        "extra": "forbid",
    }


class UserResponseDTO(BaseModel):
    id: str
    email: EmailStr
    displayName: Optional[str]
    createdAt: datetime

    model_config = {
        "frozen": True,
        "extra": "ignore",
    }
