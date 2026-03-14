# Feature: Modular Framework Logging (Console, SLF4J, and Future Providers)

## Overview

This feature implements the modular logging architecture defined in:

- [ADR-010: Modular Logging Framework With Pluggable Providers](../architecture/decisions/ADR-010-framework-logging-facade-and-slf4j-bridge.md)
- [Framework Logging Usage Guide](framework-logging-usage-guide.md)

Primary objective:

- make logging backend-pluggable at framework level
- support `console` and `slf4j` out of the box
- allow future logger providers without changing business/module callsites
- refactor existing `@Slf4j` in `store` to framework logger API

## Goals

- Introduce provider SPI for pluggable logging backends.
- Keep framework logger API stable for all modules.
- Provide built-in providers (`console`, `slf4j`).
- Add deterministic backend selection + fallback behavior.
- Refactor store callsites to framework logger usage.

## Scope

In scope:

- `framework` logger SPI + bootstrap/selection
- built-in console and SLF4J providers
- `framework` dependency on `slf4j-api` (no backend binding)
- `store` refactor of existing `@Slf4j` callsites in `src/main/java`
- `store` development console backend config (`logback-spring.xml`)
- tests for provider discovery/selection/delegation/fallback

Out of scope:

- refactor non-store modules in this iteration
- production log aggregation schema redesign
- tracing filter middleware implementation

## Target Design

### 1) Stable logging facade

Retain:

- `Logger`
- `LoggerFactory`
- `LogLevel`

Add:

- `Loggers` static bootstrap/entrypoint for callers

### 2) Provider SPI

Add interfaces/types:

- `com.grab.framework.logger.spi.LoggerProvider`
- `com.grab.framework.logger.spi.LoggerConfig`

Proposed contract:

```java
public interface LoggerProvider {
    String id();
    int priority();
    boolean isAvailable();
    LoggerFactory createFactory(LoggerConfig config);
}
```

`LoggerConfig` should include:

- backend id (`console`, `slf4j`, custom)
- default level
- key/value options map for provider-specific settings

### 3) Provider registry and resolution

Add internal bootstrap components:

- `LoggerProviderRegistry` (built-ins + ServiceLoader providers)
- `LoggerFactoryResolver` (select active provider)
- `LoggerConfigLoader` interface (config loading contract)

Bootstrap policy:

- framework does not provide a default `LoggerConfigLoader`
- application must set loader explicitly (`Loggers.setConfigLoader(...)`)

Selection algorithm:

1. read explicit backend from:
   - system property: `logger.backend`
   - env var: `LOGGER_BACKEND`
2. if explicit backend exists and provider is available, use it
3. otherwise choose highest-priority available provider
4. if none available, fallback to console provider

Optional strict mode:

- `logger.fail-on-missing-backend=true` to fail fast if explicit backend unavailable

### 4) Built-in providers

#### 4.1 Console provider

- `ConsoleLoggerProvider`
- Uses framework `ConsoleLogger` semantics (updated for robustness)
- Supports level from config:
  - `logger.level`
  - `LOGGER_LEVEL`

#### 4.2 SLF4J provider

- `Slf4jLoggerProvider`
- `Slf4jLoggerFactory`
- `Slf4jLogger` delegates methods to `org.slf4j.Logger`

### 5) External provider extensibility

Framework supports third-party providers by adding jar with:

- implementation of `LoggerProvider`
- service descriptor:
  - `META-INF/services/com.grab.framework.logger.spi.LoggerProvider`

No framework code changes required for new providers.

### 6) Store caller refactor

All existing `@Slf4j` in `store/src/main/java` are replaced with framework logger usage.

Refactor pattern:

1. remove:
   - `import lombok.extern.slf4j.Slf4j;`
   - `@Slf4j`
2. add:
   - `import com.grab.framework.logger.Logger;`
   - `import com.grab.framework.logger.Loggers;`
3. define:
   - `private static final Logger log = Loggers.getLogger(CurrentClass.class);`
4. keep existing log statements unchanged.

## Detailed Implementation Plan

### Phase 1: Framework SPI foundation

Files to add:

- `framework/src/main/java/com/grab/framework/logger/Loggers.java`
- `framework/src/main/java/com/grab/framework/logger/spi/LoggerProvider.java`
- `framework/src/main/java/com/grab/framework/logger/spi/LoggerConfig.java`
- `framework/src/main/java/com/grab/framework/logger/internal/LoggerProviderRegistry.java`
- `framework/src/main/java/com/grab/framework/logger/internal/LoggerFactoryResolver.java`
- `framework/src/main/java/com/grab/framework/logger/internal/LoggerConfigLoader.java`

