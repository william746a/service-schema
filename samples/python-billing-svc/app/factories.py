from __future__ import annotations
from .domain import CustomerEntity
from .schemas import UserCreatedEventDTO


class CustomerFactory:
    @staticmethod
    def createCustomerFromEvent(event: UserCreatedEventDTO) -> CustomerEntity:
        return CustomerEntity(
            customerId=event.userId,
            email=event.email,
            displayName=event.displayName,
        )
