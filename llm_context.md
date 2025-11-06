# LLM Context: How to Generate Services from This Repository

This file provides practical context for any LLM that needs to generate a new service (or sample project) from the application specs in this repository. It complements `llm_context_schema.md` (which explains the schema) by documenting the concrete project structure, tech stack, conventions, and application configuration used in the existing samples.

## 1) Purpose and Scope
- Your task: read a spec file (e.g., `app-spec.billing.json` or `app-spec.ddd.json`) and generate a runnable sample service that faithfully implements the described API, persistence model, and business logic.
- Target output: a standalone Spring Boot application under `samples/<bounded-context>-svc` that compiles and runs with minimal setup.

## 2) Repository Layout (What you can rely on)
- Root files:
  - `business-spec.schema.ddd.json`: JSON Schema that validates extended DDD spec fields.
  - `app-spec.*.json`: Example application specifications (OpenAPI 3.0 + DDD extensions).
  - `llm_context_schema.md`: Explains how to interpret the business logic schema.
  - `llm_context.md` (this file): Practical generation guide.
- Samples:
  - `samples/user-management-svc`: A working Spring Boot sample showing mapping from spec → code for a User Management context.
  - `samples/billing-svc`: A working Spring Boot sample generated from `app-spec.billing.json` for the Billing context.
- Tools:
  - `tools/` contains generator-related tooling. Unless explicitly requested, do not modify or depend on this directory when generating new samples.

## 3) Tech Stack and Versions
- Language: Java 17
- Framework: Spring Boot 3.3.x (parent managed in each sample `pom.xml`)
- Dependencies typically used in samples:
  - spring-boot-starter-web (REST controllers)
  - spring-boot-starter-validation (Bean Validation/Jakarta for request/DTO validation)
  - spring-boot-starter-data-jpa (JPA/Hibernate persistence)
  - H2 database (runtime, in-memory)
  - Lombok (optional; keep code readable without heavy Lombok reliance)
  - spring-security-crypto (when password hashing or similar is needed)
- Build: Maven

## 4) Application Configuration (Defaults used by samples)
- Database: H2 in-memory with JPA/Hibernate auto DDL (create/update) for convenience.
- Ports (by convention in existing samples; adjust if needed):
  - `samples/user-management-svc`: 8080
  - `samples/billing-svc`: 8081
- Typical JPA/H2 properties (place in `src/main/resources/application.properties`):
  - spring.jpa.hibernate.ddl-auto=update
  - spring.h2.console.enabled=true (optional)
  - logging.level.org.hibernate.SQL=warn (or debug during development)

## 5) Project Structure for a Generated Sample
Create a new module under `samples/<bounded-context>-svc` with:
- `pom.xml`: Spring Boot module with the dependencies listed above.
- `src/main/java/...` packages (example for context `billing`):
  - `api`: REST controllers (maps OpenAPI `paths`).
  - `service`: Application services (maps `x-services` operations and `x-business-logic`).
  - `domain`: JPA entities that reflect `components/schemas` with `x-persistence`.
  - `repo`: Spring Data repositories for each aggregate/entity.
  - `events`: Domain events that operations may publish via Spring's `ApplicationEventPublisher`.
  - `security` or `gateway`/`integration`: Domain services or external gateways referenced by business logic steps (e.g., Stripe, password hashing).
  - `config` and `error` (optional): Exception handlers, configuration.
- `src/main/resources`:
  - `application.properties` for port, JPA, H2, etc.
- Root class: `<ContextName>Application` (e.g., `BillingApplication`).

## 6) Mapping Rules from Spec → Code
Follow these rules for consistency with existing samples.

- API (OpenAPI `paths`):
  - Each path/operation becomes a controller method.
  - Wire operation to service method specified by `x-service-operation` (e.g., `SubscriptionService.getSubscriptionByUserId`).
  - Use HTTP status codes per spec; map domain exceptions to appropriate HTTP codes via an `@ControllerAdvice` when needed.

- Services (`x-services` and `operations`):
  - Create one Spring `@Service` per named service (e.g., `UserService`, `BillingService`).
  - Implement each `operation` by tracing `x-business-logic.steps` in order:
    - validation → decision → mapping → persistence-call → domain-service-call → publish-event → throw → return
  - Publish any `emitsEvents` using Spring `ApplicationEventPublisher`.

