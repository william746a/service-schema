from __future__ import annotations
from dataclasses import dataclass
from typing import Callable, Dict, List, Type, Any


@dataclass(frozen=True)
class UserCreatedEvent:
    userId: str
    email: str


class EventBus:
    def __init__(self) -> None:
        self._handlers: Dict[Type[Any], List[Callable[[Any], None]]] = {}

    def publish(self, event: Any) -> None:
        for etype, handlers in self._handlers.items():
            if isinstance(event, etype):
                for h in handlers:
                    h(event)

    def subscribe(self, event_type: Type[Any], handler: Callable[[Any], None]) -> None:
        self._handlers.setdefault(event_type, []).append(handler)


# Single in-process bus instance for the service
bus = EventBus()
