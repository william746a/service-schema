from __future__ import annotations
from typing import Optional
from sqlalchemy.orm import Session
from sqlalchemy import select
from .domain import UserEntity


class UserRepository:
    def __init__(self, session: Session) -> None:
        self._session: Session = session

    def existsByEmail(self, email: str) -> bool:
        stmt = select(UserEntity.id).where(UserEntity.email == email).limit(1)
        return self._session.execute(stmt).first() is not None

    def save(self, entity: UserEntity) -> UserEntity:
        self._session.add(entity)
        self._session.commit()
        self._session.refresh(entity)
        return entity
