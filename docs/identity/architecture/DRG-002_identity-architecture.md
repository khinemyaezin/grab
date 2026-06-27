# Identity Security Architecture Diagram

This diagram describes the authentication and authorization architecture for
the platform. It keeps the application contract stable while credential and
token infrastructure changes from self-hosted JWT to an external provider.

## Overview: Provider-Neutral Authentication Architecture

```mermaid
flowchart TD
    Client["Client (Mobile / Web)"]

    %% --- Key Components ---
    subgraph FilterLayer["Security Filter Chain"]
        AuthFilter["Authentication Filter\n(Intercepts Request)"]
    end

    subgraph AuthLayer["Authentication & Identity"]
        JwtAuth["JWT Authenticator\n(Verifies Signature)"]
        IdentityResolver["Identity Resolver\n(Builds User Principal)"]
        TokenIssuer["Token Issuer\n(Handles Login / Refresh)"]
    end

    subgraph DataLayer["Infrastructure & Persistence"]
        KeyConfig["RSA Key Pair\n(Signing / Verification)"]
        UserDB[(Users, Roles & Authorities)]
        SessionDB[(Refresh Sessions)]
    end

    subgraph AppLayer["Application"]
        Controllers["Secured Controllers & Services"]
    end

    %% --- Token Issuance Flow ---
    TokenIssuer -- "Signs JWT" --> KeyConfig
    TokenIssuer -- "Manages Family Rotation" --> SessionDB

    %% --- API Request Flow ---
    Client -- "1. Request + Bearer Token" --> AuthFilter
    
    AuthFilter -- "2. Validate Token" --> JwtAuth
    JwtAuth -. "Read Public Key" .-> KeyConfig
    JwtAuth -- "3. Valid Token Subject" --> IdentityResolver
    
    IdentityResolver -. "Fetch User & Entitlements" .-> UserDB
    IdentityResolver -- "4. Return Authenticated Actor" --> AuthFilter
    
    AuthFilter -- "5. Forward Request" --> Controllers
    
    %% Styling for clarity
    classDef filter fill:#f9f,stroke:#333,stroke-width:2px;
    classDef auth fill:#bbf,stroke:#333,stroke-width:2px;
    classDef db fill:#bfb,stroke:#333,stroke-width:2px;
    
    class AuthFilter filter;
    class JwtAuth,IdentityResolver,TokenIssuer auth;
    class UserDB,SessionDB,KeyConfig db;

```

## Authentication Flow

```mermaid
sequenceDiagram
    participant Client
    participant Filter as Spring Bearer Token Filter
    participant Auth as AccessTokenAuthenticator
    participant Resolver as PlatformIdentityResolver
    participant Identity as Platform Identity
    participant SC as SecurityContext
    participant Controller
    participant Handler

    Client->>Filter: HTTP Request with Bearer token
    Filter->>Filter: Extract token from Authorization header
    Filter->>Auth: authenticate(bearerToken)
    Auth->>Auth: Validate local JWT, OIDC JWT, or opaque token
    Auth-->>Filter: ExternalPrincipal
    Filter->>Resolver: resolve(externalPrincipal)
    Resolver->>Identity: Resolve identity, status, roles, authorities
    Identity-->>Resolver: Platform user and effective access
    Resolver-->>Filter: AuthenticatedActor
    Filter->>SC: Set SecurityContext with SecurityPrincipal
    Filter->>Controller: Continue filter chain
    Controller->>Controller: @AuthenticationPrincipal SecurityPrincipal
    Controller->>Handler: Dispatch via CQRS bus
    Handler-->>Controller: Result
    Controller-->>Client: HTTP Response
```

## Login Flow

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant CommandService as AuthCommandService
    participant Bus as CommandBus
    participant Handler as LoginCommandHandler
    participant Repo as UserRepository
    participant Hasher as PasswordHasher
    participant Issuer as Local TokenIssuer

    Client->>AuthController: POST /api/v1/identity/auth/login {email, password}
    AuthController->>CommandService: login(request)
    CommandService->>Bus: dispatch(LoginCommand)
    Bus->>Handler: handle(LoginCommand)
    Handler->>Repo: findByEmail(email)
    Repo-->>Handler: User aggregate
    Handler->>Handler: Validate status is ACTIVE
    Handler->>Hasher: verify(rawPassword, user.passwordHash)
    Hasher-->>Handler: true
    Handler->>Issuer: issue(AuthenticatedActor)
    Issuer-->>Handler: TokenPair(accessToken with roles[] claim, refreshToken, expiresIn)
    Handler-->>Bus: LoginResult
    Bus-->>CommandService: LoginResult
    CommandService-->>AuthController: AuthResponse
    AuthController-->>Client: 200 OK {accessToken, refreshToken, expiresIn, role}
