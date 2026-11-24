# Seller BFF OAuth and identity implementation plan

**Status:** Proposed for implementation. **Primary module:** `service/seller-bff`. **Account change:** one read-only operation on the existing BFF-authorized mTLS binding.

The release must demonstrate that **the same packaged seller BFF works with two compatible authorization servers**. Changing providers requires deployment configuration, external identity links in Account, and fresh browser sign-in. The browser continues to use a session cookie. Spring Security authenticates the server-held tokens, and Account decides which merchant operations a seller may perform.

## 1. Review of the current implementation

| Area | Current behavior | Consequence |
| --- | --- | --- |
| Browser authentication | Both OAuth starters are present. The chain configures `oauth2Login()`, then a custom browser filter obtains a live seller principal. [Security chain](../service/seller-bff/src/main/java/com/grab/service/sellerbff/internal/adapter/inbound/rest/security/oidc/SellerOidcSecurityConfig.java) | Configure Resource Server token validation explicitly while preserving session-only HTTP access and CSRF. |
| Issuer calls | Java submits refresh and private seller-context grants, calls fixed introspection and revocation paths, reads custom claims, and treats `sub` as the platform user ID. [Issuer client](../service/seller-bff/src/main/java/com/grab/service/sellerbff/internal/adapter/inbound/rest/security/oidc/SellerIssuerClient.java) | Changing the issuer URL alone cannot support a provider with opaque tokens or independently assigned subjects. |
| Client configuration | YAML constructs endpoint paths, defaults to the development profile, and supplies local infrastructure defaults. Browser logout appends a fixed issuer path. [Configuration](../service/seller-bff/src/main/resources/application.yml) · [Browser session](../service/seller-bff/src/main/java/com/grab/service/sellerbff/internal/adapter/inbound/rest/security/oidc/SellerOidcBrowserSession.java) | Make trust settings explicit and support providers with different public and backchannel addresses. |
| Seller authorization | Account already resolves exact external identities and computes eligible assignments, permissions, and user, merchant, and storefront revisions. The mTLS interface carrying seller-state reads also supports password verification and is restricted to the issuer. [Identity lookup](../service/account/account-message-contract/src/main/java/com/grab/service/contract/message/account/identity/IdentityLookupPort.java) · [Seller state resolution](../identity-application/src/main/java/com/identity/application/service/ResolveSellerAuthenticationService.java) · [Transport bindings](../service/account/account-service/src/main/java/com/grab/service/account/identity/internal/config/SellerAuthenticationTransportConfig.java) | Reuse the queries through one narrowly scoped BFF read. Preserve lifecycle checks when moving context out of the issuer. |
| Browser contract | Current acceptance criteria require context selection to supersede the previous token pair. [Feature specification](spec/features/feat_seller-oidc.md) · [Technical specification](spec/system/seller-oidc-tech-spec.md) | Change that criterion explicitly. A successful switch replaces the authorized session context atomically. OAuth refresh and revocation retain their own lifecycles. |

## 2. Acceptance criteria

| ID | Observable behavior |
| --- | --- |
| AC1 | Browser login uses OIDC authorization code with S256 PKCE, state, and nonce. Tokens stay server-side. Registration, CSRF, profile, context, logout, and HAL links work through the existing routes. |
| AC2 | Each protected seller request requires a browser session and a successful live introspection of its access token. A bearer header alone cannot authenticate the request. |
| AC3 | Account maps the authenticated issuer and subject to a platform user and returns current seller access. Both providers support the complete seller flow using standard OAuth/OIDC capabilities. |
| AC4 | Context selection commits atomically. Denied selection preserves the previous context. A revoked session, suspended identity, or changed lifecycle revision prevents further access through the old session. |
| AC5 | All environment-specific settings come from deployment configuration. Unsafe production settings fail startup with an actionable, secret-free error. |
| AC6 | One BFF artifact passes the same end-to-end suite for both issuers. Rollout and rollback preserve each issuer’s outstanding revocations. |

