from __future__ import annotations
from datetime import datetime
from typing import List, Optional, Type
from sqlalchemy import String, DateTime, ForeignKey, TypeDecorator
from sqlalchemy.orm import Mapped, mapped_column, relationship
from .database import Base
from .states import SubscriptionState, get_state_from_status, Active


class StateType(TypeDecorator):
    impl = String(50)
    cache_ok = True

    def process_bind_param(self, value: SubscriptionState, dialect) -> str:
        return value.status

    def process_result_value(self, value: str, dialect) -> SubscriptionState:
        return get_state_from_status(value)


class CustomerEntity(Base):
    __tablename__ = "customers"

    customerId: Mapped[str] = mapped_column("customer_id", String(36), primary_key=True)
    email: Mapped[str] = mapped_column("email", String(255), unique=True)
    displayName: Mapped[Optional[str]] = mapped_column("display_name", String(255))
    stripeCustomerId: Mapped[Optional[str]] = mapped_column("stripe_customer_id", String(255), unique=True, nullable=True)

    subscriptions: Mapped[List["SubscriptionEntity"]] = relationship(
        back_populates="customer",
        cascade="all, delete-orphan",
    )


class SubscriptionEntity(Base):
    __tablename__ = "subscriptions"

    subId: Mapped[str] = mapped_column("sub_id", String(36), primary_key=True)
    _status: Mapped[SubscriptionState] = mapped_column("status", StateType, default=Active)
    planId: Mapped[str] = mapped_column(String(100))
    expiresAt: Mapped[datetime] = mapped_column(DateTime(timezone=True))

    customer_id: Mapped[str] = mapped_column(ForeignKey("customers.customer_id"))
    customer: Mapped[CustomerEntity] = relationship(back_populates="subscriptions")

    def __init__(self, **kw):
        super().__init__(**kw)
        self.state = self._status or Active()

    @property
    def state(self) -> SubscriptionState:
        return self._status

    @state.setter
    def state(self, new_state: SubscriptionState):
        self._status = new_state

    @property
    def status(self) -> str:
        return self.state.status

    def cancel(self):
        self.state.cancel(self)

    def activate(self):
        self.state.activate(self)
    
    def mark_past_due(self):
        self.state.mark_past_due(self)
