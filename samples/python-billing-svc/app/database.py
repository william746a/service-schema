from typing import Iterator
from sqlalchemy.orm import DeclarativeBase, Session

class Base(DeclarativeBase):
    pass


def get_session() -> Iterator[Session]:  # overridden by main.py at runtime
    raise RuntimeError("Session provider not configured. Override in main.py")