## 3. Implementation steps

### Step 1 — Record the baseline and provider contract

1. Run the existing browser and Account transport suites with Docker available. Record outcomes for PKCE login, CSRF, registration, profile, selection, logout, authorization by route, and direct-bearer rejection. Distinguish existing failures from regressions. [Browser tests](../service/seller-bff/src/test/java/com/grab/service/sellerbff/internal/adapter/inbound/rest/security/oidc/SellerOidcBrowserIntegrationTest.java) · [Account transport tests](../service/account/account-service/src/test/java/com/grab/service/account/SellerAuthenticationIntegrationTest.java)
2. During implementation, update the existing seller OIDC feature and technical specifications with AC1–AC6 and the revised context-switch behavior. Keep the existing public URLs, HAL relations, cookie semantics, and protected-route rules as regression fixtures.
3. Document the required provider profile: OIDC login and ID-token verification, confidential-client code exchange with S256 PKCE, refresh tokens, live token introspection, refresh-token revocation, and RP-initiated logout. Require introspection to identify an active access token for the expected API audience with a valid expiry and the same subject as the login. Some RFC 7662 response fields are optional in the protocol; these fields are requirements of this deployment.

### Step 2 — Expose one read-only seller-state operation from Account

1. Add a remote interface to `account-message-contract` with one operation, immutable serializable request/result records, and `RemoteException`. The request carries the validated issuer, external subject, and selection intent. Support initial selection, revalidation of the current assignment (including an unscoped session), and validation of a requested assignment ID.
2. Resolve external identities by the exact issuer–subject pair. Use the existing platform-ID lookup only for an explicitly configured trusted native issuer. Perform this decision inside Account. Require an existing external identity link for other issuers and prohibit automatic linking by email or by a matching subject string. A new external user needs a provisioned link before first seller sign-in.
3. Map the resolved identity to the existing Account seller-state query. Return the platform user ID, email, effective roles and authorities, resolved context, authentication version, and merchant/storefront revisions. Reuse Account’s single-eligible-context and lifecycle rules. Treat an unknown or inactive identity, an invalid current context, and a denied proposed assignment as distinct results. Propagate dependency failures separately.
4. Use the Account query bus and module read transactions. Keep Account and Merchant reads within their existing boundaries. Export the new read alongside registration on the BFF mTLS binding. Verify the BFF certificate can read seller state while credential verification remains restricted to the issuer certificate.
5. In the BFF, add a read adapter for that published contract. Reuse the protected transport configuration used by registration and bind connect/read timeouts through validated properties. Keep RMI, Account message types, and transport error translation out of controllers. [Existing registration adapter](../service/seller-bff/src/main/java/com/grab/service/sellerbff/internal/adapter/outbound/message/SellerAuthMessageAdapter.java)

### Step 3 — Configure Spring OAuth2 Client and Resource Server

1. Configure the stable `seller` login registration under `spring.security.oauth2.client`. Use the provider’s trusted issuer metadata for endpoints, with explicit per-endpoint overrides for deployments using private backchannels. Preserve the public issuer as the expected ID-token issuer. Retain Spring’s signature, issuer, audience, nonce, and authorization-response checks.
2. Configure introspection with `spring.security.oauth2.resourceserver.opaquetoken`. Feed the access token from the server-side Redis session to `OpaqueTokenAuthenticationProvider` using Spring’s `OpaqueTokenIntrospector`. The browser continues to authenticate through its session and CSRF protection. Keep the default HTTP bearer-token resolver off this chain. [Spring Resource Server introspection](https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/opaque-token.html)
3. Introspect each protected request, including after refresh. Require an active access token, nonblank matching subject, expected audience, and valid expiry. Bind a missing introspection issuer to the configured issuer-specific trusted endpoint and reject any conflicting returned issuer. Introspect only the access-token slot populated by the authenticated code or refresh exchange. Use Account’s result to build the request-local `SellerSecurityPrincipal`.
4. Use Spring’s refresh-token response client under the existing Redis lock. Persist a replacement refresh token when supplied and retain the existing one when the successful response omits a replacement. Validate the refreshed access token before granting access. Handle terminal grant rejection separately from temporary dependency failures. Avoid blindly retrying a refresh whose remote rotation outcome is unknown. [Spring client configuration](https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/login/core.html) · [Refresh-token support](https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/client/authorization-grants.html)
5. Resolve revocation and RP-initiated logout endpoints from trusted metadata or configured overrides. Keep bounded revocation retries and local logout during issuer outages. Retire the private context grant, duplicated grant handling, fixed endpoint suffixes, and seller-specific introspection DTO after replacement tests pass.

