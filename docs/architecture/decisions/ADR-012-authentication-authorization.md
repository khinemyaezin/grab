# ADR-012: Authentication and Authorization

## Status
Proposed

## Context

The platform currently has no authentication or authorization. All REST
endpoints are publicly accessible. Actor identity is passed through an
`X-Actor-Id` request header on some inventory endpoints, but the value is
never validated and any caller can impersonate any user.

The platform needs:

- verified identity for every state-changing request
- role-based access control for admin, seller, and customer personas
- dynamic role management so new roles and their authority assignments can be
  added without code changes
- public read access for storefront product browsing
- a design that allows migrating from self-hosted JWT to an external identity
  provider (Keycloak, Auth0, Firebase Auth, AWS Cognito) without changing
  business logic or controllers

---

## Decision

### 1. Introduce a provider-neutral authenticated actor in the framework module

Every module receives the same authenticated actor without depending on Spring
Security, a JWT library, or any specific identity provider.

```java
public record AuthenticatedActor(
    String platformUserId,
    String issuer,
    String subject,
    String email,
    Set<String> roles,
    Set<String> authorities
) {}

public interface AccessTokenAuthenticator {
    ExternalPrincipal authenticate(String bearerToken);
}

public record ExternalPrincipal(
    String issuer,
    String subject,
    Optional<String> email,
    Set<String> entitlements
) {}

public interface PlatformIdentityResolver {
    AuthenticatedActor resolve(ExternalPrincipal principal);
}
```

`platformUserId` is the stable commerce-platform identity used for resource
ownership. `issuer` and `subject` identify the account at the credential
provider. For locally issued tokens, `subject` may equal `platformUserId`; an
external provider is not required to use the same identifier.

`AuthenticatedActor` uses string sets instead of Java enums. This is critical
for external IdP compatibility and dynamic authorization:

- External IdPs (Keycloak, Auth0, Cognito) represent roles as string-based
  groups or scopes, not Java enums.
- A user may hold multiple roles simultaneously (e.g., both `ADMIN` and
  `SELLER`).
- New roles and their authority mappings can be created at runtime.
- Provider-specific claims are normalized before application code sees them.

`AccessTokenAuthenticator` hides whether a bearer token is a locally issued
JWT, an OIDC JWT validated through discovery/JWKS, or an OAuth2 opaque token
validated through introspection. `PlatformIdentityResolver` maps the verified
external principal to the local platform user, roles, and authorities.
`ExternalPrincipal.entitlements` is a normalized set populated from provider
roles, groups, scopes, or equivalent introspection fields. Email is optional
because it is not guaranteed in an OAuth2 access token.

Controllers and handlers depend only on `AuthenticatedActor` and stable
authority codes. They never receive raw tokens, JWT claims, OAuth2
introspection responses, or provider SDK types.

### 2. Use provider-neutral bearer-token authentication

The initial implementation uses:

- JJWT for local token generation
- a local JWT implementation of `AccessTokenAuthenticator`
- Spring Security OAuth2 resource-server support for external JWT validation
  and opaque-token introspection
- an asymmetric signing key with `kid`, issuer, and audience claims
- BCrypt for password hashing
- local `users`, `roles`, `authorities`, assignment/mapping, refresh-session,
  and external-identity tables in a dedicated `identity` database
- Spring Security's `SecurityFilterChain` for request-level authorization

The security filter passes the bearer token to `AccessTokenAuthenticator`.
The selected implementation returns a normalized `ExternalPrincipal`:

- local JWT: validate with the configured public key, issuer, and audience
- OIDC/OAuth2 JWT: validate through provider discovery and JWKS
- OAuth2 opaque token: validate through the provider introspection endpoint

Only access tokens authenticate API requests. OIDC ID tokens are client-facing
identity assertions and must not be accepted as commerce API access tokens.

`PlatformIdentityResolver` then resolves `(issuer, subject)`, checks platform
status, maps external entitlements when present, and calculates effective authorities
before placing an `AuthenticatedActor` in the `SecurityContext`.

Local token lifecycle operations are deliberately separate from access-token
verification:

```java
public interface TokenIssuer {
    TokenPair issue(AuthenticatedActor actor);
    TokenPair refresh(String refreshToken);
    void revoke(String refreshToken);
}

public record TokenPair(
    String accessToken,
    String refreshToken,
    long expiresInMs
) {}
```

