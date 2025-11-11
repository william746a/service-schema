from __future__ import annotations
from typing import Optional, List
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import select
from .domain import CustomerEntity, SubscriptionEntity


class CustomerRepository:
    def __init__(self, session: Session) -> None:
        self._session: Session = session

    def save(self, entity: CustomerEntity) -> CustomerEntity:
        self._session.add(entity)
        self._session.commit()
        self._session.refresh(entity)
        return entity

    def findById(self, customer_id: str) -> Optional[CustomerEntity]:
        stmt = (
            select(CustomerEntity)
            .options(joinedload(CustomerEntity.subscriptions))
            .where(CustomerEntity.customerId == customer_id)
            .limit(1)
        )
        res = self._session.execute(stmt).scalars().first()
        return res
