# Identity Security Architecture Diagram

This diagram describes the authentication, authorization, access context resolution,
and distributed security manifest synchronization architecture for the platform.

## Overview: Provider-Neutral Authentication Architecture

```mermaid
flowchart TD
    Client["Client (Mobile / Web)"]

    %% --- Key Components ---
    subgraph FilterLayer["Security Filter Chain"]
        AuthFilter["ProviderBearerAuthenticationFilter\n(Inspects Header & HttpOnly Cookies)"]
    end

    subgraph AuthLayer["Authentication & Identity"]
        JwtAuth["LocalJwtAccessTokenAuthenticator\n(Verifies RSA Signature & Claims)"]
        IdentityResolver["PlatformIdentityResolver / IdentityResolver\n(Resolves Actor & Scoped Entitlements)"]
        TokenLifeCycle["TokenLifeCycle\n(Handles Issuance & Refresh Rotation)"]
    end

    subgraph DataLayer["Infrastructure & Persistence"]
        KeyConfig["RSA Key Pair\n(Signing / Verification)"]
        UserDB[(Users, Roles, Authorities, Access Assignments)]
        SessionDB[(Refresh Sessions)]
        ManifestDB[(Security Manifests & Scopes)]
    end

    subgraph AppLayer["Application Layer"]
        Controllers["Secured Controllers & CQRS Handlers"]
    end

    %% --- Token Issuance Flow ---
    TokenLifeCycle -- "Signs JWT" --> KeyConfig
    TokenLifeCycle -- "Manages Family Rotation" --> SessionDB

    %% --- API Request Flow ---
    Client -- "1. Request (Bearer Token or Cookie)" --> AuthFilter
    
    AuthFilter -- "2. Validate Token" --> JwtAuth
    JwtAuth -. "Read Public Key" .-> KeyConfig
    JwtAuth -- "3. Valid ExternalPrincipal" --> IdentityResolver
    
    IdentityResolver -. "Fetch User & Scoped Effective Roles" .-> UserDB
    IdentityResolver -- "4. Return AuthenticatedActor" --> AuthFilter
    
    AuthFilter -- "5. Set SecurityPrincipal in SecurityContext" --> Controllers
```

---

## Authentication Flow

```mermaid
sequenceDiagram
    participant Client
    participant Filter as ProviderBearerAuthenticationFilter
    participant CookieHelper as AuthCookieHelper
    participant Auth as AccessTokenAuthenticator
    participant Resolver as PlatformIdentityResolver
    participant Identity as IdentityLookupQueryPort
    participant SC as SecurityContext
    participant Controller
    participant Handler

    Client->>Filter: HTTP Request (Bearer token or accessToken cookie)
    Filter->>Filter: Extract token from Authorization header or cookie
    alt Token Missing
        Filter->>Controller: Continue filter chain (anonymous access)
    else Token Present
        Filter->>Auth: authenticate(token)
        Auth->>Auth: Validate RSA signature, issuer, audience, expiry, type
        Auth-->>Filter: ExternalPrincipal (with optional AccessContext)
        Filter->>Resolver: resolve(externalPrincipal)
        Resolver->>Identity: resolveByPlatformUserId(issuer, subject, accessContext)
        Identity-->>Resolver: Platform user, active status, effective roles & authorities
        Resolver-->>Filter: AuthenticatedActor
        Filter->>SC: Set SecurityContext with SecurityPrincipal
        Filter->>Controller: Continue filter chain
        Controller->>Controller: @AuthenticationPrincipal SecurityPrincipal
        Controller->>Handler: Dispatch via CQRS CommandBus/QueryBus
        Handler-->>Controller: Result
        Controller-->>Client: HTTP Response
    end
```

---

