from __future__ import annotations
from typing import List
from fastapi import APIRouter, Depends, Request
from sqlalchemy.orm import Session
from .schemas import SubscriptionResponseDTO, StripeEventDTO
from .service import BillingService
from .database import get_session

router = APIRouter()


@router.get("/subscriptions/{customerId}", response_model=List[SubscriptionResponseDTO])
def get_subscriptions(customerId: str, session: Session = Depends(get_session)) -> List[SubscriptionResponseDTO]:
    service = BillingService(session)
    return service.getSubscriptions(customerId)


@router.post("/webhooks/stripe")
async def handle_stripe_webhook(request: Request, session: Session = Depends(get_session)):
    service = BillingService(session)
    payload = await request.json()
    event = StripeEventDTO.model_validate(payload)
    service.handleStripeWebhook(event)
    return {"status": "ok"}
