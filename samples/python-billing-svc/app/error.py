from fastapi import FastAPI
from fastapi.responses import JSONResponse

class NotFoundException(RuntimeError):
    pass


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(NotFoundException)
    async def _not_found_handler(_, exc: NotFoundException):
        return JSONResponse(status_code=404, content={"error": str(exc) or "Not Found"})
