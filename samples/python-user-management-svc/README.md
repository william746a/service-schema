Python User Management Service (FastAPI)

Implements the spec from app-spec.ddd.json for the UserManagement bounded context.

Features
- POST /users -> creates a user following x-business-logic steps.
- Publishes a UserCreatedEvent via an in-process event bus.
- SQLite database via SQLAlchemy; schema auto-created on startup.

Run locally
1) Create a virtual environment and install deps:
   python -m venv .venv
   .venv\Scripts\activate
   pip install -r requirements.txt

2) Start the service:
   uvicorn main:app --reload --port 8080

3) Try it
   curl -i -X POST http://localhost:8080/users \
     -H "Content-Type: application/json" \
     -d '{"email":"alice@example.com","password":"supersecret","displayName":"Alice"}'

   # expected: 201 with JSON body containing id, email, displayName, createdAt

Notes
- DTOs are immutable (Pydantic models with frozen=True).
- Entities map x-persistence (table/columns) from spec.
- Follows repo guidelines: prefer final/immutable data and parameters.
