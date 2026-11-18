# Seller BFF

Standalone Spring Boot web boundary for Seller APIs. Browser authentication uses one configurable OIDC provider and a server-side Redis session. Account remains authoritative for platform identity, seller assignments, roles, and lifecycle revisions. The same packaged BFF can use different compatible issuers through deployment configuration.

## Docker setup

Copy `docker/env/seller-bff.env.example` to `docker/env/seller-bff.env`, configure the issuer/client, callback URLs, Redis, and Account mTLS secret source, then start the authorization profile with Docker Compose. In Docker, the public issuer is used for browser redirects and ID-token validation; `SELLER_OIDC_METADATA_URI` and individual endpoint overrides can use private service DNS names. The callback and post-logout URLs must exactly match the provider client registration.

The BFF image runs as a non-root Java 21 runtime user and exposes health probes at `/actuator/health/liveness` and `/actuator/health/readiness`. Production deployments should expose management routes only on the internal network.

## Provider configuration

The stable OAuth registration ID is `seller`. Provider metadata supplies authorization, token, JWKS, introspection, revocation, and end-session endpoints when present. Deployment overrides use the standard Spring property environment names `GRAB_SELLER_OIDC_METADATA_URI`, `GRAB_SELLER_OIDC_AUTHORIZATION_ENDPOINT`, `GRAB_SELLER_OIDC_TOKEN_ENDPOINT`, `GRAB_SELLER_OIDC_JWK_SET_ENDPOINT`, `GRAB_SELLER_OIDC_INTROSPECTION_ENDPOINT`, `GRAB_SELLER_OIDC_REVOCATION_ENDPOINT`, and `GRAB_SELLER_OIDC_LOGOUT_ENDPOINT`. The public issuer remains `SELLER_OIDC_ISSUER`, including when metadata is fetched through a private backchannel.

The provider must support Authorization Code with S256 PKCE, ID-token verification, confidential-client refresh tokens, active-token introspection, refresh-token revocation, and RP-initiated logout. Introspection must return an active token with a matching subject, the configured audience, and a future expiry. Resource-server introspection credentials can be set separately with `SELLER_OIDC_RESOURCE_CLIENT_ID` and `SELLER_OIDC_RESOURCE_CLIENT_SECRET`.

Non-native provider subjects must have an exact issuer-subject link provisioned in Account before first seller sign-in. Email and matching subject strings never link accounts. The built-in authorization server may use platform-ID lookup only when Account explicitly configures its issuer in `SELLER_AUTH_TRUSTED_NATIVE_ISSUER`.

## Sessions, refresh, and context

Tokens remain in Redis; browsers receive only the session cookie. Use the same Redis namespace, cookie name, session timeout, and provider configuration on all replicas. Set a fresh `SELLER_OIDC_REDIS_NAMESPACE` when changing providers or session generations. Keep the prior BFF deployment and its original issuer credentials available until its revocation queue drains.

Protected requests introspect the current access token and query Account for live seller state. Context selection uses the existing assignment route and atomically commits an Account-approved context under the Redis lock. Access and refresh token lifetimes remain owned by OAuth. Refresh reuses an existing refresh token when the provider does not return a replacement. A timeout with an unknown rotation result retires the local slot and queues revocation to prevent replay.

## Configuration

`application.yml` binds deployment settings from environment variables. The optional `/run/secrets/seller-bff/` Spring config tree can provide secrets as files named for property keys, such as `grab.seller-oidc.client-secret`, `grab.seller-oidc.resource-client-secret`, `grab.seller-oidc.account-key-password`, `grab.seller-oidc.account-trust-password`, and `spring.data.redis.password`. Do not define empty environment variables for these secret values, because they would override the secret source. The Account client key/trust stores are mounted under `/run/secrets/seller-auth/`. The BFF bounds issuer and Account connection/read calls against the Redis lock TTL.

The `prod` profile fails startup unless the issuer and callbacks use HTTPS, cookies are secure, CORS has explicit origins, Redis uses TLS and authentication, OAuth secrets are supplied, and Account mTLS material is readable. Use authenticated TLS Redis and a persistent volume/service. Store OAuth secrets, Redis credentials, and mTLS passwords in deployment secret storage, never in source control or logs.

## Browser flow

The browser fetches `GET /api/v1/identity/auth/csrf`, then posts registration JSON to `POST /api/v1/identity/auth/register` with the CSRF header. On success, follow the `login` relation through top-level navigation to `/oauth2/authorization/seller`. After callback, fetch a fresh CSRF token and `GET /api/v1/identity/session`. Unsafe API calls include the fresh CSRF header. Logout is an authenticated top-level POST to `/api/v1/identity/auth/logout`; local access ends immediately and refresh-token revocation is queued.

The BFF does not accept bearer-only browser authentication and has no JSON login or refresh routes. Existing registration, profile, context, logout, HAL, and route-authority contracts remain in place.