```

## Registration Flow

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant CommandService as AuthCommandService
    participant Bus as CommandBus
    participant Handler as RegisterUserCommandHandler
    participant Repo as UserRepository
    participant Hasher as PasswordHasher
    participant Issuer as Local TokenIssuer

    Client->>AuthController: POST /api/v1/identity/auth/register {email, password, role}
    AuthController->>CommandService: register(request)
    CommandService->>Bus: dispatch(RegisterUserCommand)
    Bus->>Handler: handle(RegisterUserCommand)
    Handler->>Repo: existsByEmail(email)
    Repo-->>Handler: false
    Handler->>Hasher: hash(rawPassword)
    Hasher-->>Handler: HashedPassword
    Handler->>Handler: Create User aggregate (status based on role)
    Handler->>Repo: save(user)
    Repo-->>Handler: saved User

    alt Customer (ACTIVE)
        Handler->>Issuer: issue(AuthenticatedActor)
        Issuer-->>Handler: TokenPair
        Handler-->>Client: 201 {accessToken, refreshToken, expiresIn}
    else Seller (PENDING_APPROVAL)
        Handler-->>Client: 201 {message: "Pending admin approval"}
    end
```

## External Provider Flow (Future)

```mermaid
sequenceDiagram
    participant Client
    participant IdP as Keycloak
    participant Security as Spring Resource Server
    participant Auth as AccessTokenAuthenticator
    participant Resolver as PlatformIdentityResolver
    participant Identity as Platform Identity
    participant Controller

    Client->>IdP: Login / refresh / logout
    IdP-->>Client: Provider token pair
    Client->>Security: API request with access token
    Security->>Auth: Authenticate JWT or opaque bearer token
    Auth->>Auth: Use discovery/JWKS or token introspection
    Auth-->>Security: ExternalPrincipal
    Security->>Resolver: Resolve provider principal
    Resolver->>Identity: Resolve identity and provider-entitlement mappings
    Identity-->>Resolver: platformUserId, status, roles, authorities
    Resolver-->>Security: AuthenticatedActor
    Security->>Controller: Continue with the same application principal
```

Keycloak replaces local credential and token endpoints. It does not replace
the platform user, seller approval, local role-to-authority policy, or
resource-ownership checks.

## Authorization: Endpoint Access Matrix

```mermaid
flowchart LR
    subgraph public["Public (No Auth)"]
        P1["POST /auth/register"]
        P2["POST /auth/login"]
        P3["POST /auth/refresh"]
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

    subgraph commerce["Commerce Authorities"]
        S1["POST/PUT/DELETE /products/**"]
        S2["/inventory/**"]
    end

    subgraph authenticated["Any Authenticated"]
        AU1["GET /identity/profile"]
        AU2["All other endpoints"]
    end
```

The moderation routes require `PRODUCT_MODERATE` or the corresponding finer
authority. Product mutation requires `PRODUCT_WRITE_OWN` or
`PRODUCT_WRITE_ANY`; inventory mutation requires `INVENTORY_MANAGE_OWN` or
`INVENTORY_MANAGE_ANY`. Identity administration uses authorities such as
`SELLER_APPROVE`, `USER_SUSPEND`, and `ROLE_MANAGE`. Roles receive these
authorities through database mappings. Handlers separately verify that the
authenticated `platformUserId` owns seller-scoped resources.

## Module Dependency After Security

```mermaid
flowchart BT
    framework["framework"]
    identityDomain["identity-domain"]
    identityInfra["identity-infrastructure"]
    catalogDomain["catalog-domain"]
    catalogInfra["catalog-infrastructure"]
    inventoryDomain["inventory-domain"]
    inventoryInfra["inventory-infrastructure"]
    outbox["outbox-infrastructure"]
    logger["logger-slf4j"]
    store["store"]

    identityDomain --> framework
    identityInfra --> identityDomain
    catalogDomain --> framework
    catalogInfra --> catalogDomain
    inventoryDomain --> framework
    inventoryInfra --> inventoryDomain
    outbox --> framework
    logger --> framework
    store --> identityInfra
    store --> catalogInfra
    store --> inventoryInfra
    store --> outbox
    store --> logger
```

## Notes

- The `AuthenticatedActor` record lives in `framework`, allowing modules to
  consume identity without depending on Spring Security or an IdP SDK.
- `AccessTokenAuthenticator` hides token format and provider SDKs. Its
  implementations validate local JWTs, OIDC JWTs through discovery/JWKS, or
  OAuth2 opaque tokens through introspection.
- Every authenticator returns the same `ExternalPrincipal`; raw JWT claims and
  introspection responses do not enter controllers or handlers.
- The API accepts bearer access tokens only. OIDC ID tokens are not valid API
  access tokens.
- `PlatformIdentityResolver` supports configurable realm-role, client-role,
  group, scope, or introspection-field mappings and hides those formats from
  the application.
- `(issuer, subject)` resolves to a stable local `platformUserId`; email is not
  used as a permanent identity key.
- Local roles and role-to-authority mappings remain the commerce authorization
  source after Keycloak adoption.
- `TokenIssuer` is local identity infrastructure. Keycloak replaces local
  login, issuance, refresh, and logout rather than implementing that port.
- Error responses for 401 and 403 follow the existing RFC 7807 ProblemDetail
  format from `GlobalApiExceptionHandler`.
- Dashed lines represent future external JWT/JWKS or opaque-token
  introspection infrastructure.
