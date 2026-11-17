# Seller Authorization Server

This Boot service issues OIDC tokens for Seller BFF. Account remains the only store for user passwords, assignments, roles, and permissions. The issuer has a separate PostgreSQL database role and schema, `authorization_server`. Provision the database and give its dedicated role permission to create the schema and run its Flyway migrations before starting.

## Local deployment

1. Copy the three environment examples in `docker/env` to their corresponding ignored `.env` files. Set a random confidential client secret in the BFF file and its Spring delegated BCrypt encoding, prefixed with `{bcrypt}`, in the issuer file. Use single quotes around the BCrypt value in Compose env files so the dollar signs are literal.
2. Mount PKCS#12 files using the directories in `docker-compose.seller-oidc.yml`. Account's server certificate must have the name clients connect to in its SAN, normally `account-service`. Use distinct issuer and BFF client certificates with client-auth usage. Each Account peer allowlist contains only the client leaf certificates authorized for that port. Each client truststore trusts the Account server certificate or its CA. The issuer signing keystore has a separate RSA key of at least 2048 bits.
3. Set `SELLER_AUTH_RPC_ENABLED=true` in the Account environment file and `SELLER_OIDC_ENABLED=true` in the Seller BFF file. Start the services with the overlay:

   ```sh
   docker compose -f docker-compose.yml -f docker-compose.seller-oidc.yml --profile seller-oidc up --build
   ```

The profile expects a reachable authorization database in `AUTHORIZATION_DB_URL`. The optional local Redis instance stores both server-side sessions and pending revocations. The two mTLS RMI ports are private. For production, use HTTPS for issuer and BFF, set both secure-cookie flags to true, and configure a private, persistent Redis service with authentication and TLS. Restrict Actuator metrics to the internal network.

## Browser integration

Navigate to `GET /oauth2/authorization/seller` to log in. After the callback, fetch `GET /api/v1/identity/auth/csrf` with the BFF session cookie and send the returned token using its `headerName` on unsafe API methods. Read `GET /api/v1/identity/session` and `GET /api/v1/identity/session/contexts`. The selection POST returns HAL context metadata and no token strings. For logout, submit a top-level POST with the CSRF form parameter so the browser follows the issuer logout redirect. Fetch a fresh CSRF token after login or logout.

Rotate signing keys by deploying a new current alias and retaining the previous public aliases in `SELLER_OIDC_RETIRING_ALIASES` for at least the lifetime of tokens signed by them. Use distinct key aliases. Keep issuer URLs and registered callback URLs stable during a deploy.

The existing bearer issuer works independently of this browser mode. Before enabling the opt-in mode in production, verify the roles granted to real Seller accounts against the route permissions, the app's navigation and CSRF behavior, and legacy bearer callers. Monitor the `seller.oidc.*` metrics for denied logins and switches, callback failures, Account outages, introspection latency, refresh replay, and pending revocations.
