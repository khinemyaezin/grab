# Feature: Framework Logging Usage Guide

## Purpose

This guide explains how application code should use the framework logging API after the modular logging refactor.

Related docs:

- [ADR-010: Modular Logging Framework With Pluggable Providers](../architecture/decisions/ADR-010-framework-logging-facade-and-slf4j-bridge.md)
- [Modular Framework Logging Implementation Plan](framework-logging-slf4j-bridge-implementation.md)

## 1) Logger usage in code

Use framework logger facade in classes:

```java
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

private static final Logger log = Loggers.getLogger(CurrentClass.class);
```

Do not use Lombok `@Slf4j` in migrated modules.

## 2) Message formatting contract

- Use `{}` placeholders for parameterized logs.
- Do not use `String.format` style placeholders (`%s`, `%d`) in logger messages.

Examples:

```java
log.info("Saving product: {}", productId);
log.warn("Variant {} is inactive for product {}", variantId, productId);
log.error("Failed to persist product {}", productId, exception);
```

## 3) Configuration source in `store` (Spring app)

`store` is the configuration owner for framework logger selection.

Set logger keys in:

- `store/src/main/resources/application.yml`

```yaml
logger:
  backend: ${LOGGER_BACKEND:slf4j}
  level: ${LOGGER_LEVEL:INFO}
  fail-on-missing-backend: ${LOGGER_FAIL_ON_MISSING_BACKEND:false}
```

`LoggerEnvironmentPostProcessor` installs a Spring-backed `LoggerConfigLoader` into framework bootstrap before application startup:

- `store/src/main/java/com/grab/store/shared/config/LoggerEnvironmentPostProcessor.java`
- `store/src/main/resources/META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor`

Resolution precedence follows Spring `Environment` ordering (for example: JVM/system properties, OS env vars, then `application.yml` defaults).

Framework does not provide a default loader; if an application does not install one, logger bootstrap fails fast.

## 4) Current built-in behavior

- Built-in providers: `slf4j`, `console`.
- If configured backend is unavailable and strict mode is disabled, resolver falls back to the highest-priority available provider.
- If no provider is available, resolver falls back to console logger.
- `store` has explicit dev console configuration in:
  - `store/src/main/resources/logback-spring.xml`
- Console pattern includes MDC trace key `traceId`.

## 5) Adding a new logger backend

Implement provider SPI:

- `com.grab.framework.logger.spi.LoggerProvider`

Package it with service registration file:

- `META-INF/services/com.grab.framework.logger.spi.LoggerProvider`

After adding provider jar to runtime classpath, select it via `logger.backend=<provider-id>`.

Example:

```java
public final class JsonLoggerProvider implements LoggerProvider {
    @Override
    public String id() { return "json"; }

    @Override
    public int priority() { return 300; }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public LoggerFactory createFactory(LoggerConfig config) {
        return new JsonLoggerFactory(config);
    }
}
```

`META-INF/services/com.grab.framework.logger.spi.LoggerProvider`:

```text
com.example.logging.JsonLoggerProvider
```
