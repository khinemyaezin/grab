# Authorization Server

This service hosts one OIDC issuer backed by PostgreSQL, Redis, and Account. Client registrations come from YAML. Account verifies credentials and resolves active subjects over private mTLS RMI. The optional `seller-context` capability enables Seller Central's assignment claims and context grant.

## Local Compose deployment

1. Copy `docker/env/{authorization-server,account-service,seller-bff}.env.example` to the corresponding `.env` files. Copy `docker/config/authorization-clients.yml.example` to `docker/config/authorization-clients.yml`.
2. Set a random client secret in the Seller BFF environment and its delegated BCrypt hash in `AUTHORIZATION_CLIENT_0_SECRET_HASH`. Keep single quotes around the hash in the Compose env file to preserve its dollar signs. Match the BFF's client ID, audience, callback, and post-logout URL to the registry. Generate a separate secret for each client. Match the BFF's public issuer URL to `AUTHORIZATION_ISSUER`.
3. Mount the files in the table below under `docker/secrets/authorization`. Set their passwords in the corresponding service env files. Use PKCS#12 files readable by container UID 1001. Give Account's server certificate a DNS SAN of `account-service`. Use separate issuer and BFF client certificates with client-auth usage. Each peer truststore on Account should trust the authorized client leaf. Client truststores trust the Account server certificate or its CA. Use a separate RSA signing key of at least 2048 bits.
4. Set the issuer database password and start the overlay. The provisioning job creates the dedicated role and schema and also works on an initialized PostgreSQL volume. Its defaults match `jdbc:postgresql://postgres:5432/authorization`. Set `AUTHORIZATION_DB_NAME` in the Compose interpolation environment if you change the database name. Change an existing role's password in PostgreSQL before deploying a new password.

| Service | Files in its secrets subdirectory |
|---|---|
| `account` | `server.p12`, `issuer-peers.p12`, `bff-peers.p12` |
| `issuer` | `signing.p12`, `client.p12`, `account-trust.p12` |
| `bff` | `client.p12`, `account-trust.p12` |

```sh
docker compose -f docker-compose.yml -f docker-compose.authorization.yml --profile authorization config --quiet
docker compose -f docker-compose.yml -f docker-compose.authorization.yml --profile authorization up --build --wait seller-bff
```

The browser uses the public issuer URL. Seller BFF calls the issuer over the private Compose backchannel, `http://authorization-server:9000` by default. Keep the mTLS listeners and Redis private. If you change the published issuer or BFF port, update the corresponding public URL and redirects. The registry is mounted read-only through `SPRING_CONFIG_IMPORT`. Set `AUTHORIZATION_CLIENTS_FILE` and `AUTHORIZATION_SECRETS_DIR` in the Compose interpolation environment to select other paths.

## Register another client

Append a client entry with its own client ID, name, delegated BCrypt hash, exact redirect and optional logout URIs, `openid` scope, and unique access-token audience. Use an empty capability list for a basic client. All clients use `client_secret_basic`, authorization code with S256 PKCE, and rotating refresh tokens. Apply additions, updates, or removal by redeploying all issuer replicas with the same registry. JDBC retains historical registrations and grants. Revocation and client ownership checks use the active registry.

## Production and existing installations

Use the `prod` profile, public HTTPS URLs, exact HTTPS redirects, secure BFF cookies, persistent authenticated TLS Redis, and private management endpoints. Terminate HTTPS at the deployment proxy and give the issuer the same public URL it advertises. Provision the issuer role, database, and schema before startup when deploying with managed PostgreSQL. Set the Account transport listener and truststore on Account before bringing up the issuer. Rotate keys by setting `AUTHORIZATION_SIGNING_ALIAS` to the new alias and retaining previous public aliases in `AUTHORIZATION_RETIRING_ALIASES` through the lifetime of signed tokens.

For cutover, deploy Account first so port 1199 serves both identity bindings. Stop the previous issuer instances, copy their `SELLER_OIDC_*` issuer settings to the matching `AUTHORIZATION_*` settings, and move the former single-client settings into the YAML registry. Keep the same client ID, audience, signing alias, public URLs, Redis data and namespaces, and cookie names. The registry preserves the stored JDBC registration ID. Reuse the existing Compose project and volumes. To reuse the previous secrets directory, set `AUTHORIZATION_SECRETS_DIR=./docker/secrets/seller-oidc`.

Start the new issuer against the existing PostgreSQL database so Flyway applies V2, then bring up the BFF and verify discovery, login, refresh, assignment selection, and logout. V2 retains an updatable compatibility view for the previous issuer. To roll back, stop the new issuer and restore the previous image and configuration against the same database and Redis. Sessions first authenticated by the new issuer may require signing in again on the previous image. Keep V2 in the schema history.

## Browser smoke test

Navigate to `GET /oauth2/authorization/seller` on the BFF. Sign in with an active Account user. After the callback, fetch `GET /api/v1/identity/auth/csrf` and send the returned token using its `headerName` on unsafe API requests. Check `GET /api/v1/identity/session` and `GET /api/v1/identity/session/contexts`. For logout, submit a top-level POST with the CSRF form parameter so the browser follows the issuer redirect. Fetch a fresh CSRF token after login or logout.

Monitor `authorization.oidc.*` on the issuer and `seller.oidc.*` on the BFF.
