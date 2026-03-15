# Feature: Framework Logging Implementation Notes

## Overview

This document explains the logging implementation that currently exists in the repo. It is not a future plan. It is the shape of the code today.

The design goals are:

- keep application code independent from a specific backend
- allow backend choice at runtime
- make caller code feel familiar to developers used to SLF4J

For a new joiner, the easiest summary is:

- code in modules logs through `Logger`
- `Loggers` bootstraps one active `LoggerFactory`
- the resolver decides whether that factory is backed by SLF4J or the console logger
- `store` supplies runtime config and, in the common case, Logback handles the final output

## Main pieces

The implementation is built from these parts:

- `Logger`: stable framework-facing logging interface
- `Loggers`: static entry point used by application code
- `LoggerFactoryResolver`: chooses the active backend
- `LoggerProviderRegistry`: tracks built-in and external providers
- `LoggerProvider`: SPI for backends
- `LoggerConfigLoader`: contract for runtime configuration loading
- `Slf4jLogger*` classes: adapter path for normal Spring Boot execution
- `ConsoleLogger*` classes: in-framework fallback logger path

## 1) Caller side

Application code does not log against raw SLF4J directly. It uses:

```java
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

private static final Logger log = Loggers.getLogger(CurrentClass.class);
```

This pattern is used throughout `store/src/main/java`.

The benefit is that caller code stays stable even if the active backend changes.

## 2) Lazy bootstrap and caching

`Loggers` does not create loggers directly. It caches a single active `LoggerFactory`.

Behavior:

1. the first call to `Loggers.getLogger(...)` triggers `getFactory()`
2. `getFactory()` checks whether a factory is already cached
3. if not, it resolves one through `LoggerFactoryResolver`
4. once resolved, the factory is cached and reused

This keeps backend selection centralized and avoids repeated resolution work.

There are also two utility methods mainly used in tests or explicit bootstrap:

- `Loggers.setFactory(...)`
- `Loggers.reset()`

## 3) Runtime configuration loading

The framework expects the application to provide a `LoggerConfigLoader`.

In this repo, `store` installs it through:

- `store/src/main/java/com/grab/store/shared/config/LoggerEnvironmentPostProcessor.java`
- `store/src/main/resources/META-INF/spring.factories`

The post-processor reads these properties from the Spring `Environment`:

- `logger.backend`
- `logger.level`
- `logger.fail-on-missing-backend`

and converts them into a `LoggerConfig`.

## 4) Backend selection logic

`LoggerFactoryResolver` is responsible for choosing the active backend.

Current logic:

1. load `LoggerConfig`
2. if an explicit backend is configured, try to find that provider by id
3. if the requested provider is available, use it
4. if the requested provider is unavailable and strict mode is enabled, throw
5. otherwise choose the highest-priority available provider
6. if no provider is available, create `ConsoleLoggerFactory` directly

This is why backend choice is deterministic and still safe when configuration is incomplete.

## 5) Provider registry behavior

`LoggerProviderRegistry` builds the provider list from:

- built-in providers
- external providers discovered with `ServiceLoader`

Current built-in providers are:

- `Slf4jLoggerProvider`
- `ConsoleLoggerProvider`

Current priorities are:

- `slf4j`: `200`
- `console`: `100`

If multiple providers share the same id, the registry keeps the higher-priority one. If priorities are equal, class name ordering is used as a deterministic tie-break.

## 6) The normal `store` runtime path

For the `store` application, the usual path is:

1. `Loggers` resolves `Slf4jLoggerFactory`
2. the factory creates `Slf4jLogger`
3. `Slf4jLogger` delegates to `org.slf4j.Logger`
4. Logback handles final formatting and output

That means:

- the framework provides the API and backend selection
- Logback provides appenders, rolling policy, and output format

Relevant files:

- `framework/src/main/java/com/grab/framework/logger/slf4j/Slf4jLoggerFactory.java`
- `framework/src/main/java/com/grab/framework/logger/slf4j/Slf4jLogger.java`
- `store/src/main/resources/logback-spring.xml`

This is the normal path you should assume unless config says otherwise.

## 7) The console fallback path

If the console backend is selected, the flow becomes:

1. `Loggers` resolves `ConsoleLoggerFactory`
2. the factory creates `ConsoleLogger`
3. `ConsoleLogger` formats and prints messages itself

`ConsoleLogger` currently supports:

- log level filtering
- `{}` placeholder replacement
- trailing throwable detection
- appending unused arguments as `extraArgs`

This makes the console path usable even without an external logging framework.

## 8) Responsibility split

`framework` owns:

- the logging API
- provider discovery
- backend selection
- built-in backends

`store` owns:

- runtime config values
- Spring bootstrap wiring
- Logback appenders and output formatting

This split is intentional. The framework stays backend-agnostic while the application controls runtime behavior.

That means when you debug a logging issue, ask two questions first:

1. Did `framework` choose the backend you expected?
2. If the backend is `slf4j`, is Logback configured the way you expected?

## 9) Why the project does not log with raw SLF4J everywhere

Using the framework facade gives the project:

- a stable API for all modules
- backend swapping without caller rewrites
- one place to handle fallback behavior
- an extension point for future providers

It also keeps framework modules from depending directly on a concrete backend configuration model.

## 10) Adding another backend

To add a new backend:

1. implement `com.grab.framework.logger.spi.LoggerProvider`
2. create a `LoggerFactory` for that backend
3. register the provider in `META-INF/services/com.grab.framework.logger.spi.LoggerProvider`
4. select it with `logger.backend=<provider-id>`

No caller code should need to change.

## Useful files for new joiners

Start here if you want the shortest path from API to runtime behavior:

- `framework/src/main/java/com/grab/framework/logger/Logger.java`
- `framework/src/main/java/com/grab/framework/logger/Loggers.java`
- `framework/src/main/java/com/grab/framework/logger/internal/LoggerFactoryResolver.java`
- `framework/src/main/java/com/grab/framework/logger/internal/LoggerProviderRegistry.java`
- `framework/src/main/java/com/grab/framework/logger/console/ConsoleLogger.java`
- `framework/src/main/java/com/grab/framework/logger/slf4j/Slf4jLogger.java`
- `store/src/main/java/com/grab/store/shared/config/LoggerEnvironmentPostProcessor.java`
- `store/src/main/resources/application.yml`
- `store/src/main/resources/logback-spring.xml`

## Useful tests

These tests are the fastest way to understand expected behavior:

- `framework/src/test/java/com/grab/framework/logger/LoggersTest.java`
- `framework/src/test/java/com/grab/framework/logger/internal/LoggerFactoryResolverTest.java`
- `framework/src/test/java/com/grab/framework/logger/internal/LoggerProviderRegistryTest.java`
- `framework/src/test/java/com/grab/framework/logger/slf4j/Slf4jLoggerTest.java`
- `framework/src/test/java/com/grab/framework/logger/console/ConsoleLoggerTest.java`
