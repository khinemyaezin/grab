# Authorization Server (IAM Authority)

This service hosts the self-contained IAM Authority and OIDC / OAuth 2.0 Issuer (Keycloak alternative) backed by PostgreSQL, Redis, and outbound SCIM integration with `account-service`.

## Core Responsibilities & Architecture

1. **Local Identity & Credential Store**:
   - Manages user identity locally (`iam_user` table) with BCrypt password hashing.
   - Handles direct user self-registration via `/register`.
2. **Outbound SCIM 2.0 Synchronization**:
   - Outbound provisioning to `account-service` over Java RMI (`ScimUserPort`).
   - On local registration, automatically provisions a SCIM user to `account-service` and receives the authoritative `platform_user_id`.
3. **In-IAM Context Selection & Delayed Token Issuance**:
   - Before authorization codes or tokens are minted, authenticated users select their active access context (`/select-context`).
   - Access contexts are synced from `account-service` (`ScimUserPort.getUserAccessContexts`) and cached locally (`iam_access_context`).
   - Context switching is triggered seamlessly by clients passing `prompt=select_context` to the `/oauth2/authorize` endpoint.
4. **Token Minting & Claims**:
   - ID tokens, access tokens, and `/userinfo` responses are enriched with standard and Grab claims:
     - `platform_user_id`: Authoritative UUID for downstream domain calls.
     - `scope_key`: Generalized context category (e.g. `merchant.account`, `merchant.storefront`, `platform`).
     - `scope_id`: Identifier of the selected context (e.g. `merchant-123`, `store-456`).
     - `assignment_id`: User assignment identifier.
     - `role`: Selected context role code (e.g. `MERCHANT_OWNER`, `STORE_MANAGER`).
     - `authorities`: Granted permissions for the selected context.

## Local Compose Deployment

1. Copy `docker/env/{authorization-server,account-service,seller-bff}.env.example` to the corresponding `.env` files. Copy `docker/config/authorization-clients.yml.example` to `docker/config/authorization-clients.yml`.
2. Set a client secret in the Seller BFF environment and its delegated BCrypt hash in `AUTHORIZATION_CLIENT_0_SECRET_HASH`. Match the BFF's client ID, audience, callback, and post-logout URL to the registry. Match the BFF's public issuer URL to `AUTHORIZATION_ISSUER`.
3. Start the environment with Docker Compose:

```sh
docker compose --env-file docker/env/dev.env config --quiet
docker compose --env-file docker/env/dev.env up --build -d
```

## Client Registration & Registry

Clients are declared in `docker/config/authorization-clients.yml` with:
- `client-id`: Unique OAuth 2.0 client ID (e.g. `seller`).
- `client-secret`: BCrypt hash of client secret.
- `redirect-uris`: Exact authorization callback URIs.
- `post-logout-redirect-uris`: RP-initiated logout destination URIs.
- `scopes`: Supported scopes (`openid`, `profile`, `email`).
- Capabilities: PKCE (S256), rotating refresh tokens, and claim minting.

## Database Migrations

Database schema management uses Flyway:
- `V1__oauth2.sql`: Spring Authorization Server standard tables.
- `V2__rename_consumed_refresh.sql`: Refresh token tracking schema adjustments.
- `V3__local_users_and_access_contexts.sql`: Local user identities (`iam_user`) and cached access contexts (`iam_access_context`).
