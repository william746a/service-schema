
# LLM Context: Understanding the Business Logic Specification

## 1. High-Level Goal

This document provides context for interpreting application specifications (like `app-spec.ddd.json`) that are validated by the `business-spec.schema.ddd.json`.

The primary goal of this schema is to create a single, machine-readable source of truth for an entire application. It extends the OpenAPI 3.0 standard to unify three distinct layers of software development:

*   **API Contract (`paths`)**: The public-facing HTTP endpoints.
*   **Persistence Model (`x-persistence`)**: The database tables, columns, and relations.
*   **Business Logic (`x-services`)**: The explicit, step-by-step service-layer logic.

Your task as an LLM is to parse a spec file and use this unified information to understand the application's structure, behavior, and data models in their entirety.

## 2. Core Concepts & File Roles

*   **`business-spec.schema.ddd.json` (The Schema)**: This is the "dictionary" or "validator." It formally defines the structure and rules for all the custom `x-` extensions. You should use it to understand how a spec is structured.
*   **`app-spec.ddd.json` (The Specification)**: This is the "instance" or "blueprint." It's a concrete description of a specific application, written by a human, following the rules of the schema. This is the file you will most often be asked to read and interpret.

## 3. How to Interpret the Schema

When you are given a specification file (like `app-spec.ddd.json`), here is how to understand its key parts.

### A. Top-Level: `x-ddd`

*   **What it is**: This sets the high-level domain boundary.
*   **`boundedContext`**: This is the name of the microservice or application domain (e.g., "UserManagement", "Billing", "Shipping"). All components inside this file belong to this single context.

### B. The Models: `components/schemas`

This section defines all data structures. You must distinguish between three main types:

*   **Data Transfer Objects (DTOs)**
    *   **How to spot**: A schema with no `x-persistence` or `x-ddd` properties (e.g., `UserCreateDTO`, `UserResponseDTO`).
    *   **Purpose**: Plain data bags used for API requests and responses.
*   **Value Objects (VOs)**
    *   **How to spot**: A schema with `x-ddd: { "isValueObject": true }` (e.g., `UserEmailVO`).
    *   **Purpose**: Represents a "smart" piece of data that has its own validation and rules. It is defined by its attributes, not an ID, and is typically immutable.
*   **Entities**
    *   **How to spot**: A schema with an `x-persistence` block (e.g., `UserEntity`).
    *   **Purpose**: This is the core persistence model that maps directly to a database table.
    *   **`x-persistence` (on the object)**:
        *   `isEntity: true`: Confirms this is a database entity.
        *   `tableName`: The name of the SQL table.
        *   `isAggregateRoot: true`: This is a critical DDD concept. It means this entity is the "root" or "captain" of a data cluster. All operations on related objects (e.g., a User's Address) should go through this entity's repository.
    *   **`x-persistence` (on a property)**:
        *   `isPrimaryKey: true`: Marks the primary key field.
        *   `columnName`: The name of the database column.
        *   `dataType`: The SQL data type (e.g., `varchar(255)`, `uuid`).
        *   `relation`: Defines a database relationship (e.g., one-to-many, many-to-one).

### C. The Application Logic: `x-services`

*   **What it is**: This is the Application Service layer. It defines the application's features and orchestrates the business logic. It is the "entry point" from the API (`paths`) into the domain.
*   **`UserService` (etc.)**: The name of the service class.
*   **`operations`**: A list of all available methods in that service (e.g., `createUser`).
*   **`emitsEvents`**: A crucial property. This lists the Domain Events (e.g., `UserCreatedEvent`) that this operation publishes as a side-effect. This signals that other parts of the system may react to this event.

### D. The Step-by-Step Logic: `x-business-logic`

*   **What it is**: This is the "how-to" guide for a single service operation. It provides an explicit, ordered list of steps to execute.
*   **`steps`**: Read this array in order. Each object is an atomic instruction.
*   **Key Step Types (from the `type` property)**:
    *   `validation`: Check an input variable (`target`) against a list of rules.
    *   `decision`: An `if/then` block. Evaluate the `condition`. If true, execute the steps in the `ifTrue` array.
    *   `mapping`: Transform one data structure into another. Read the `source` variable, map it to the `targetSchema`, and store the result in the `variable`.
    *   `persistence-call`: Interact with the database. This step identifies the repository (e.g., `UserRepository`) and the method to call (e.g., `save`, `existsByEmail`).
    *   `domain-service-call`: Call a "specialist" service (a Domain Service) that contains complex, stateless logic (e.g., `PasswordSecurityService`).
    *   `publish-event`: Publish a Domain Event (`eventName`) to notify other parts of the system.
    *   `throw`: Stop execution and return an exception.
    *   `return`: The final step, returning the specified value (`variable`).

## 4. Your Task: How to Use This Context

When a user gives you a specification (like `app-spec.ddd.json`):

1.  Read the whole file to build a complete mental model.
2.  Identify the models in `components/schemas`. Classify them as DTOs, VOs, or Entities. For Entities, note the table names, columns, and especially which one is the `isAggregateRoot`.
3.  Identify the Application Services in `x-services`.
4.  To understand a feature (e.g., "create user"):
    1.  Find the API path (`/users` POST).
    2.  See its `x-service-operation` (`UserService.createUser`).
    3.  Go to `x-services.UserService.operations.createUser`.
    4.  Trace the `x-business-logic.steps` one by one. This is the exact logic flow you must follow.
5.  Use this structured trace to answer questions, generate code (Controllers, Services, Repositories, Entities), create test plans, or write documentation.
