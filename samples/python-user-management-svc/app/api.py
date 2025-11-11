from __future__ import annotations
from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session
from .schemas import UserCreateDTO, UserResponseDTO
from .service import UserService
from .database import get_session

router = APIRouter()


@router.post("/users", response_model=UserResponseDTO, status_code=status.HTTP_201_CREATED)
def create_user(dto: UserCreateDTO, session: Session = Depends(get_session)) -> UserResponseDTO:
    service = UserService(session)
    return service.createUser(dto)
