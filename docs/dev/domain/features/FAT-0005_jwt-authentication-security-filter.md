# Feature: Provider-Neutral Bearer Authentication and Local JWT

## 1. Objective

Authenticate every protected request through a provider-neutral bearer-token
contract while supplying a simple local JWT implementation for the current
platform. The application-facing principal must remain unchanged when a future
OAuth2/OIDC provider validates JWT access tokens through discovery/JWKS or
opaque access tokens through introspection.

---

## 2. Scope

### In scope

- Framework `AccessTokenAuthenticator`, `ExternalPrincipal`,
  `PlatformIdentityResolver`, and `AuthenticatedActor` contracts from FAT-0004
- Local asymmetric JWT access-token generation and validation
- Local `TokenIssuer` implementation and integration with hashed refresh
  sessions owned by FAT-0004
- Spring Security bearer-token authentication pipeline
- `SecurityPrincipal` wrapping `AuthenticatedActor`
- Public-route allowlist and authenticated-by-default policy
- RFC 7807 responses for authentication and access failures
- Configuration validation for issuer, audience, key IDs, keys, and expiry
- Extension points for OIDC JWT/JWKS and OAuth2 opaque-token introspection
- Security contract, unit, slice, and integration tests

### Out of scope

- Deploying or configuring Keycloak/Auth0/Cognito
- Implementing an OAuth2 authorization server or OIDC provider
- Accepting OIDC ID tokens as API credentials
- Dynamic role management and identity persistence (FAT-0004)
- Business authority annotations and ownership checks (FAT-0006)
- CORS policy, MFA, social login, and brute-force protection

### Delivery dependency

This feature depends on FAT-0004's contracts, platform identity resolver, and
refresh-session service. It must be complete before FAT-0006 enables protected
catalog and inventory endpoints.

---

## 3. Architectural Placement

```text
framework/
└── .../security/
    ├── AccessTokenAuthenticator.java
    ├── ExternalPrincipal.java
    ├── PlatformIdentityResolver.java
    └── AuthenticatedActor.java

store/src/main/java/com/grab/store/shared/security/
├── SecurityConfig.java
├── SecurityPrincipal.java
├── ProviderBearerAuthenticationProvider.java
├── LocalJwtAccessTokenAuthenticator.java
├── LocalTokenIssuer.java
├── LocalJwtProperties.java
├── ProblemDetailAuthEntryPoint.java
└── ProblemDetailAccessDeniedHandler.java
```

Provider-neutral contracts contain no Spring, JJWT, Nimbus, Keycloak, servlet,
or JPA types. Security infrastructure may depend on Spring Security and JJWT.
Controllers and CQRS handlers consume only `SecurityPrincipal` or
`AuthenticatedActor`.

---

## 4. Authentication Pipeline

```text
Authorization: Bearer <access-token>
  → Spring BearerTokenAuthenticationFilter
  → ProviderBearerAuthenticationProvider
  → AccessTokenAuthenticator.authenticate(token)
      current: LocalJwtAccessTokenAuthenticator
      future:  OidcJwtAccessTokenAuthenticator
               OpaqueTokenAccessTokenAuthenticator
  → ExternalPrincipal(issuer, subject, email?, entitlements)
  → PlatformIdentityResolver.resolve(principal)
  → AuthenticatedActor(platformUserId, roles, authorities)
  → SecurityPrincipal
  → SecurityContext
  → controller → service → CQRS handler
```

The authenticator proves the external identity. The resolver determines
whether that identity may use the commerce platform and calculates current
roles and authorities. Token claims alone never bypass account status or local
policy.

### Missing-token behavior

- Public route: continue anonymously.
- Protected route: Spring invokes `ProblemDetailAuthEntryPoint` and returns 401.
- A malformed or invalid supplied token fails immediately with 401; it is not
  treated as an anonymous request.

---

## 5. Local Access Token Contract

### JOSE header

