from __future__ import annotations
from .patterns import WebhookStrategy


class InvoicePaidStrategy(WebhookStrategy):
    def supports(self, event_type: str) -> bool:
        return event_type == "invoice.paid"

    def handle(self, event_data: dict) -> None:
        print(f"Handling invoice.paid event: {event_data}")
        # Business logic for a paid invoice
        pass


class SubscriptionDeletedStrategy(WebhookStrategy):
    def supports(self, event_type: str) -> bool:
        return event_type == "customer.subscription.deleted"

    def handle(self, event_data: dict) -> None:
        print(f"Handling customer.subscription.deleted event: {event_data}")
        # Business logic for a deleted subscription
        pass


class DefaultStrategy(WebhookStrategy):
    def supports(self, event_type: str) -> bool:
        return True

    def handle(self, event_data: dict) -> None:
        print(f"Default handler for event: {event_data}")
        # Default business logic
        pass