- Entities (`components/schemas` with `x-persistence`):
  - Create JPA entities with table and column names from `x-persistence`.
  - Respect relations: one-to-one, one-to-many, many-to-one, many-to-many as described by `relation` fields.
  - Mark primary key fields and SQL data types as hints; map types appropriately in Java.
  - Aggregate roots (`isAggregateRoot: true`) get a primary repository; related entities are persisted via the root when applicable.

- DTOs (schemas without `x-persistence` and without `x-ddd.isValueObject`):
  - Create simple immutable DTOs. Prefer Java records where feasible, or POJOs with final fields and builders.

- Repositories:
  - Create Spring Data interfaces for each aggregate root and for any entity requiring queries used in `x-business-logic` steps.
  - Method names should reflect steps (e.g., `existsByEmail`, `findByCustomer_CustomerId`, `findByStripeCustomerId`).

- Domain Services / Gateways (`domain-service-call`):
  - Provide simple service/gateway classes/interfaces with methods referenced by steps.
  - For external integrations (e.g., Stripe), stubs are acceptable in samples (returning deterministic values) unless otherwise required.

- Events (`publish-event`):
  - Implement event payloads as classes or records under `events/`.
  - Publish via `ApplicationEventPublisher#publishEvent`.

## 7) Naming, Style, and Project Guidelines
- Follow repository guidelines:
  - Favor immutable data structures where possible.
  - Favor final classes, variables, and method parameters.
- Naming:
  - Classes and packages mirror spec names (e.g., `CustomerEntity`, `SubscriptionEntity`, `SubscriptionService`).
  - Repositories: `<EntityName>Repository` with descriptive method names.
  - Controllers: `<Feature>Controller` or `<Context>Controller` depending on surface area.
- Exceptions:
  - Throw domain-specific runtime exceptions (e.g., `NotFoundException`, `ConflictException`) and map via `@ControllerAdvice` to HTTP status codes (404, 409, etc.).

## 8) Examples from Existing Samples
- User Management (`samples/user-management-svc`):
  - Path POST /users → `api/UserController#createUser`
  - Service → `service/UserService#createUser` with validation, repository checks, password hashing, save, publish `UserCreatedEvent`, return DTO.
  - Entity → `domain/UserEntity` (table `users`), Repository → `repo/UserRepository` with `existsByEmail` and `save`.

- Billing (`samples/billing-svc`):
  - Path GET /subscriptions/{userId} → `api/SubscriptionController#getSubscription`
  - Path POST /webhooks/stripe → `api/SubscriptionController#handleStripe`
  - Services: `BillingService#handleUserCreated`, `SubscriptionService#getSubscriptionByUserId`, `SubscriptionService#handlePaymentWebhook`.
  - Entities: `CustomerEntity` (aggregate root, table `customers`, PK `customer_id`) with 1-1 `SubscriptionEntity` (table `subscriptions`, PK `sub_id`).
  - Repositories: `CustomerRepository` (existsByCustomerId, findByStripeCustomerId), `SubscriptionRepository` (findByCustomer_CustomerId).
  - Event: `CustomerCreatedEvent` published after save.
  - Gateway stub: `StripePaymentGateway` with `createCustomer` returning a mock id.

## 9) Build & Run (for generated samples)
- Prerequisites: Java 17+, Maven.
- Build: `mvn -q -DskipTests package`
- Run: `mvn spring-boot:run`
- Verify: Hit the documented endpoints; expect H2 in-memory persistence and JSON responses.

## 10) Do and Don’t
- Do:
  - Keep code minimal but complete; honor the spec’s `x-business-logic` steps exactly.
  - Prefer records or immutable DTOs.
  - Use final for fields, parameters, and classes where possible.
  - Provide clear README in each sample with curl examples and expected responses.
- Don’t:
  - Don’t couple new samples to `tools/` unless explicitly required.
  - Don’t invent endpoints, fields, or steps beyond the spec.

## 11) Notes for Portability
- The conventions above aim to make services independently runnable. If a spec references cross-context events (e.g., `UserCreatedEvent` consumed by Billing), represent them as DTOs and simple in-process events for samples.
- If a spec includes webhooks or external integrations, implement minimal stubs with deterministic outcomes to keep samples reliable.

## 12) Time and Provenance
- Current local date/time when this context was authored: 2025-11-05 15:25.
- This file describes conventions validated by existing samples in this repository and should be used as a guide when generating new ones from scratch.