## Login Flow

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant CommandService as AuthCommandService
    participant Bus as CommandBus
    participant Handler as LoginCommandHandler
    participant UseCase as LoginService
    participant UserRepo as UserRepository
    participant Hasher as PasswordHasher
    participant AssignRepo as AccessAssignmentRepository
    participant Resolver as PlatformIdentityResolver
    participant Issuer as TokenLifeCycle
    participant CookieHelper as AuthCookieHelper

    Client->>AuthController: POST /api/v1/identity/auth/login {email, password}
    AuthController->>CommandService: login(request)
    CommandService->>Bus: dispatch(LoginCommand)
    Bus->>Handler: handle(LoginCommand)
    Handler->>UseCase: execute(command)
    UseCase->>UserRepo: findByEmail(email)
    UserRepo-->>UseCase: User aggregate
    UseCase->>UseCase: Validate user is ACTIVE
    UseCase->>Hasher: verify(rawPassword, passwordHash)
    Hasher-->>UseCase: true

    UseCase->>AssignRepo: findEffectiveByUser(userId, now)
    AssignRepo-->>UseCase: List<AccessAssignment>

    alt Single Distinct Scope
        UseCase->>UseCase: Auto-select single scope context
    else Multiple Scopes or Zero Scopes
        UseCase->>UseCase: Context-free token (requires subsequent context selection)
    end

    UseCase->>Resolver: resolve(ExternalPrincipal with resolved context)
    Resolver-->>UseCase: AuthenticatedActor
    UseCase->>Issuer: issue(actor)
    Issuer-->>UseCase: TokenPair (access + refresh token)
    UseCase-->>Bus: AuthResult
    Bus-->>CommandService: AuthResult
    CommandService-->>AuthController: AuthResponse
    AuthController->>CookieHelper: createTokenCookies(...)
    AuthController-->>Client: 200 OK + Set-Cookie (accessToken, refreshToken) + AuthResponse body
```

---

## Registration Flow

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant CommandService as AuthCommandService
    participant Bus as CommandBus
    participant Handler as RegisterCommandHandler
    participant UseCase as RegisterService
    participant UserRepo as UserRepository
    participant Hasher as PasswordHasher
    participant EventPub as UserRegistrationIntegrationEventPublisher
    participant CustomerModule as CustomerBoundedContext

    Client->>AuthController: POST /api/v1/identity/auth/register {email, password}
    AuthController->>CommandService: register(request)
    CommandService->>Bus: dispatch(RegisterCommand)
    Bus->>Handler: handle(RegisterCommand)
    Handler->>UseCase: execute(RegisterCommand)
    UseCase->>UserRepo: findByEmail(email)
    UserRepo-->>UseCase: Optional.empty()
    UseCase->>Hasher: hash(rawPassword)
    Hasher-->>UseCase: HashedPassword
    UseCase->>UseCase: User.createLocal(id, email, password)
    UseCase->>UserRepo: save(user)
    UserRepo-->>UseCase: saved User
    UseCase-->>Bus: UserProfileResult
    Bus-->>CommandService: UserProfileResult
    CommandService-->>AuthController: UserProfileResponse

    Note over UseCase,EventPub: Domain event UserRegisteredEvent emitted
    EventPub->>EventPub: onUserRegistered(UserRegisteredEvent)
    EventPub-->>CustomerModule: UserRegisteredIntegrationEvent
    CustomerModule->>CustomerModule: AttachUserToCustomer / create customer profile

    AuthController-->>Client: 201 Created (UserProfileResponse)
```

---

## Distributed Security Manifest Synchronization Flow

```mermaid
sequenceDiagram
    participant ModulePub as {Module}SecurityManifestStartupPublisher
    participant Outbox as Transactional Outbox
    participant Listener as IdentitySecurityManifestRegistrationListener
    participant Bus as CommandBus
    participant Handler as RegisterSecurityManifestCommandHandler
    participant Service as RegisterSecurityManifestService
    participant Lock as SecurityCatalogLock
    participant Inbox as SecurityManifestInboxRepository
    participant Adapter as SecurityManifestCatalogRepositoryAdapter
    participant DB as Identity Database
    participant Memory as ScopeHierarchy (In-Memory)

    ModulePub->>Outbox: Produce SecurityManifestDeclaredIntegrationEvent
    Outbox-->>Listener: Receive SecurityManifestDeclaredIntegrationEvent
    Listener->>Listener: Verify module ownership against event class
    Listener->>Bus: dispatch(RegisterSecurityManifestCommand)
    Bus->>Handler: handle(command) [@IdentityTransactional]
    Handler->>Service: execute(command)

    Service->>Lock: lockNowait() (Pessimistic lock)
    Service->>Service: Verify SHA-256 content digest
    Service->>Inbox: Check idempotency & revision conflicts
    Service->>Service: Verify cross-module dependencies (minimum revision)
    Service->>Service: SecurityManifestValidator.validate()
    Service->>Adapter: catalog.apply(manifest)
    Adapter->>DB: Upsert Authorities & Apply Scopes
    Service->>DB: Record applied module revision & bump catalog revision
    Service->>Inbox: Record status = APPLIED
    Memory-->>Memory: Hierarchy available for scope encompasses checks
```

