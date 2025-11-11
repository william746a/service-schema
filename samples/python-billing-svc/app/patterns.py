from __future__ import annotations
from abc import ABC, abstractmethod
from .schemas import CustomerDTO, StripeEventDTO
from .domain import CustomerEntity


class PaymentGateway(ABC):
    @abstractmethod
    def createCustomer(self, customer: CustomerDTO) -> str:
        ...


class WebhookStrategy(ABC):
    @abstractmethod
    def supports(self, event_type: str) -> bool:
        ...

    @abstractmethod
    def handle(self, event_data: dict) -> None:
        ...


class WebhookStrategyFactory:
    def __init__(self, strategies: list[WebhookStrategy]):
        self._strategies = strategies

    def get_strategy(self, event_type: str) -> WebhookStrategy:
        for strategy in self._strategies:
            if strategy.supports(event_type):
                return strategy
        raise ValueError(f"No strategy found for event type {event_type}")
