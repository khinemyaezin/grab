# Modular Logging Framework Diagram

This diagram describes the pluggable logging architecture for framework-level logging with built-in `console` and `slf4j` providers plus external provider extension.

## Architecture Overview

```mermaid
flowchart TB
    SpringCfg["store application.yml<br/>logger.*"] --> PostProcessor["LoggerEnvironmentPostProcessor"]
    PostProcessor --> LoaderInstall["Loggers.setConfigLoader(...)"]
    Caller["Module Class / Handler"] --> Facade["framework logger facade<br/>Loggers.getLogger(...)"]
    Facade --> Resolver["LoggerFactoryResolver"]
    Resolver --> Config["LoggerConfigLoader<br/>active loader strategy"]
    Resolver --> Registry["LoggerProviderRegistry"]

    Registry --> BuiltInConsole["ConsoleLoggerProvider"]
    Registry --> BuiltInSlf4j["Slf4jLoggerProvider"]
    Registry --> ServiceLoader["External Provider(s)<br/>ServiceLoader"]

    Config --> Select["Provider Selection<br/>explicit -> available highest priority -> fallback"]
    LoaderInstall --> Config
    Registry --> Select
    Select --> ActiveFactory["Active LoggerFactory"]
    ActiveFactory --> Logger["Logger"]

    Logger --> ConsoleOut["Console Output"]
    Logger --> Slf4jApi["SLF4J API"]
    Slf4jApi --> Backend["Runtime Binding<br/>Logback / Log4j2 / Other"]
```

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
    R->>REG: load providers (built-in + ServiceLoader)

    alt explicit backend configured and available
        R->>P: choose explicit provider id
    else explicit backend missing/unavailable
        R->>P: choose highest priority available
    else no providers available
        R->>P: fallback to console provider
    end

    P-->>R: createFactory(config)
    R-->>L: LoggerFactory
    L-->>C: Logger
    C->>OUT: log.info / log.error / log.debug
```

## Notes

- `framework` depends on `slf4j-api` only and does not ship a backend binding.
- `framework` does not ship a default `LoggerConfigLoader`; app bootstrap must install one.
- Applications choose backend bindings/configuration at runtime.
- New logger backends are added via `LoggerProvider` + `META-INF/services` without changing module callsites.
