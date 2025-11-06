# Service Generator CLI

A simple Java command-line tool that reads an app-spec.ddd.json file and generates a minimal Spring Boot service skeleton similar to samples/user-management-svc.

## Requirements

- JDK 21 (the build enforces Java 21 via Maven Enforcer)
- Maven 3.9+
- (Optional, for native build) GraalVM JDK 21 or Oracle/OpenJDK 21 + GraalVM Native Image toolchain available on PATH

## Build (JAR)

From the repository root:

```
cd tools/svc-generator-cli
mvn -q -DskipTests package
```

This will produce `target/svc-generator-cli-1.0.0.jar`.

## Build native executable (GraalVM)

With GraalVM Native Image available, run:

```
cd tools/svc-generator-cli
mvn -q -Pnative -DskipTests package
```

This will produce a native executable under `target/` named `svc-generator-cli` (on Windows it will be `svc-generator-cli.exe`).

## Usage

Using the JAR (generate a Spring Boot skeleton):
```
java -jar target/svc-generator-cli-1.0.0.jar \
  --spec "..\..\app-spec.ddd.json" \
  --out "..\..\samples\generated-user-management-svc" \
  --groupId com.example \
  --artifactId user-management-svc-generated \
  --package com.example.usermanagement
```

Using the native executable (PowerShell on Windows):
```
./target/svc-generator-cli.exe \
  --spec "..\..\app-spec.ddd.json" \
  --out "..\..\samples\generated-user-management-svc" \
  --groupId com.example \
  --artifactId user-management-svc-generated \
  --package com.example.usermanagement
```

- `--spec` Path to the spec (e.g., app-spec.ddd.json)
- `--out` Output directory for the generated project (or the output directory for SQL when using `--sql`)
- `--groupId` Maven groupId (default: com.example)
- `--artifactId` Maven artifactId (default: generated-service)
- `--package` Base Java package for generated code (defaults to `${groupId}.${boundedContext}`)

### SQL-only mode

You can generate SQL DDL for entities defined in the spec (ignoring everything else) using `--sql` (or `--generate-sql`). This creates a `schema.sql` file in the specified `--out` directory.

Examples (PowerShell on Windows):
```
# From repo root, generate SQL for the UserManagement spec
java -jar tools\svc-generator-cli\target\svc-generator-cli-1.0.0.jar \
  --spec app-spec.ddd.json \
  --out samples\user-management-svc\db \
  --sql

# Generate SQL for the Billing spec
java -jar tools\svc-generator-cli\target\svc-generator-cli-1.0.0.jar \
  --spec app-spec.billing.json \
  --out samples\billing-svc\db \
  --sql
```

What the SQL generation does:
- Reads `components/schemas` and selects only those with `x-persistence.isEntity=true`.
- Uses `tableName` for table names, and property-level `x-persistence` for `columnName`, `dataType`, `isNullable`, `isUnique`, and `isPrimaryKey`.
- Creates `FOREIGN KEY` constraints for relations that specify a `joinColumn` (the owning side), pointing to the target entity's primary key.
- Orders tables so referenced tables are created first when possible.

## What gets generated

- A minimal Spring Boot project (pom.xml, Application class)
- DTO classes derived from `components/schemas` that are not entities or value objects
- A REST controller for operations discovered in `paths` (based on `x-service-operation`)
- A Service class with stub methods for each operation
- `application.yml` and a short README
- In SQL-only mode: `schema.sql` file with `CREATE TABLE` statements for entities

## Notes

- This is a minimal generator meant to demonstrate parsing the DDD spec and materializing a runnable skeleton or SQL schema. It does not (yet) generate entities, repositories, persistence configuration, or the full step-by-step x-business-logic execution.
