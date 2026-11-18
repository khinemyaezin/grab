# Seller BFF

Standalone Spring Boot web boundary for Seller APIs. Browser authentication uses the Authorization Server and an OIDC session cookie. Account remains the source of seller credentials and identity. The BFF keeps OAuth tokens server-side in Redis.

## Local OIDC setup

Follow the [Authorization Server deployment guide](../authorization-server/README.md) to start Account, the issuer, Redis, and Seller BFF. Set the BFF's public issuer URL, callback, post-logout URL, client ID, client secret, and audience to match the deployed issuer and YAML registration. Set `SELLER_OIDC_UI` to the frontend URL to open after sign-in.

## Session and issuer settings

| Environment variable | Default |
|---|---|
| `SELLER_OIDC_COOKIE_NAME` | `GRAB_SELLER_SESSION` |
| `SELLER_OIDC_REDIS_NAMESPACE` | `grab:seller` |
| `SELLER_OIDC_SESSION_TIMEOUT` | `7d` |
| `SELLER_OIDC_CONNECT_TIMEOUT` | `2s` |
| `SELLER_OIDC_READ_TIMEOUT` | `6s` |

Use the same namespace, cookie name, and session timeout on every BFF replica. Changing the namespace selects a different set of Redis sessions, token slots, and queued revocations. Issuer connect and read timeouts must total at most 12 seconds. Set `SELLER_OIDC_BACKCHANNEL` when the BFF reaches the issuer at an internal URL. Leave the variable unset to use the public issuer URL for backchannel calls. The public issuer URL remains the browser redirect destination and token issuer identity.

The seller client registration's `token-uri` and `jwk-set-uri` are shared by login and the live seller session. To override each independently, set `SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_SELLER_TOKENURI` or `SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_SELLER_JWKSETURI` to an absolute HTTP(S) URL.

## CORS and revocation settings

| Environment variable | Default | Purpose |
|---|---|---|
| `SECURITY_API_CORS` | Explicit origins required | Comma-separated origins allowed to make credentialed requests |
| `SECURITY_API_CORS_ALLOWED_METHODS` | `GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS` | Comma-separated CORS methods |
| `SECURITY_API_CORS_ALLOWED_HEADERS` | `Content-Type,Authorization,X-CSRF-TOKEN` | Comma-separated CORS request headers |
| `SELLER_OIDC_REVOCATION_RETRY_DELAY` | `30s` | Delay between retry passes |
| `SELLER_OIDC_REVOCATION_TOKEN_TTL` | `7d` | Retention of queued refresh tokens, as a whole-second duration such as `7d` |
| `SELLER_OIDC_REVOCATION_CLAIM_LEASE` | `60s` | Time before another worker can retry a claim, as a whole-second duration greater than `30s` |
| `SELLER_OIDC_REVOCATION_BATCH_SIZE` | `25` | Maximum entries considered per queue in one retry pass |

## Browser flow

The browser fetches `GET /api/v1/identity/auth/csrf`, then posts registration JSON to `POST /api/v1/identity/auth/register` with the returned CSRF header. On success, follow the `login` relation using top-level browser navigation to `/oauth2/authorization/seller`. After the issuer callback redirects to the UI, fetch a fresh CSRF token and `GET /api/v1/identity/session`. Unsafe API calls must include the fresh CSRF header. Logout uses an authenticated top-level POST to `/api/v1/identity/auth/logout` with the CSRF form parameter. It ends the BFF session immediately, redirects the browser to the issuer, and queues refresh-token revocation in Redis.

The Seller BFF does not accept Account bearer tokens or expose JSON login and refresh routes. Context selection and live authorization continue through the OIDC session flow.

## Production

Use HTTPS for the issuer and BFF, secure HttpOnly cookies, persistent Redis with authentication and TLS, and exact registered callback and post-logout URLs. Keep the issuer client secret and signing keys in secret storage. Rotate signing keys by publishing the new current alias and retaining prior public aliases through the maximum token lifetime. Restrict Actuator endpoints to the internal network.
