# Seller BFF OIDC and Account authorization specification

This specification implements [the Seller BFF OAuth portability plan](../../bff_oauth.md). The same deployable BFF supports any provider that satisfies the documented OIDC and OAuth profile. Account remains authoritative for platform identity, external identity links, seller assignments, permissions, and lifecycle revisions. The issuer owns authorization codes and token lifecycles. The BFF owns browser sessions and never exposes OAuth tokens to the browser.

## Sign-in and browser contract

1. The browser starts at `/oauth2/authorization/seller`. Spring Security uses authorization code with S256 PKCE, state, and nonce. ID tokens are signature- and issuer-validated against trusted provider metadata and the configured public issuer.
2. The issuer verifies credentials with Account over the issuer-only mTLS binding. The BFF uses a separate BFF-authorized mTLS binding for registration and the additive seller access read. Private keys and password hashes remain with their owning service.
3. At callback, the BFF validates the login token response, introspects the access token, and resolves seller access through Account. Account uses platform-ID lookup only for its explicitly configured trusted native issuer; all other providers require an exact issuer-subject link.
4. Registration keeps the existing CSRF protected route and login relation. After sign-in, the browser receives only the HttpOnly session cookie, fetches a fresh CSRF token, and includes it on unsafe requests. Existing profile, context, selection, logout, and HAL routes remain stable.

## Session authorization and token lifetime

The stable Spring client registration ID is `seller`. Provider metadata supplies authorization, token, JWKS, introspection, revocation, and end-session endpoints. Explicit endpoint overrides may use private backchannel addresses while the public issuer remains the expected ID-token issuer. Metadata must report the same issuer as configured.

The BFF stores access and refresh tokens, issuer, subject, platform user ID, selected assignment, and authorization revisions in Redis. Every protected request introspects the active access token and queries Account for current seller authorization. Introspection must return an active token with the login subject, configured audience, and future expiry; a missing issuer is bound to the configured trusted provider, and a conflicting issuer is rejected. Bearer-only authentication is not enabled on the browser chain.

At login, exactly one eligible assignment is auto-selected; zero or multiple assignments produce an unscoped session. Context selection asks Account to authorize the target and atomically commits the new context and revision baseline under the Redis lock. A denied selection preserves the previous context. User authentication version and merchant/storefront revisions are compared on every request, so suspension, revocation, or lifecycle changes invalidate old sessions after reactivation as well.

Context switching does not issue new OAuth tokens. Provider refresh owns OAuth token rotation. If a successful refresh omits a replacement refresh token, the BFF retains the current token. A terminal grant rejection invalidates the session. If a timeout leaves rotation outcome unknown, the BFF retires the slot and queues revocation rather than replaying an uncertain token. Redis ownership checks fence refresh, selection, and logout writes.

## Failure behavior

| Condition | Result |
| --- | --- |
| Missing/invalid session, inactive token, mismatched identity, or stale authorization snapshot | `401`; invalidate the affected session where applicable |
| Authenticated seller lacks permission or Account rejects a proposed assignment | `403`; preserve the previous valid selection |
| Route requires context while the seller is unscoped | `409`; allow context listing and selection |
| Issuer, Account, or Redis dependency is unavailable | `503`; deny access and record a bounded-cardinality outcome metric |

Local logout takes effect immediately. Refresh-token revocation is queued in Redis and retried with bounded leases. Provider changes use a fresh Redis namespace/session generation; the prior deployment keeps its provider credentials and drains its own queue before retirement. Rollback uses another fresh generation so old sessions cannot resume.

## Production configuration

All deployment-specific values are bound through `application.yml` and environment variables. OAuth, Redis, and mTLS passwords come from deployment secret sources or the mounted Spring config tree. Startup validation requires a trusted issuer, client, audience, scopes, callback, UI and post-logout URLs, HTTPS in production, Secure/HttpOnly cookies, explicit credentialed CORS origins, authenticated TLS Redis, and readable Account mTLS material. Connection/read timeouts must fit inside the Redis lock renewal budget. Validation errors and metrics never expose credentials, tokens, session IDs, or subjects.

The provider must support OIDC authorization code, S256 PKCE, ID-token verification, refresh tokens, opaque-token introspection, refresh-token revocation, and RP-initiated logout. Introspection requires active status, the expected audience, a future expiry, and the login subject. Introspection credentials may be distinct from the OAuth client credentials.

## Acceptance criteria

- **AC1:** OIDC authorization code, S256 PKCE, state, nonce, CSRF, and existing browser/HAL routes work; tokens remain server-side.
- **AC2:** Every protected request requires the browser session and successful live access-token introspection; bearer-only access fails.
- **AC3:** Account resolves issuer-subject identity and current seller authorization with either compatible provider.
- **AC4:** Context selection is atomic; denial preserves the previous selection; stale lifecycle revisions prevent access.
- **AC5:** Deployment settings are externally bound and unsafe production configuration fails startup without exposing secrets.
- **AC6:** The same BFF artifact passes the provider suite for both issuers, with provider-specific revocation queues preserved through rollout and rollback.

## Deployment and verification

Deploy Account's additive seller read first and verify that the BFF mTLS identity can call it while issuer-only credential verification remains separate. Provision exact redirect URIs and external identity links before sign-in. Run the same packaged artifact against the built-in issuer and a second provider fixture with different endpoint paths, opaque access tokens, and an externally linked subject. Cover registration, login, profile, context listing/selection, protected merchant operation, refresh, and logout. Exercise inactive, expired, wrong-audience, and wrong-subject tokens; unknown links; lifecycle changes; Redis races between two BFF instances; refresh without replacement tokens; provider and Account outages; and revocation retries. Record artifact digest and provider-specific outcomes; see [the implementation plan](../../bff_oauth.md) for the required Maven reactor and release evidence.

The broader route ownership audit includes stock allocation and deallocation. Those operations accept a SKU and order reference and still need merchant ownership enforced by the inventory workflow before production cutover.
