# ADR-003: MessageSource-Based Exception Handling Framework

## Status
Proposed (March 9, 2026)

## Context

Error handling across modules needs a consistent, low-coupling shape that:

- keeps domain/application errors typed and explicit
- preserves stable machine-readable error codes
- avoids exception-class explosion
- keeps domain independent from HTTP transport concerns
- supports safe refactoring with compile-time checks

The shared framework already provides core contracts:

- [`DomainException`](../../framework/src/main/java/com/grab/framework/exception/DomainException.java)
- [`MessageSource`](../../framework/src/main/java/com/grab/framework/exception/MessageSource.java)
- [`ErrorCategory`](../../framework/src/main/java/com/grab/framework/exception/ErrorCategory.java)
- [`MessageResolver`](../../framework/src/main/java/com/grab/framework/exception/MessageResolver.java)

## Decision

Adopt a standard exception pattern for all bounded contexts.

### 1. Module error model

Each module should implement:

1. One sealed `*Error` interface that implements `MessageSource`
2. Nested record error types with required arguments
3. One wrapper `*Exception` that extends `DomainException`
4. Domain/application code throws the wrapper with a typed error instance

Generic shape:

```java
public sealed interface ModuleDomainError extends MessageSource
        permits ModuleDomainError.InvalidState, ModuleDomainError.NotFound {

    record InvalidState(String current, String expected) implements ModuleDomainError {
        @Override public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }
        @Override public String code() { return "mod.domain.invalid_state"; }
        @Override public Map<String, Object> args() { return Map.of("current", current, "expected", expected); }
    }

    record NotFound(String id) implements ModuleDomainError {
        @Override public ErrorCategory kind() { return ErrorCategory.NOT_FOUND; }
        @Override public String code() { return "mod.domain.not_found"; }
        @Override public Map<String, Object> args() { return Map.of("id", id); }
    }
}
```

```java
public final class ModuleDomainException extends DomainException {
    public ModuleDomainException(ModuleDomainError error, String defaultMessage) {
        super(error, defaultMessage);
    }
}
```

```java
throw new ModuleDomainException(
    new ModuleDomainError.InvalidState("ARCHIVED", "ACTIVE"),
    "Invalid state transition."
);
```

### 2. API transport mapping

Use global `@RestControllerAdvice` to convert `DomainException` into RFC7807-style `ProblemDetail`.

Status mapping by `ErrorCategory`:

- `BUSINESS_RULE -> 422`
- `BAD_REQUEST -> 400`
- `NOT_FOUND -> 404`
- `CONFLICT -> 409`
- `INTERNAL -> 500`

Payload extensions must include:

- `code`
- `args`
- `detail`
- `status`
- `path`
- `timestamp`
- `traceId` (if available)

### 3. Error-code conventions

Use dot-notation codes:

- `<module>.domain.<reason>`
- `<module>.service.<reason>`
- `<module>.infra.<reason>`
- `shr.<area>.<reason>` for shared/system errors

Rules:

- codes are stable API contract
- codes do not contain human-readable prose
- `args` stay structured and non-sensitive

### 4. Testing requirements

Domain contract tests:

- each typed error verifies `kind`, `code`, `args`
- each business-rule violation throws expected wrapper exception

API contract tests:

- HTTP status matches `ErrorCategory`
- `ProblemDetail.code` and `args` match source error
- payload shape remains backward-compatible

## Consequences

### Positive

- consistent exception architecture across modules
- stable machine-readable contract for clients/integrations
- low coupling between domain errors and transport concerns
- better maintainability than method-per-error factory sprawl

### Negative

- initial migration effort from legacy exception styles
- requires discipline on code naming and `args` consistency
- needs test updates when error contracts evolve

## Alternatives Considered

### One exception class per error case

Rejected because:

- high class-count growth
- high ceremony for simple validation scenarios
- lower change velocity for evolving domains

### Raw `IllegalArgumentException` + ad-hoc message parsing

Rejected because:

- unstable client contract
- no structured `kind/code/args`
- poor localization and status mapping quality
