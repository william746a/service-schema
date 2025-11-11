from __future__ import annotations
from abc import ABC, abstractmethod

class SubscriptionState(ABC):
    @property
    def status(self) -> str:
        return self.__class__.__name__.lower()

    @abstractmethod
    def cancel(self, subscription: "SubscriptionEntity") -> None:
        ...

    @abstractmethod
    def activate(self, subscription: "SubscriptionEntity") -> None:
        ...

    @abstractmethod
    def mark_past_due(self, subscription: "SubscriptionEntity") -> None:
        ...


class Active(SubscriptionState):
    def cancel(self, subscription: "SubscriptionEntity") -> None:
        subscription.state = Cancelled()

    def activate(self, subscription: "SubscriptionEntity") -> None:
        # Already active
        pass

    def mark_past_due(self, subscription: "SubscriptionEntity") -> None:
        subscription.state = PastDue()


class Cancelled(SubscriptionState):
    def cancel(self, subscription: "SubscriptionEntity") -> None:
        # Already cancelled
        pass

    def activate(self, subscription: "SubscriptionEntity") -> None:
        subscription.state = Active()

    def mark_past_due(self, subscription: "Subscription_Entity") -> None:
        # Cannot be past due if cancelled
        raise ValueError("Cannot mark a cancelled subscription as past due.")


class PastDue(SubscriptionState):
    def cancel(self, subscription: "SubscriptionEntity") -> None:
        subscription.state = Cancelled()

    def activate(self, subscription: "SubscriptionEntity") -> None:
        subscription.state = Active()

    def mark_past_due(self, subscription: "SubscriptionEntity") -> None:
        # Already past due
        pass

# Helper to map string status to state object
def get_state_from_status(status: str) -> SubscriptionState:
    if status == "active":
        return Active()
    elif status == "cancelled":
        return Cancelled()
    elif status == "pastdue":
        return PastDue()
    else:
        raise ValueError(f"Invalid status: {status}")