---

## Authorization: Endpoint Access Matrix

```mermaid
flowchart LR
    subgraph public["Public (No Auth)"]
        P1["POST /identity/auth/register"]
        P2["POST /identity/auth/login"]
        P3["POST /identity/auth/refresh"]
        P4["GET /api/v1"]
        P5["GET /catalog/products/*"]
        P6["GET /catalog/categories/**"]
        P7["POST /catalog/products/search"]
        P8["Swagger UI"]
    end

    subgraph moderation["Moderation Authorities"]
        A1["POST /products/*/approve"]
        A2["POST /products/*/reject"]
        A3["POST /products/*/suspend"]
        A4["POST /products/bulk/**"]
        A5["*/identity/admin/**"]
    end

    subgraph commerce["Scoped Commerce Authorities"]
        S1["POST/PUT/DELETE /products/** (MERCHANT_WRITE)"]
        S2["/inventory/** (INVENTORY_MANAGE)"]
        S3["POST /identity/access-contexts/{id}/select"]
    end

    subgraph authenticated["Any Authenticated"]
        AU1["GET /identity/profile"]
        AU2["GET /identity/access-contexts"]
    end
```

---

## Hexagonal Module Architecture & Dependencies

```mermaid
flowchart BT
    framework["framework\n(Core domain, Security types, CQRS)"]
    
    subgraph identityBC["Identity Bounded Context"]
        identityDomain["identity-domain\n(User, Role, AccessAssignment, Scopes)"]
        identityApp["identity-application\n(Use Cases, Commands, Queries, Services)"]
        identityPersistence["identity-adapter-persistence\n(JPA Entities, Repositories, Adapters)"]
    end

    subgraph otherBCs["Catalog, Merchant, Inventory Bounded Contexts"]
        otherDomain["{bc}-domain"]
        otherApp["{bc}-application"]
        otherPersistence["{bc}-adapter-persistence"]
    end

    outbox["outbox-infrastructure"]
    logger["logger-slf4j"]
    store["store\n(REST Controllers, Security Filters, Modulith Root)"]

    identityDomain --> framework
    identityApp --> identityDomain
    identityPersistence --> identityApp
    identityPersistence --> identityDomain

    otherDomain --> framework
    otherApp --> otherDomain
    otherPersistence --> otherApp
    otherPersistence --> otherDomain

    store --> identityPersistence
    store --> identityApp
    store --> identityDomain
    store --> otherPersistence
    store --> otherApp
    store --> otherDomain
    store --> outbox
    store --> logger
```

---

## Notes

- **Decoupled Roles**: The `User` aggregate does not store roles or refresh tokens. Roles and scopes are assigned dynamically via `AccessAssignment`.
- **HttpOnly Cookies & Bearer Tokens**: `ProviderBearerAuthenticationFilter` inspects both the `Authorization: Bearer` header and `accessToken` cookies, ensuring secure browser execution while supporting headless API clients.
- **Scope Hierarchy**: Scope parent-child relationships (e.g. `merchant.storefront` owned by `merchant.account`) are published via manifests and cached in `ScopeHierarchy` for zero-IO permission encompassing checks.
- **Provider Neutrality**: Token parsing emits `ExternalPrincipal`, resolved into `AuthenticatedActor` by `PlatformIdentityResolver`, allowing easy migration from local RSA JWTs to external OIDC providers (Keycloak, Auth0, Cognito).