| Field | Requirement |
|---|---|
| `alg` | `RS256` initially; never `none` or symmetric fallback |
| `kid` | Required and matched to configured active verification key |
| `typ` | `at+jwt` for locally issued API access tokens |

### Claims

| Claim | Requirement |
|---|---|
| `iss` | Configured local issuer URI |
| `sub` | Stable platform user UUID for the local issuer |
| `aud` | Contains configured commerce API audience |
| `iat` | Issued-at instant |
| `nbf` | Optional, with bounded clock skew |
| `exp` | Required access-token expiry |
| `jti` | Unique token identifier |
| `email` | Current email; profile data, not identity key |
| `roles` | Array of active role codes at issuance time |

The local authenticator validates algorithm allowlist, `kid`, signature,
issuer, audience, time claims, token type, and required claim types. It returns
an `ExternalPrincipal` whose entitlements are the local `roles` values.
`PlatformIdentityResolver` reloads current platform status and effective access.

### Key management

- Private signing key is supplied through a secret/file reference and is never
  committed or logged.
- Public verification keys are configured by `kid`.
- Configuration supports an active signing key plus previous public keys so
  deployments can rotate without invalidating all unexpired tokens.
- Startup fails when keys are missing, malformed, too weak, or incompatible
  with the configured algorithm.

---

## 6. Local Refresh Token Contract

Refresh tokens are opaque random values, not JWT access tokens.

1. `LocalTokenIssuer.issue()` signs an access token and generates a
   cryptographically random refresh token.
2. FAT-0004 persists only the refresh-token hash and token-family metadata.
3. `refresh()` locks the current session, verifies expiry/revocation, creates a
   replacement, revokes the old row, and returns the new pair atomically.
4. Reuse of a replaced token revokes the entire family.
5. `revoke()` is idempotent and never reveals whether a supplied token existed.

Access tokens are not accepted by the refresh endpoint, and refresh tokens are
not accepted by the bearer authentication pipeline.

---

## 7. Spring Security Components

### ProviderBearerAuthenticationProvider

```java
public final class ProviderBearerAuthenticationProvider
        implements AuthenticationProvider {

    private final AccessTokenAuthenticator authenticator;
    private final PlatformIdentityResolver identityResolver;

    @Override
    public Authentication authenticate(Authentication request) {
        String token = ((BearerTokenAuthenticationToken) request).getToken();
        ExternalPrincipal external = authenticator.authenticate(token);
        AuthenticatedActor actor = identityResolver.resolve(external);
        SecurityPrincipal principal = new SecurityPrincipal(actor);
        return UsernamePasswordAuthenticationToken.authenticated(
                principal, token, principal.getAuthorities());
    }
}
```

Credentials must be erased after authentication. Authentication exceptions
must carry safe error categories without leaking parsing or key details.

### SecurityPrincipal

- Exposes `platformUserId`, `issuer`, `subject`, email, roles, and authorities.
- Produces `SimpleGrantedAuthority` directly from stable authority codes.
- Does not synthesize `ROLE_` prefixes for business authorization.
- Stores immutable defensive copies.

### SecurityFilterChain

- Stateless session creation policy
- CSRF disabled for bearer-token API endpoints
- `DefaultBearerTokenResolver` or equivalent strict Bearer parsing
- Explicit public allowlist before the authenticated fallback
- All routes not explicitly public require authentication
- `ProblemDetailAuthEntryPoint` for 401
- `ProblemDetailAccessDeniedHandler` for 403
- Method security enabled for FAT-0006

The filter chain decides public versus authenticated access. Stable authority
requirements belong to FAT-0006 annotations or explicit request matchers and
must never use dynamic role names as hardcoded policy.

---

## 8. Public Endpoint Allowlist

