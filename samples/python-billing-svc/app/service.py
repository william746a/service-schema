from __future__ import annotations
from typing import List
from sqlalchemy.orm import Session
from .repo import CustomerRepository
from .domain import CustomerEntity
from .schemas import UserCreatedEventDTO, SubscriptionResponseDTO, CustomerDTO, StripeEventDTO
from .error import NotFoundException
from .factories import CustomerFactory
from .adapters import StripeAdapter
from .patterns import PaymentGateway, WebhookStrategyFactory
from .strategies import InvoicePaidStrategy, SubscriptionDeletedStrategy, DefaultStrategy


class BillingService:
    def __init__(self, session: Session) -> None:
        self._customers = CustomerRepository(session)
        self._payment_gateway: PaymentGateway = StripeAdapter()
        self._webhook_strategy_factory = WebhookStrategyFactory([
            InvoicePaidStrategy(),
            SubscriptionDeletedStrategy(),
            DefaultStrategy()
        ])

    def handleUserCreated(self, eventDTO: UserCreatedEventDTO) -> CustomerEntity:
        new_customer = CustomerFactory.createCustomerFromEvent(eventDTO)
        return self._customers.save(new_customer)

    def getSubscriptions(self, customerId: str) -> List[SubscriptionResponseDTO]:
        customer = self._customers.findById(customerId)
        if customer is None:
            raise NotFoundException("Customer not found.")
        return [
            SubscriptionResponseDTO(
                subId=s.subId,
                status=s.status,
                planId=s.planId,
                expiresAt=s.expiresAt,
            )
            for s in customer.subscriptions
        ]

    def createStripeCustomer(self, customerDTO: CustomerDTO) -> CustomerDTO:
        stripe_id = self._payment_gateway.createCustomer(customerDTO)
        customer_to_update = self._customers.findById(customerDTO.id)
        if not customer_to_update:
            raise NotFoundException("Customer not found.")
        customer_to_update.stripeCustomerId = stripe_id
        updated_customer = self._customers.save(customer_to_update)
        return CustomerDTO(
            id=updated_customer.customerId,
            email=updated_customer.email,
            stripeId=updated_customer.stripeCustomerId
        )

    def handleStripeWebhook(self, event: StripeEventDTO) -> None:
        strategy = self._webhook_strategy_factory.get_strategy(event.type)
        strategy.handle(event.data)
