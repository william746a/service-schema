# Billing Sample Service

This is a minimal Spring Boot sample generated from app-spec.billing.json (bounded context: Billing). It demonstrates how the DDD-oriented spec can be translated into runnable code.

Key mappings from spec → code:
- Path GET /subscriptions/{userId} → api/SubscriptionController#getSubscription
- Path POST /webhooks/stripe → api/SubscriptionController#handleStripe
- x-service-operation SubscriptionService.getSubscriptionByUserId → service/SubscriptionService#getSubscriptionByUserId
- x-service-operation SubscriptionService.handlePaymentWebhook → service/SubscriptionService#handlePaymentWebhook
- x-service BillingService.handleUserCreated (event-driven) → service/BillingService#handleUserCreated
- Entity components.schemas.CustomerEntity (table: customers, aggregate root) → domain/CustomerEntity (JPA)
- Entity components.schemas.SubscriptionEntity (table: subscriptions) → domain/SubscriptionEntity (JPA)
- DTO SubscriptionResponseDTO → dto/SubscriptionResponseDTO
- Input DTO for event UserCreatedEventDTO → dto/UserCreatedEventDTO
- Repository CustomerRepository.existsByCustomerId/save/findByStripeCustomerId → repo/CustomerRepository
- Repository SubscriptionRepository.findByCustomer_CustomerId → repo/SubscriptionRepository
- Domain service (gateway) StripePaymentGateway.createCustomer → gateway/StripePaymentGateway
- Event CustomerCreatedEvent → events/CustomerCreatedEvent (published after save)

## Build and run

Prerequisites: Java 17+, Maven.

- Build: mvn -q -DskipTests package
- Run: mvn spring-boot:run

The app starts on http://localhost:8081 with an in-memory H2 database.

## Try it

Get a subscription (will return 404 Not Found by default until data exists):

curl -s http://localhost:8081/subscriptions/00000000-0000-0000-0000-000000000000

Stripe webhook simulation (no-op, returns status ok):

curl -s -X POST http://localhost:8081/webhooks/stripe \
  -H "Content-Type: application/json" \
  -d '{
    "type": "invoice.payment_succeeded",
    "data": { "customer": "cus_test_123" }
  }'

## Notes
- This sample uses in-memory H2 with JPA schema auto-update.
- StripePaymentGateway is a stub that returns a mock Stripe customer id.
- BillingService#handleUserCreated follows the spec flow: validate → check exists → create Stripe customer → map and save → publish event → return status.
