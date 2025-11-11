from __future__ import annotations
from datetime import datetime
from typing import Optional
from sqlalchemy import String, DateTime
from sqlalchemy.orm import Mapped, mapped_column
from .database import Base


class UserEntity(Base):
    __tablename__ = "users"

    # x-persistence: id (PK uuid) -> column user_id
    id: Mapped[str] = mapped_column("user_id", String(36), primary_key=True)

    # x-persistence: email unique, not null -> column user_email
    email: Mapped[str] = mapped_column("user_email", String(255), unique=True, nullable=False)

    # x-persistence: passwordHash -> password_hash, not null
    passwordHash: Mapped[str] = mapped_column("password_hash", String(255), nullable=False)

    # x-persistence: displayName -> display_name
    displayName: Mapped[Optional[str]] = mapped_column("display_name", String(50), nullable=True)

    # x-persistence: createdAt -> created_at timestamp with timezone
    createdAt: Mapped[datetime] = mapped_column("created_at", DateTime(timezone=True), nullable=False)
