from __future__ import annotations
import uuid
from .patterns import PaymentGateway
from .schemas import CustomerDTO


class StripeAdapter(PaymentGateway):
    def createCustomer(self, customer: CustomerDTO) -> str:
        print(f"Creating Stripe customer for {customer.email}")
        # In a real implementation, this would make an API call to Stripe
        return f"cus_{uuid.uuid4().hex[:14]}"
