# Modular Logging Framework Diagram

This page explains the current logging architecture in the repo.

If you are new to the project, read it in this order:

1. how application code enters through `Loggers.getLogger(...)`
2. how the resolver chooses the backend
3. which concrete logger object is finally created

Use this page for the runtime picture. If you want code-level usage rules, read `docs/features/framework-logging-usage-guide.md` next.

## Architecture Overview

```mermaid
flowchart TB
    SpringCfg["store application.yml<br/>logger.*"] --> PostProcessor["LoggerEnvironmentPostProcessor"]
    PostProcessor --> LoaderInstall["Loggers.setConfigLoader(...)"]
    Caller["Module Class / Handler"] --> Facade["framework logger facade<br/>Loggers.getLogger(...)"]
    Facade --> Resolver["LoggerFactoryResolver"]
    Resolver --> Config["LoggerConfigLoader<br/>active loader strategy"]
    Resolver --> Registry["LoggerProviderRegistry"]

    ServiceLoader --> Slf4jModule["logger-slf4j"]

    Config --> Select["Provider Selection<br/>explicit -> highest available -> no-op"]
    LoaderInstall --> Config
    Registry --> Select
    Select --> ActiveFactory["Active LoggerFactory"]
    Select --> NoOpFactory["NoOpLoggerFactory"]
    ActiveFactory --> Logger["Logger"]

    Logger --> ConsoleOut["Console Output"]
    Logger --> Slf4jApi["SLF4J API"]
    Slf4jApi --> Backend["Runtime Binding<br/>Logback / Log4j2 / Other"]
    Registry --> ServiceLoader["Adapter Provider(s)<br/>ServiceLoader"]

```

## Internal Class Diagram

```mermaid
classDiagram
    direction LR

    class Loggers {
        -LoggerFactory loggerFactory
        -LoggerConfigLoader configLoader
        +getLogger(Class) Logger
        +getLogger(String) Logger
        +getFactory() LoggerFactory
        +setConfigLoader(LoggerConfigLoader)
        +setFactory(LoggerFactory)
        +reset()
    }

    class Logger {
        <<interface>>
    }

    class LoggerFactory {
        <<interface>>
        +getLogger(String) Logger
        +getLogger(Class) Logger
    }

    class LoggerConfigLoader {
        <<interface>>
        +load() LoggerConfig
    }

    class LoggerFactoryResolver {
        -LoggerProviderRegistry providerRegistry
        -LoggerConfigLoader configLoader
        +resolveFactory() LoggerFactory
    }

    class LoggerProviderRegistry {
        -List~LoggerProvider~ providers
        +findById(String) Optional~LoggerProvider~
        +highestPriorityAvailable() Optional~LoggerProvider~
    }

    class LoggerProvider {
        <<interface>>
        +id() String
        +priority() int
        +isAvailable() boolean
        +createFactory(LoggerConfig) LoggerFactory
    }

    class LoggerConfig {
        +backend String
        +level LogLevel
        +failOnMissingBackend boolean
        +requiredBackend() String
        +effectiveLevel() LogLevel
    }

    class NoOpLoggerFactory
    class Slf4jLoggerProvider
    class Slf4jLoggerFactory
    class Slf4jLogger

    Loggers o--> LoggerFactory : caches active factory
    Loggers o--> LoggerConfigLoader : provided by app bootstrap
    Loggers ..> LoggerFactoryResolver : resolves on first use

    LoggerFactoryResolver --> LoggerConfigLoader : reads config
    LoggerFactoryResolver --> LoggerProviderRegistry : asks for provider
    LoggerProviderRegistry --> LoggerProvider : tracks providers

    Slf4jLoggerProvider ..|> LoggerProvider

    Slf4jLoggerFactory ..|> LoggerFactory

    Slf4jLogger ..|> Logger

    LoggerProvider --> LoggerFactory : createFactory()
    NoOpLoggerFactory ..|> LoggerFactory
    Slf4jLoggerFactory --> Slf4jLogger : creates
```

## How To Read The Class Diagram

- `Loggers` is the entry point used by application classes.
- `Loggers` does not create loggers directly. It resolves and caches a `LoggerFactory`.
- `LoggerFactoryResolver` decides which backend to use.
- `LoggerProviderRegistry` knows only about providers discovered through `ServiceLoader`.
- The chosen provider creates a `LoggerFactory`.
- That factory creates a concrete logger object such as `Slf4jLogger`.
- `Slf4jLogger` delegates to SLF4J. If nothing is configured, `NoOpLoggerFactory` keeps callers safe.

## Runtime Selection Sequence

```mermaid
sequenceDiagram
    autonumber
    participant C as Caller
    participant L as Loggers
    participant R as LoggerFactoryResolver
    participant CFG as LoggerConfigLoader
    participant REG as LoggerProviderRegistry
    participant SP as Spring PostProcessor
    participant P as Selected Provider
    participant F as LoggerFactory
    participant OUT as Backend Output

    SP->>L: setConfigLoader(SpringEnvironmentLoader)
    C->>L: getLogger(CurrentClass)
    L->>R: resolveFactory()
    R->>CFG: load backend + options
    R->>REG: load providers through ServiceLoader

    alt explicit backend configured and available
        R->>P: choose explicit provider id
    else explicit backend missing/unavailable
        R->>P: choose highest priority available
    else no providers available
        R->>F: use NoOpLoggerFactory
    end

    opt provider selected
        P-->>R: createFactory(config)
    end
    R-->>L: LoggerFactory
    L-->>C: Logger
    C->>OUT: log.info / log.error / log.debug
```

## Notes

- `framework` does not depend on `slf4j-api` and does not ship concrete backend adapters.
- `framework` does not ship a default `LoggerConfigLoader`; app bootstrap installs one when real backend selection is needed.
- Applications choose backend bindings and formatting at runtime.
- New logger backends are added via `LoggerProvider` plus `META-INF/services` without changing module callsites.
- In this repo, the common runtime path is `Logger` -> `Loggers` -> `logger-slf4j` -> Logback.