| Method | Exact route or constrained pattern |
|---|---|
| POST | `/api/v1/identity/auth/register` |
| POST | `/api/v1/identity/auth/login` |
| POST | `/api/v1/identity/auth/refresh` |
| GET | `/api/v1` and bounded-context discovery roots intended as public |
| GET | Exact product detail-by-ID and detail-by-slug routes |
| POST | Exact product search route |
| GET | Category and explicitly approved storefront reference-data routes |
| GET | `/swagger-ui/**`, `/v3/api-docs/**` |

Do not allow broad `/products/**` or `GET /**` patterns. Route-order tests must
prove that administrative, mutation, inventory, profile, and future unknown
routes remain protected.

---

## 9. External Provider Extension Contract

Future implementations replace only `AccessTokenAuthenticator` and provider
configuration:

| Mode | Validation | Normalized output |
|---|---|---|
| OIDC/OAuth2 JWT | Discovery metadata, JWKS, issuer, audience, expiry | `ExternalPrincipal` |
| OAuth2 opaque token | RFC 7662-style introspection plus active/audience checks | `ExternalPrincipal` |

The adapter supplies a canonical configured issuer when an opaque response
does not contain one. A user-delegated token must yield a stable subject; a
token without a usable subject fails identity authentication. Provider roles,
groups, scopes, or other access values are normalized to `entitlements`.

OIDC ID tokens must be rejected. For external JWTs, validate the API audience
and provider-specific access-token type where available. For opaque tokens,
require `active=true` and validate audience/resource indicators.

No provider implementation may return `AuthenticatedActor` directly or query
catalog/inventory. All provider identities pass through
`PlatformIdentityResolver`.

---

## 10. Configuration

```yaml
security:
  bearer:
    provider: local-jwt
  local-jwt:
    issuer: https://identity.local.grab
    audience: grab-commerce-api
    algorithm: RS256
    active-key-id: local-2026-01
    private-key-location: ${JWT_PRIVATE_KEY_LOCATION}
    public-keys:
      local-2026-01: ${JWT_PUBLIC_KEY_LOCATION}
    access-token-ttl: PT15M
    refresh-token-ttl: P7D
    clock-skew: PT30S
```

Use validated `@ConfigurationProperties`. Production configuration must not
provide insecure default keys. Logs may include issuer, `kid`, and failure
category but never tokens, private keys, passwords, or introspection secrets.

Future provider configuration may add `mode: oidc-jwt|opaque`, `issuer-uri`,
`audience`, `jwk-set-uri`, or introspection endpoint/client credentials without
changing application contracts.

---

## 11. Error Contract

| Condition | Status | Code |
|---|---:|---|
| Missing token on protected route | 401 | `idt.service.auth.missing_token` |
| Malformed Bearer header | 401 | `idt.service.auth.malformed_token` |
| Invalid signature/key/issuer/audience/type | 401 | `idt.service.auth.invalid_token` |
| Expired or not-yet-valid token | 401 | `idt.service.auth.expired_token` |
| Inactive provider token | 401 | `idt.service.auth.inactive_token` |
| Unknown external identity | 401 | `idt.service.auth.identity_not_linked` |
| Suspended or pending platform user | 401 | `idt.service.auth.account_not_active` |
| Missing stable authority | 403 | `idt.service.auth.access_denied` |

Responses follow the existing RFC 7807 extensions: `code`, `args`, `traceId`,
`module`, `retryable`, and `retryAfterMs`. Authentication messages do not reveal
whether an email, subject, key, or session exists.

---

## 12. Detailed Implementation Plan

1. **Add dependencies and contracts**
   - Add Spring Security resource-server/JOSE support and JJWT only to the
     security implementation module.
   - Place provider-neutral ports and records in framework.
   - Add compile-time architecture tests preventing Spring/provider imports in
     framework contracts.

2. **Implement validated configuration and keys**
   - Create `LocalJwtProperties` as a validated configuration record.
   - Load PEM keys by location, construct the signing key registry, and fail
     startup on invalid configuration.
   - Add key-rotation tests for active and previous `kid` values.

