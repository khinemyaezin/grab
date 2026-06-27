# Exception Handling Framework Flow

This diagram describes the framework-level exception flow from request handling to RFC7807 response generation.

## End-to-End Flow

```mermaid
flowchart TB
    Client["Client"] --> Controller["Controller / Endpoint"]
    Controller --> App["Application Handler (Command/Query)"]

    subgraph DomainPath["Module Exception Sources"]
        App --> DomainRules["Domain Invariant or Validation"]
        App --> ServiceRules["Service Rule Check"]
        App --> Persistence["PersistenceExecutor"]

        DomainRules -- "violation" --> DomEx["ModuleDomainException<br/>(ModuleDomainError)"]
        ServiceRules -- "violation" --> SvcEx["ModuleServiceException<br/>(ModuleServiceError)"]
        Persistence -- "integrity violation" --> InfraConflict["ModuleInfraException<br/>(module.infra.persistence.conflict)"]
        Persistence -- "data access failure" --> InfraInternal["ModuleInfraException<br/>(module.infra.persistence.internal)"]
    end

    subgraph SharedPath["Shared Request/System Sources"]
        Controller --> BeanValidation["MethodArgumentNotValidException or ConstraintViolationException"]
        Controller --> Malformed["HttpMessageNotReadableException"]
        Controller --> Unexpected["Unhandled Exception"]

        BeanValidation --> SharedValidation["Shared RequestValidation Error"]
        Malformed --> SharedMalformed["Shared MalformedJson Error"]
        Unexpected --> SharedUnexpected["Shared InternalUnexpected Error"]
    end

    DomEx --> Advice["GlobalApiExceptionHandler"]
    SvcEx --> Advice
    InfraConflict --> Advice
    InfraInternal --> Advice
    SharedValidation --> Advice
    SharedMalformed --> Advice
    SharedUnexpected --> Advice

    Advice --> StatusMap["Map ErrorCategory to HTTP Status<br/>BAD_REQUEST=400, NOT_FOUND=404,<br/>CONFLICT=409, BUSINESS_RULE=422, INTERNAL=500"]
    StatusMap --> ResolveDetail["Resolve detail via MessageResolver<br/>(fallback to exception message)"]
    ResolveDetail --> Problem["Build ProblemDetail + extensions:<br/>code, args, traceId, path, timestamp,<br/>module, retryable, retryAfterMs"]
    Problem --> Response["HTTP ProblemDetail Response"]
```

## Request Exception Lifecycle (Sequence)

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as Controller
    participant H as Command/Query Handler
    participant D as Domain/Service
    participant P as PersistenceExecutor
    participant A as GlobalApiExceptionHandler

    C->>API: HTTP request
    API->>H: Execute use case
    H->>D: Apply business rules

    alt Domain or service violation
        D-->>H: throw DomainException wrapper
        H-->>A: propagate exception
    else Persistence conflict
        H->>P: command(resource, operation)
        P-->>H: throw ModuleInfraException(conflict)
        H-->>A: propagate exception
    else Persistence internal error
        H->>P: command/query(resource, operation)
        P-->>H: throw ModuleInfraException(internal)
        H-->>A: propagate exception
    else Request validation/malformed JSON
        API-->>A: Spring validation/read exception
    else Unexpected exception
        H-->>A: unhandled exception
    end

    A->>A: Map ErrorCategory -> HTTP status
    A->>A: Resolve detail via MessageResolver
    A->>A: Build ProblemDetail extensions\n(code,args,traceId,path,timestamp,module,retryable,retryAfterMs)
    A-->>C: HTTP ProblemDetail response
```

## Notes

- All module wrappers extend `DomainException` and carry a typed `MessageSource`.
- `GlobalApiExceptionHandler` centralizes transport mapping so domain and infrastructure code stay HTTP-agnostic.
- Retry metadata is attached only for configured retryable internal errors.
