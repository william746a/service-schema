from typing import Iterator
from sqlalchemy.orm import DeclarativeBase, Session

class Base(DeclarativeBase):
    pass

# This function type is used for dependency injection; main.py overrides it with a real Session factory

def get_session() -> Iterator[Session]:  # pragma: no cover - overridden at runtime
    raise RuntimeError("Session provider not configured. Override get_session dependency in main.py")
