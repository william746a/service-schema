from fastapi import FastAPI, Depends
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session

from app.database import Base, get_session
from app import domain as _domain  # ensure models registered
from app.api import router as api_router
from app.error import register_exception_handlers

DATABASE_URL = "sqlite:///./billing.db"

engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)


def _session_dep() -> Session:
    session = SessionLocal()
    try:
        yield session
    finally:
        session.close()


# Initialize DB (ensure models imported above)
Base.metadata.create_all(bind=engine)

app = FastAPI(title="Billing Service (Python)")
app.dependency_overrides[get_session] = _session_dep
app.include_router(api_router)
register_exception_handlers(app)


@app.get("/health")
def health():
    return {"status": "ok"}
