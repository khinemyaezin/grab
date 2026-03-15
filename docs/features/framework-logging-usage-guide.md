# Feature: Framework Logging Usage Guide

## Purpose

This guide explains how logging works in the project today and how application code should use it.

If you are new to the repo, the short version is:

- application code logs through `com.grab.framework.logger.Logger`
- classes create loggers with `Loggers.getLogger(...)`
- the logging backend is chosen at runtime
- `store` normally uses the `slf4j` backend and Logback formats the final output

Keep this mental model in mind:

- business code talks only to the framework logger API
- `framework` decides which logger backend is active
- `store` provides the runtime config and Logback output rules

Related docs:

- [ADR-010: Modular Logging Framework With Pluggable Providers](../architecture/decisions/ADR-010-framework-logging-facade-and-slf4j-bridge.md)
- [Framework Logging Implementation Notes](framework-logging-slf4j-bridge-implementation.md)
- [Modular Logging Framework Diagram](../architecture/diagrams/modular-logging-framework.md)

## 1) How to log in code

Use the framework facade directly in each class:

```java
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

private static final Logger log = Loggers.getLogger(CurrentClass.class);
```

This is the current convention used in `store/src/main/java`.

Typical usage:

```java
log.debug("Handling productId={}", productId);
log.info("Saved product: {}", productId);
log.warn("Variant {} is inactive for product {}", variantId, productId);
log.error("Failed to persist product {}", productId, exception);
```

Do not use Lombok `@Slf4j` in classes that already follow this pattern.

## 2) Message formatting rules

- Use `{}` placeholders for parameterized values.
- Do not use `String.format` placeholders such as `%s` or `%d`.
- Passing a trailing exception is supported.

Examples:

```java
log.info("Saving product: {}", productId);
log.warn("Variant {} is inactive", variantId);
log.error("Save failed for product {}", productId, exception);
```

The formatting contract is defined in:

- `framework/src/main/java/com/grab/framework/logger/Logger.java`

## 3) What happens when code calls `Loggers.getLogger(...)`

When a class calls:

```java
Loggers.getLogger(CurrentClass.class)
```

the flow is:

1. `Loggers` checks whether a `LoggerFactory` is already cached.
2. If a factory is already cached, it reuses it.
3. If not, it asks `LoggerFactoryResolver` to resolve one.
4. The resolver loads runtime config through `LoggerConfigLoader`.
5. The resolver picks the requested provider or the best available provider.
6. The chosen provider creates a `LoggerFactory`.
7. That factory creates the concrete logger instance for the class.

After the first resolution, `Loggers` caches the factory so later calls are cheap.

If you only remember one thing, remember this:

- callers do not choose Logback, console, or any other backend directly
- callers always ask `Loggers` for a logger
- backend choice is a bootstrap concern, not a business-code concern

## 4) How `store` configures logging

`store` is the application that owns runtime logger configuration.

The main properties live in:

- `store/src/main/resources/application.yml`

Current logging settings:

```yaml
logger:
  backend: ${LOGGER_BACKEND:slf4j}
  level: ${LOGGER_LEVEL:INFO}
  fail-on-missing-backend: ${LOGGER_FAIL_ON_MISSING_BACKEND:false}
  file:
    path: ${LOGGER_FILE_PATH:logs}
    name: ${LOGGER_FILE_NAME:store.log}
```

Spring installs the config loader through:

- `store/src/main/java/com/grab/store/shared/config/LoggerEnvironmentPostProcessor.java`
- `store/src/main/resources/META-INF/spring.factories`

This matters because the framework itself does not ship a default `LoggerConfigLoader`. If an application never installs one, logger bootstrap fails fast.

## 5) Current built-in backends

Two providers are built into `framework`:

- `slf4j`
- `console`

Current priority values are:

- `slf4j`: `200`
- `console`: `100`

Selection behavior:

1. If `logger.backend` is explicitly configured and available, use it.
2. If the explicit backend is unavailable and strict mode is enabled, fail fast.
3. Otherwise, choose the highest-priority available provider.
4. If no provider is available, fall back to `ConsoleLoggerFactory`.

## 6) Where output formatting actually happens

This is the most common point of confusion for new joiners:

- `Slf4jLogger` does not format logs itself. It delegates to `org.slf4j.Logger`.
- The final output format in `store` comes from Logback configuration in:
  - `store/src/main/resources/logback-spring.xml`

That file controls:

- console output
- file output
- rolling policy
- file path and file name
- MDC fields such as `traceId`

So the responsibility split is:

- `framework`: API, provider selection, fallback behavior
- `store`: environment properties, Spring bootstrap, Logback formatting

## 7) What the console backend does

If the `console` backend is selected, `ConsoleLogger` inside `framework` handles rendering directly.

Current behavior includes:

- log level filtering
- `{}` placeholder replacement
- trailing throwable detection
- appending leftover arguments as `extraArgs`

This makes it useful as a fallback and for simple standalone execution.

## 8) Adding another backend

To add a new backend:

1. implement `com.grab.framework.logger.spi.LoggerProvider`
2. return a `LoggerFactory` for that backend
3. register it in `META-INF/services/com.grab.framework.logger.spi.LoggerProvider`
4. select it with `logger.backend=<provider-id>`

Example skeleton:

```java
public final class JsonLoggerProvider implements LoggerProvider {
    @Override
    public String id() {
        return "json";
    }

    @Override
    public int priority() {
        return 300;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public LoggerFactory createFactory(LoggerConfig config) {
        return new JsonLoggerFactory(config);
    }
}
```

Then select it with:

```text
logger.backend=json
```

## 9) What a new joiner should open first

If you want the fastest path to understanding the current setup, read these files in order:

1. `framework/src/main/java/com/grab/framework/logger/Logger.java`
2. `framework/src/main/java/com/grab/framework/logger/Loggers.java`
3. `framework/src/main/java/com/grab/framework/logger/internal/LoggerFactoryResolver.java`
4. `store/src/main/java/com/grab/store/shared/config/LoggerEnvironmentPostProcessor.java`
5. `store/src/main/resources/application.yml`
6. `store/src/main/resources/logback-spring.xml`
