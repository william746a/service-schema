# Node User Management Service

A minimal Node.js/Express implementation of the `UserManagement` bounded context derived from `app-spec.ddd.json`.

Implements:
- POST /users → validate input, check for existing email, hash password, persist, publish `UserCreatedEvent`, return response DTO.

Tech: Express, in-memory persistence, simple in-process event bus.

## Run

- Prereq: Node 18+
- Install: `npm install`
- Start: `npm start`
- Default port: 3000

## API

POST /users
- Body: `{ "email": "a@b.com", "password": "secret123", "displayName": "Alice" }`
- 201 response:
```
{
  "id": "...uuid...",
  "email": "a@b.com",
  "displayName": "Alice",
  "createdAt": "2025-01-01T00:00:00.000Z"
}
```
- 409 when email exists
- 400 when invalid input

## Notes
- Data is in-memory and resets on restart.
- Passwords are hashed with Node `crypto`.
