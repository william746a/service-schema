from __future__ import annotations
from dataclasses import asdict
from datetime import datetime, timezone
import uuid
from sqlalchemy.orm import Session

from .repo import UserRepository
from .domain import UserEntity
from .schemas import UserCreateDTO, UserResponseDTO
from .security import PasswordSecurityService
from .events import bus, UserCreatedEvent
from .error import ConflictException


class UserService:
    def __init__(self, session: Session) -> None:
        self._repo = UserRepository(session)

    def createUser(self, userDTO: UserCreateDTO) -> UserResponseDTO:
        # validation: FastAPI/Pydantic already validates DTO structure; enforce minimal
        # decision: check if email exists
        if self._repo.existsByEmail(userDTO.email):
            raise ConflictException("A user with this email already exists.")

        # domain-service-call: hash password
        hashedPassword: str = PasswordSecurityService.hashPassword(userDTO.password)

        # mapping: DTO -> UserEntity
        now = datetime.now(timezone.utc)
        new_user = UserEntity(
            id=str(uuid.uuid4()),
            email=userDTO.email,
            passwordHash=hashedPassword,
            displayName=userDTO.displayName,
            createdAt=now,
        )

        # persistence-call: save
        saved = self._repo.save(new_user)

        # mapping: event payload from saved user
        event_payload = UserCreatedEvent(userId=saved.id, email=saved.email)

        # publish-event
        bus.publish(event_payload)

        # mapping: entity -> response DTO
        resp = UserResponseDTO(
            id=saved.id,
            email=saved.email,
            displayName=saved.displayName,
            createdAt=saved.createdAt,
        )

        # return
        return resp
