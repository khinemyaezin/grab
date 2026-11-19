# Seller Central OpenID Connect

The standalone Seller BFF supports compatible authorization servers through deployment configuration. Account remains the source of truth for platform identity, external identity links, seller access assignments, permissions, and lifecycle authorization. The browser uses a server-side session; OAuth tokens never leave the BFF.

- **AC1** OIDC authorization code uses S256 PKCE, state, and nonce. Registration, profile, context, logout, CSRF, and HAL routes keep their existing browser contract.
- **AC2** Protected requests require a browser session and live access-token introspection. Bearer-only authentication is rejected.
- **AC3** Account resolves the exact issuer-subject identity and current seller authorization. Non-native issuers require pre-provisioned links; email or subject equality never links identities.
- **AC4** Context selection commits atomically. Denial preserves the previous selection, and lifecycle revisions invalidate stale sessions.
- **AC5** Deployment settings and secrets are externally configured. Unsafe production URLs, cookies, CORS, Redis, or mTLS settings fail startup without logging secret values.
- **AC6** The same packaged BFF completes the seller flow with two compatible issuers. Provider changes use a fresh session generation and preserve revocations for the issuer that owns them.

See the [technical specification](../system/seller-oidc-tech-spec.md) and [implementation plan](../../bff_oauth.md) for provider requirements, deployment, rollout, and verification.