`TokenIssuer` belongs to identity application/infrastructure code, not the
shared framework. When Keycloak is adopted, Keycloak owns login, issuance,
refresh, and logout; protected application modules remain unchanged.

### 3. Use dynamic, database-driven roles

Roles are stored in a `roles` table and assigned to users through a
`user_roles` join table. Stable authorities are stored separately and mapped
through `role_authorities`. This provides:

- **Runtime role management**: admins can create roles, edit display details,
  deactivate roles, and assign authorities without redeployment.
- **Multiple roles per user**: a user can simultaneously hold `ADMIN` and
  `SELLER` roles.
- **External IdP compatibility**: Keycloak roles or groups can be mapped to
  platform roles while commerce authorization remains locally controlled.
- **Future extensibility**: new roles like `WAREHOUSE_MANAGER`,
  `CATALOG_MODERATOR`, `FINANCE_ADMIN`, or `SUPPORT_AGENT` can be added
  by an admin through the API.

The platform ships with three seed roles: `ADMIN`, `SELLER`, `CUSTOMER`.
These are seeded at application startup but are not hardcoded as a Java enum.

```text
┌─────────┐       ┌────────────┐       ┌──────────┐
│  users  │──M:N──│ user_roles │──M:N──│  roles   │
└─────────┘       └────────────┘       └──────────┘
```

Role codes are immutable machine identifiers. Names and descriptions are
editable. Endpoint and method rules use stable authority codes such as
`PRODUCT_WRITE_OWN`, `PRODUCT_WRITE_ANY`, `PRODUCT_MODERATE`,
`INVENTORY_MANAGE_OWN`, `INVENTORY_MANAGE_ANY`, and `SELLER_APPROVE`.
Consequently, a newly created role becomes useful by assigning existing
authorities without changing endpoint code.

### 4. Create a new identity bounded context

Following the established pattern for bounded contexts, identity gets its own
domain and infrastructure modules:

```text
identity-domain/         → User and Role aggregates, Authority entity,
                           external identity link, Email/HashedPassword value
                           objects, repositories, PasswordHasher interface
identity-infrastructure/ → JPA entities, mappers, repository impls,
                           BcryptPasswordHasher, separate DataSource
```

The identity module in `store` follows the same CQRS pattern as catalog and
inventory: controllers delegate to command/query services, which dispatch
through the command/query bus to handlers.

### 5. Use Spring Security for request-level authorization

Spring Security provides the filter chain, URL-pattern-based authorization,
method-level security (`@PreAuthorize`), and standardized entry points for
401/403 responses.

The `SecurityFilterChain` defines public versus authenticated routes. Stable
authority checks protect business operations:

- **Public endpoints**: auth routes, product read endpoints, category
  browsing, Swagger UI
- **Moderation authorities**: product approval, rejection, suspension, and
  bulk operations
- **Management authorities**: user, seller, role, and authority administration
- **Seller authorities**: product and inventory operations
- **Authenticated endpoints**: everything else

Handlers additionally enforce resource ownership using
`AuthenticatedActor.platformUserId`. A role or authority match alone never
allows one seller to mutate another seller's resources.

### 6. Replace X-Actor-Id with SecurityContext

All controllers currently using `@RequestHeader("X-Actor-Id")` will be
migrated to `@AuthenticationPrincipal SecurityPrincipal principal`, where
`SecurityPrincipal` wraps `AuthenticatedActor` as a Spring principal.

### 7. Extend ErrorCategory for security errors

Two new error categories are added to the framework:

- `UNAUTHORIZED` → HTTP 401
- `FORBIDDEN` → HTTP 403

The `GlobalApiExceptionHandler` is updated to handle Spring Security's
`AccessDeniedException` and `AuthenticationException`, returning responses in
the existing RFC 7807 ProblemDetail format.

### 8. Use a separate database for identity

Following the per-module datasource pattern established in ADR-001, the
identity module gets its own PostgreSQL database, entity manager factory, and
transaction manager.

Custom meta-annotations:

- `@IdentityTransactional` — wraps `@Transactional("identityTransactionManager")`
- `@IdentityReadTransactional` — wraps `@Transactional(transactionManager = "identityTransactionManager", readOnly = true)`

### 9. Retain the platform user with an external identity provider

