from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse

class ConflictException(RuntimeError):
    pass


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(ConflictException)
    async def _conflict_handler(_, exc: ConflictException):
        return JSONResponse(status_code=409, content={"error": str(exc) or "Conflict"})