### Step 4 — Bind seller context to the Redis session

| Operation | Required behavior |
| --- | --- |
| Login | After validating the login and access token, obtain initial seller state from Account. Auto-select exactly one eligible assignment. Start unscoped for zero or multiple eligible assignments. Store issuer, subject, platform user ID, assignment, revisions, and token ownership in the server-side session state. |
| Protected request | Read the active slot, refresh if necessary, introspect, and obtain live seller state. Compare identity, assignment, authentication version, and lifecycle revisions to the session baseline. Install current authorities for this request only. Recheck the active slot/version before admitting the request if session state changed during validation. |
| Context selection | Accept only the assignment ID in the existing selection route. Revalidate the current session, have Account validate the target, and atomically commit the new context and revision baseline under the session lock. Return the existing HAL context representation. |
| Refresh and concurrent writes | Preserve token-slot lock ownership, lease renewal, and fenced writes. Prevent stale refreshes or selections from overwriting a newer context or recreating a logged-out session. If a rotated token cannot be committed, retain its issuer association for revocation. |
| Logout | End local session access immediately and enqueue revocation for the owning issuer. Redirect to the discovered or configured end-session endpoint using the registered post-logout URI. The browser may finish local logout during an issuer outage. |

Keep the controller focused on request mapping and HAL assembly. Move direct `SellerOidcBrowserSession` lookup out of the selection controller into the BFF session boundary. Keep Redis session coordination in the security layer and use the published Account query for domain authorization. [Session controller](../service/seller-bff/src/main/java/com/grab/service/sellerbff/internal/adapter/inbound/rest/controller/SellerSessionController.java) · [Redis token slots](../service/seller-bff/src/main/java/com/grab/service/sellerbff/internal/adapter/inbound/rest/security/oidc/SellerOidcTokenSlots.java) · [Revocation queue](../service/seller-bff/src/main/java/com/grab/service/sellerbff/internal/adapter/inbound/rest/security/oidc/SellerPendingRevocations.java)

| Condition | HTTP outcome |
| --- | --- |
| Missing or invalid session, inactive token, mismatched identity, or stale authorization baseline | `401`; invalidate the affected session when applicable. |
| Valid session with insufficient permissions or an ineligible proposed assignment | `403`; preserve the prior valid selection. |
| Endpoint requires a context and the authenticated seller has none | `409`; allow the seller to list and select an eligible context. |
| Introspection, Account, or Redis is unavailable | `503`; deny the request and distinguish the outage from credential rejection in metrics. |

### Step 5 — Validate production configuration