3. **Implement local access-token issuance**
   - Build RS256 access tokens with the documented header and claims.
   - Generate secure opaque refresh values and delegate persistence/rotation
     to the FAT-0004 refresh-session service.
   - Use an injectable `Clock` and secure random source for deterministic tests.

4. **Implement LocalJwtAccessTokenAuthenticator**
   - Enforce algorithm allowlist and key selection before claims mapping.
   - Validate issuer, audience, type, expiry, not-before, and claim shapes.
   - Return immutable `ExternalPrincipal`; never return domain users.

5. **Integrate Spring Security**
   - Implement `ProviderBearerAuthenticationProvider` and `SecurityPrincipal`.
   - Configure `BearerTokenAuthenticationFilter`, stateless sessions, exception
     handlers, method security, and the exact public allowlist.
   - Ensure security classes remain under `store/shared/security`, not a
     bounded-context `internal` package needed by other modules.

6. **Implement ProblemDetail writers**
   - Reuse shared trace/module/error metadata behavior.
   - Add response-commit and content-type tests for filter-chain exceptions.

7. **Prepare external adapter extension tests**
   - Define reusable `AccessTokenAuthenticatorContractTest` cases.
   - Provide test doubles proving JWT and opaque implementations produce the
     same `ExternalPrincipal` and flow through the same resolver.
   - Do not add live provider network calls in this feature.

8. **Run integration and regression tests**
   - Verify public, protected, malformed-token, expired-token, suspended-user,
     and insufficient-authority paths.
   - Verify unknown routes are protected and no `X-Actor-Id` affects identity.

---

## 13. Test Plan

Test method names follow `{functionName}_{input}_{expectedBehavior}`.

- `issue_withActiveUser_shouldCreateRs256AccessAndOpaqueRefreshTokens`
- `authenticate_withValidLocalJwt_shouldReturnExternalPrincipal`
- `authenticate_withWrongAudience_shouldRejectToken`
- `authenticate_withIdTokenType_shouldRejectToken`
- `authenticate_withUnknownKid_shouldRejectToken`
- `authenticate_withPreviousRotationKey_shouldValidateUnexpiredToken`
- `refresh_withReusedToken_shouldRevokeTokenFamily`
- `resolve_withSuspendedUser_shouldRejectAuthentication`
- `filter_withPublicRouteAndNoToken_shouldAllowRequest`
- `filter_withProtectedRouteAndNoToken_shouldReturnProblemDetail401`
- `filter_withInvalidTokenOnPublicRoute_shouldReturnProblemDetail401`
- `filter_withMissingAuthority_shouldReturnProblemDetail403`
- `filter_withUnknownRoute_shouldRequireAuthentication`
- Contract tests for normalized local JWT, OIDC JWT, and opaque-token outputs

---

## 14. Acceptance Criteria

- [ ] Framework security contracts contain no Spring or provider SDK types.
- [ ] Local tokens use asymmetric signing, required `kid`, issuer, audience,
      expiry, and `at+jwt` type.
- [ ] Local validation rejects wrong algorithm, signature, key, issuer,
      audience, type, timing, or claim shape.
- [ ] OIDC ID tokens are never accepted as commerce API access tokens.
- [ ] `AccessTokenAuthenticator` can be replaced without controller, service,
      handler, or domain changes.
- [ ] JWT and opaque provider adapters can normalize to the same
      `ExternalPrincipal` contract.
- [ ] Every authenticated request resolves current platform status, roles, and
      authorities through `PlatformIdentityResolver`.
- [ ] Public routes are explicitly allowlisted; every other route is protected.
- [ ] Invalid supplied tokens return 401 even when the target route is public.
- [ ] Missing authority returns RFC 7807 403; authentication failures return
      RFC 7807 401 with no secret details.
- [ ] Access and refresh tokens cannot be substituted for one another.
- [ ] Key rotation validates tokens signed by retained previous keys.
- [ ] Security, contract, integration, and regression tests pass.
