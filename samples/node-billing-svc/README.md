# Node Billing Service

A minimal Node.js/Express implementation of the `Billing` bounded context derived from `app-spec.billing.json`.

Implements:
- GET /subscriptions/:userId → `SubscriptionService.getSubscriptionByUserId`
- POST /webhooks/stripe → `SubscriptionService.handlePaymentWebhook` (minimal stub)
- POST /events/user-created → `BillingService.handleUserCreated` (internal event endpoint)

Tech: Express, in-memory persistence, simple in-process event bus, deterministic Stripe gateway stub.

## Run

- Prereq: Node 18+
- Install: `npm install`
- Start: `npm start`
- Default port: 3001

## API

GET /subscriptions/{userId}
- 200 response `{ customerId, status, planId, expiresAt }` or 404

POST /webhooks/stripe
- Body: Stripe-like webhook payload with `type` and `data.customer`
- 200 OK

POST /events/user-created
- Body: `{ "userId": "...uuid...", "email": "a@b.com", "displayName": "Alice" }`
- Returns `{ "status": "created" }` or `{ "status": "ignored" }`

## Notes
- Data is in-memory and resets on restart.
- The Stripe gateway is a stub returning a deterministic id.
