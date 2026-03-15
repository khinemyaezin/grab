# ADR-010: Modular Logging Framework With Pluggable Providers

## Status
Proposed (March 11, 2026)

## Context

The project needs a logging framework that can adapt to multiple backends:

- built-in console logging for simple/local execution
- SLF4J-based logging for Spring Boot and ecosystem integration
- future backends without rewriting business/module logging callsites

Current state has mixed usage:

- `store` uses Lombok `@Slf4j`
- `framework` has custom logger contracts and a basic `ConsoleLogger`

This creates coupling and makes backend evolution inconsistent.

## Decision

Adopt a modular logging architecture in `framework` using a provider SPI.

### 1) Keep stable framework logging API

- `com.grab.framework.logger.Logger`
- `com.grab.framework.logger.LoggerFactory`

All modules should log through this API after migration.

### 2) Introduce backend provider SPI

Add a pluggable provider contract in `framework`:

- `LoggerProvider` (backend plugin contract)
- `LoggerConfig` (resolved backend + options)
- provider registry/bootstrap that selects one provider at runtime

Provider contract principles:

- provider has stable `id` (for example: `console`, `slf4j`, `log4j2-custom`)
- provider declares availability
- provider creates `LoggerFactory` for the backend

### 3) Provider discovery and selection

Backend resolution order:

1. explicit backend from config (`logger.backend` or `LOGGER_BACKEND`)
2. if not set, auto-select highest priority available provider
3. fallback to `console`

Provider discovery:

- Java `ServiceLoader` (`META-INF/services`) for external extensions
- built-in providers are always registered

### 4) Built-in providers in framework

Implement and ship:

- `ConsoleLoggerProvider`
- `Slf4jLoggerProvider`

Rules:

- `framework` depends on `slf4j-api` only
- `framework` must not include backend bindings
- app modules choose binding/config (for example Logback in `store`)

### 5) Store caller migration policy

Refactor existing `@Slf4j` callsites in `store/src/main/java` to framework logger usage:

```java
private static final Logger log = Loggers.getLogger(CurrentClass.class);
```

This keeps backend swaps transparent to store business code.

### 6) Compatibility and deprecation

- keep `ConsoleLogger` temporarily for compatibility
- mark direct `ConsoleLogger` usage as deprecated path
- preferred path is provider-based `Loggers` bootstrap

### 7) Formatting and context contract

- varargs logging follows SLF4J-style `{}` placeholders
- include MDC key `traceId` in app-level logging config when backend supports MDC

### 8) Store configuration ownership

- `store` Spring application owns runtime logger configuration via `logger.*` keys in `application.yml`
- `LoggerEnvironmentPostProcessor` installs a Spring-backed `LoggerConfigLoader` into framework bootstrap (`Loggers.setConfigLoader(...)`)
- framework intentionally has no default `LoggerConfigLoader`; startup fails fast if app loader is not installed
- effective precedence follows Spring `Environment` property source ordering

## Consequences

### Positive

- backend-pluggable logging with stable module callsites
- simple extension path for new logging backends
- framework-level standardization across modules
- app-level freedom to configure output/appenders

### Negative

- additional SPI/bootstrap complexity in framework
- requires tests for provider resolution and fallback behavior
- migration effort to replace existing `@Slf4j` callsites in `store`

## Alternatives Considered

### SLF4J only, no framework provider SPI

Rejected because:

- does not meet requirement to adapt any future backend through framework plugin model
- keeps backend strategy outside framework design boundary

### Keep only ConsoleLogger custom implementation

Rejected because:

- poor interoperability with Java ecosystem logging infrastructure
- weak extensibility for future backends

### Directly support many backends without SPI

Rejected because:

- hard-coded branching becomes rigid
- increases maintenance cost for every new backend