The commerce platform remains the source of truth for seller approval,
suspension, platform roles, authority mappings, and resource ownership. An
external provider owns credentials and authentication factors.

External accounts are linked to platform users through an
`external_identities` table with a unique `(issuer, subject)` pair. Email is
profile data and is not used as the permanent provider identity.

---

## Future Migration Path

When migrating to an external identity provider, the changes are isolated:

| Component | Self-Hosted (Now) | External IdP (Later) |
|---|---|---|
| `AccessTokenAuthenticator` | Local JWT implementation | OIDC JWT/JWKS or OAuth2 introspection implementation |
| Token issuance | JJWT local signing | Keycloak/Auth0 handles |
| `SecurityConfig` | Local bearer-token authentication | OAuth2 resource server with provider metadata |
| Credential lifecycle | Local password and refresh sessions | Keycloak-managed |
| Platform users | Active | Retained for approval, status, and ownership |
| Platform roles | Local roles and authority mappings | Retained; provider entitlements map to platform roles |
| External identity link | Local issuer and subject | Keycloak issuer and subject |
| `ExternalPrincipal` | Normalized local claims | Normalized JWT claims or introspection response |
| `AuthenticatedActor` | Resolved platform identity | Resolved platform identity; unchanged contract |
| Controllers and handlers | **No change** | **No change** |

The key guarantee is that controllers and handlers consume the same
`AuthenticatedActor` and authority codes. Migration changes credential
management, token endpoints, the `AccessTokenAuthenticator` implementation,
provider metadata, and entitlement mapping, but not application
authorization or ownership contracts.

### Role Mapping Strategy for External IdP

When Keycloak is adopted:

Keycloak realm roles, client roles, or groups are provider inputs rather than
application authorities. The provider adapter reads configured JWT claim
locations or introspection fields and returns normalized entitlements in
`ExternalPrincipal`. `PlatformIdentityResolver` maps those values to local
role codes.
Local roles then resolve to stable authorities through `role_authorities`.

This keeps Keycloak responsible for authentication while the platform remains
responsible for commerce authorization policy.

---

## Consequences

### Positive

- Every state-changing request has a verified identity
- Dynamic roles and role-authority assignments require no endpoint code change
- Users can hold multiple roles simultaneously
- External IdP roles and groups can be mapped to platform roles
- Storefront endpoints remain publicly accessible
- The provider-neutral actor contract isolates application code from IdP
  migration
- Follows the same bounded context, CQRS, and multi-datasource patterns as
  existing modules
- Error responses remain consistent with the existing ProblemDetail format
- Spring Security provides battle-tested filter chain and authorization
  primitives

### Negative

- New module adds build and deployment complexity
- All existing controller tests must be updated for security context
- `X-Actor-Id` retirement is a breaking change for existing API clients
- Self-hosted JWT requires secret key management and token revocation strategy
- Password storage adds security responsibility (BCrypt mitigates this)
- Dynamic roles require role, authority, and provider-entitlement mapping
  management
- Protected requests may require a cached local lookup for platform status,
  identity linkage, and effective authorities
- Role string comparisons lose compile-time type safety compared to enums

---

## Alternatives Considered

### Alternative 1: Start with external IdP directly

**Deferred**: Adds infrastructure dependency (Keycloak server, OAuth2 config)
before the application has stable user flows. Starting self-hosted keeps the
development loop fast while the port/adapter pattern preserves the migration
path.

### Alternative 2: Static enum roles

**Rejected**: Enum roles require code changes and redeployment to add new
roles. This is incompatible with external IdPs which manage roles as dynamic
groups. An ecommerce platform typically grows to need roles beyond the initial
three (e.g., `WAREHOUSE_MANAGER`, `CATALOG_MODERATOR`, `FINANCE_ADMIN`).

### Alternative 3: API key authentication

**Rejected**: API keys do not support role-based access, user registration, or
standard browser flows needed for the storefront. JWT with claims is a better
fit for an ecommerce platform.

### Alternative 4: Session-based authentication

**Rejected**: The platform uses a stateless REST API. Session-based
authentication requires server-side session storage and does not scale well
for distributed deployments or mobile clients.

---

## References

- ADR-001: System Architecture as a Modulith
- ADR-003: Exception Handling Framework
- `docs/PRD/identity-module-prd.md` — Product Requirements Document
- `docs/BRD/identity-authentication-brd.md` — Business Requirements Document
