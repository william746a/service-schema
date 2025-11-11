from fastapi import FastAPI, Depends, status
from fastapi.responses import JSONResponse
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session

from app.database import Base, get_session
from app import domain as _domain  # ensure models are registered
from app.api import router as api_router
from app.error import register_exception_handlers

DATABASE_URL = "sqlite:///./user_mgmt.db"

engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)

# Wire session dependency
def _session_dep() -> Session:
    session = SessionLocal()
    try:
        yield session
    finally:
        session.close()

# Initialize DB
Base.metadata.create_all(bind=engine)

app = FastAPI(title="User Management Service (Python)")

# Dependency override to use this module's SessionLocal
app.dependency_overrides[get_session] = _session_dep

# Routers
app.include_router(api_router)

# Exceptions
register_exception_handlers(app)


@app.get("/health")
def health():
    return {"status": "ok"}
