Python Billing Service (FastAPI)

Implements the spec from app-spec.billing.json for the Billing bounded context.

Features
- GET /subscriptions/{customerId} -> returns all subscriptions for a customer.
- BillingService.handleUserCreated(eventDTO) available as an internal function to create a mirror customer on user creation.
- SQLite database via SQLAlchemy; schema auto-created on startup.

Run locally
1) Create a virtual environment and install deps:
   python -m venv .venv
   .venv\Scripts\activate
   pip install -r requirements.txt

2) Start the service:
   uvicorn main:app --reload --port 8081

3) Try it
   # Create a sample customer and subscription via shell or add a quick seed in code if needed.
   curl -i http://localhost:8081/subscriptions/00000000-0000-0000-0000-000000000001

Notes
- DTOs are immutable (Pydantic models with frozen=True).
- Entities map x-persistence (table/columns) from spec.
- Follows repo guidelines: prefer final/immutable data and parameters.
