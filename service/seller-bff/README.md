# Seller BFF

Standalone Spring Boot web boundary for Seller APIs. Browser authentication uses the Seller Authorization Server and an OIDC session cookie. Account remains the source of seller credentials and identity. The BFF keeps OAuth tokens server-side in Redis.

## Local OIDC setup

1. Configure the Account database and Seller mTLS settings in `docker/env/account-service.env`. Set `SELLER_AUTH_RPC_ENABLED=true` and mount the Account server keystore plus separate issuer and BFF client allowlists.
2. Configure `docker/env/authorization-server.env` with its PostgreSQL URL and credentials, `SELLER_OIDC_ISSUER=http://localhost:9000`, the BFF callback `http://localhost:8080/login/oauth2/code/seller`, the same client ID and secret configured on the BFF, the BCrypt encoded client-secret hash, the signing keystore, and its Account client keystore and truststore.
3. Configure `docker/env/seller-bff.env` from `docker/env/seller-bff.env.example`. Set the issuer and callback URLs, UI and post-logout URLs, client secret, and the registration mTLS client keystore and Account truststore. For local HTTP, secure cookies are disabled in the example. Use HTTPS and secure cookies outside local development.
4. Place the PKCS#12 files under `docker/secrets/seller-oidc/{account,issuer,bff}` as mounted by the Compose overlay. Use distinct issuer and BFF client certificates. Account's TLS certificate SAN must match `account-service`. The issuer and BFF truststores must trust that certificate or its CA.
5. Start the core services and OIDC profile from the repository root.

```sh
docker compose -f docker-compose.yml -f docker-compose.seller-oidc.yml --profile seller-oidc up --build
```

The authorization server needs its dedicated PostgreSQL database and role, configured through `AUTHORIZATION_DB_URL`, `AUTHORIZATION_DB_USERNAME`, and `AUTHORIZATION_DB_PASSWORD`. Redis stores issuer sessions, BFF sessions, and pending revocations. Keep Redis and the mTLS RMI ports on the private service network.

## Browser flow

The browser fetches `GET /api/v1/identity/auth/csrf`, then posts registration JSON to `POST /api/v1/identity/auth/register` with the returned CSRF header. On success, follow the `login` relation using top-level browser navigation to `/oauth2/authorization/seller`. After the issuer callback redirects to the UI, fetch a fresh CSRF token and `GET /api/v1/identity/session`. Unsafe API calls must include the fresh CSRF header. Logout uses a top-level POST to `/api/v1/identity/auth/logout` with the CSRF form parameter.

The Seller BFF does not accept Account bearer tokens or expose JSON login and refresh routes. Context selection and live authorization continue through the OIDC session flow.

## Production

Use HTTPS for the issuer and BFF, secure HttpOnly cookies, persistent Redis with authentication and TLS, and exact registered callback and post-logout URLs. Keep the issuer client secret and signing keys in secret storage. Rotate signing keys by publishing the new current alias and retaining prior public aliases through the maximum token lifetime. Restrict Actuator endpoints to the internal network.
