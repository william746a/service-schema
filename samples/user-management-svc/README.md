# User Management Sample Service

This is a minimal Spring Boot sample generated from the repository's app-spec.ddd.json (bounded context: UserManagement). It demonstrates how the DDD-oriented spec can be translated into runnable code.

Key mappings from spec → code:
- Path POST /users → api/UserController#createUser
- x-service-operation UserService.createUser → service/UserService#createUser
- Entity components.schemas.UserEntity (table: users) → domain/UserEntity (JPA)
- DTOs UserCreateDTO, UserResponseDTO → dto package
- Domain service PasswordSecurityService.hashPassword → security/PasswordSecurityService
- Repository UserRepository.existsByEmail/save → repo/UserRepository
- Event UserCreatedEvent → events/UserCreatedEvent (published after save)

## Build and run

Prerequisites: Java 17+, Maven.

- Build: mvn -q -DskipTests package
- Run: mvn spring-boot:run

The app starts on http://localhost:8080 with an in-memory H2 database.

## Try it

Create a user:

curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "P@ssw0rd!",
    "displayName": "Alice"
  }'

Expected 201 Created with a JSON body similar to:
{
  "id": "5e2b6e7e-...",
  "email": "alice@example.com",
  "displayName": "Alice",
  "createdAt": "2025-11-04T20:48:12.345Z"
}

Duplicate email gives 409 Conflict.
Validation errors (e.g., short password) give 400 with details.

## Notes
- createdAt is set automatically by JPA/Hibernate; we also initialize it to now() to reflect the spec mapping step.
- Passwords are hashed with BCrypt via Spring Security Crypto.