Files to update:

- `framework/pom.xml` (add `slf4j-api`, test deps)
- `framework/src/main/java/com/grab/framework/logger/Logger.java` (Javadoc: `{}` formatting contract)

### Phase 2: Built-in providers

Files to add/update:

- `framework/src/main/java/com/grab/framework/logger/console/ConsoleLoggerProvider.java`
- `framework/src/main/java/com/grab/framework/logger/console/ConsoleLoggerFactory.java`
- `framework/src/main/java/com/grab/framework/logger/console/ConsoleLogger.java` (robust `{}` formatting + throwable handling)
- `framework/src/main/java/com/grab/framework/logger/slf4j/Slf4jLogger.java`
- `framework/src/main/java/com/grab/framework/logger/slf4j/Slf4jLoggerFactory.java`
- `framework/src/main/java/com/grab/framework/logger/slf4j/Slf4jLoggerProvider.java`

Notes:

- `console` and `slf4j` are built into `framework` registry.
- `ServiceLoader` is used for external providers only.

### Phase 3: Store caller migration

- Refactor all `@Slf4j` classes in `store/src/main/java` to `Loggers` usage.
- Do not alter business logic.
- Verify no `@Slf4j` remains in `store/src/main/java`.

### Phase 4: Store backend configuration

Add:

- `store/src/main/resources/logback-spring.xml`
- `store/src/main/resources/application.yml` (`logger.*` keys)
- `store/src/main/java/com/grab/store/shared/config/LoggerEnvironmentPostProcessor.java`
- `store/src/main/resources/META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor`

Use:

- console appender pattern with `traceId` MDC output
- local-friendly format (timestamp, level, logger, thread, message)
- install Spring-backed `LoggerConfigLoader` via `LoggerEnvironmentPostProcessor` so store owns logger config resolution

## Testing Strategy

### 1) Framework unit tests

Add tests for:

- provider registry collects built-in + ServiceLoader providers
- resolver selects explicit backend when available
- resolver fallback behavior when backend missing/unavailable
- strict mode fail-fast behavior
- SLF4J adapter delegation for all overloads
- console provider factory and level configuration behavior

Suggested files:

- `framework/src/test/java/com/grab/framework/logger/internal/LoggerFactoryResolverTest.java`
- `framework/src/test/java/com/grab/framework/logger/internal/LoggerProviderRegistryTest.java`
- `framework/src/test/java/com/grab/framework/logger/slf4j/Slf4jLoggerTest.java`
- `framework/src/test/java/com/grab/framework/logger/console/ConsoleLoggerProviderTest.java`

### 2) Extension compatibility test

Add a test-only fake provider via `META-INF/services` in test resources to validate plugin loading path.

### 3) Store verification

- compile and run tests after refactor
- verify startup with `logback-spring.xml`
- ensure no Lombok logging annotation remains in store main code

Commands:

- `./mvnw -pl framework test`
- `./mvnw -pl store -am -Dtest=GlobalApiExceptionHandlerIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false test`
- `./mvnw -pl store -am test`

Completeness checks:

- `rg -n "@Slf4j|lombok\.extern\.slf4j\.Slf4j" store/src/main/java` => no result
- `rg -n "com\.grab\.framework\.logger\.Loggers|private static final Logger log" store/src/main/java` => migrated usage present

## Rollout Plan

1. Deliver framework SPI + built-in providers with tests.
2. Migrate store callsites to framework logger.
3. Add/verify store backend config.
4. Publish usage guide and link docs for developer onboarding.
5. Run test suite and completeness checks.
6. Merge once acceptance criteria pass.

## Risks and Mitigations

- Risk: misconfigured backend id causes silent fallback.
  - Mitigation: strict mode option (`logger.fail-on-missing-backend=true`) to fail fast.
- Risk: provider conflicts (same id from multiple jars).
  - Mitigation: deterministic tie-break by priority, then class name.
- Risk: refactor introduces accidental non-logging edits.
  - Mitigation: mechanical refactor rules + focused review.

## Acceptance Criteria

- Framework supports runtime-selectable backend providers.
- Built-in `console` and `slf4j` providers are available and tested.
- Third-party provider loading via `ServiceLoader` is tested.
- `store` main code has no `@Slf4j` remaining.
- Store tests pass and console logging works in development.