| Configuration area | Implementation and startup checks |
| --- | --- |
| Issuer and client | Require trusted issuer, client ID, secret reference, audience, scopes, registered callback, UI and post-logout URLs. Give explicit overrides precedence for their individual endpoints. Validate HTTPS production URLs and reject embedded credentials and fragments. |
| Introspection and transport | Bind the introspection URI and credentials, allowing separate resource-server credentials. Bound discovery, code exchange, JWKS, refresh, introspection, revocation, and Account RPC calls. Validate the total time spent under each Redis lease against its renewal budget. |
| Cookies, CORS, and Redis | Require Secure, HttpOnly cookies, explicit allowed origins for credentialed CORS, and authenticated TLS Redis in production. Make Redis namespace and session retention explicit across replicas. Keep local HTTP, insecure cookies, and localhost defaults in an explicitly selected development profile. |
| Secrets and management | Read OAuth, Redis, and mTLS secrets from deployment secret sources. Keep private management endpoints restricted. Emit operation duration, dependency outcome, refresh failure, and revocation backlog without raw tokens, secrets, session IDs, or subjects in logs or metric labels. |
| Deployment examples | Update the BFF README, environment example, and Compose settings. Configure the existing issuer’s seller client with an empty capability list and document identity links, exact redirects, and the required introspection fields for a second issuer. [BFF guide](../service/seller-bff/README.md) · [Environment example](../docker/env/seller-bff.env.example) · [Client registration example](../docker/config/authorization-clients.yml.example) |

## 4. Verification and release evidence

| Coverage | Scenarios to run | Criteria |
| --- | --- | --- |
| Provider portability | Build once and record the BFF artifact digest. Test the existing issuer with the seller-context extension disabled. Repeat with a second issuer fixture using different endpoint paths, opaque access tokens, an externally linked subject, and standard introspection fields. Complete registration or provision a test identity, then login, profile, list/select, authorized merchant operation, refresh, and logout. | AC1–AC3, AC6 |
| Browser boundary | Verify PKCE, state, nonce, exact callback, secure cookies, CSRF, public routes, route-to-authority coverage, HAL responses, and bearer-only rejection. Check that access and refresh tokens stay out of browser responses. | AC1, AC2 |
| Identity and token checks | Exercise revoked, expired, wrong-audience, and wrong-subject tokens. Cover unknown links, equal subjects at different issuers, denied merchant access, and user suspension. Verify Account mTLS binding separation. | AC2, AC3 |
| Context and revocation | Exercise zero, one, and multiple eligible assignments, invalid selection, role removal, assignment revocation, and merchant suspension followed by reactivation between requests. A session with an old revision must fail even after reactivation. | AC3, AC4 |
| Concurrency and outages | Run against real Redis with two BFF instances. Race refresh, select, and logout. Simulate timeouts independently for the issuer, Account, and Redis. Cover persisted rotation failure, logout during outage, revocation retry, and successful refresh without a replacement refresh token. | AC2, AC4, AC6 |
| Configuration and architecture | Test missing production secrets, insecure URLs, invalid timeout budgets, wildcard CORS, and split public/private networking. Run applicable ArchUnit checks and PostgreSQL Testcontainers tests for affected persistence behavior. | AC3, AC5 |

Run the affected Maven reactor from the repository root with Java 21 and Docker available:

```sh
./mvnw -pl service/account/account-service,service/seller-bff -am verify
```

Record the reactor result, integration test reports, artifact digest, and the two provider-specific end-to-end results. A green reactor alone does not demonstrate a configuration-only provider switch.

## 5. Rollout and rollback

1. **Deploy Account first.** Publish the additive read, verify the BFF workload certificate can invoke it, and smoke-test existing registration and issuer login paths.
2. **Prepare both providers.** Register exact callback and post-logout URLs, audience, introspection and revocation credentials. Provision identity links. Verify a linked seller and rejection of an unlinked seller in staging.
3. **Separate session generations.** Use a fresh Redis session namespace and require fresh sign-in. Keep the prior deployment’s revocation worker and credentials available against its original issuer until its queue drains. During rollback, use a fresh generation again so superseded sessions cannot resume.
4. **Canary the packaged BFF.** Route a limited traffic slice to the new deployment. Complete login, context selection, a protected merchant operation, refresh, and logout. Compare authentication and authorization outcomes, dependency latency, and queue age/backlog against the baseline and existing service objectives.
5. **Promote or roll back.** Promote once AC1–AC6 pass, including the two-provider run using the same artifact digest. On regression, restore the prior application and matching provider configuration, require sign-in, and continue draining revocations for each issuer with its original credentials. Retire credentials only after the corresponding queue drains.
