# Seller BFF

Platform BFF web boundary for Seller Central. Implements a provider-agnostic Spring Security OAuth 2.0 / OIDC Client with standard discovery via `issuer-uri` and server-side Redis sessions.

## Core Responsibilities & Architecture

1. **Standard Spring Security OIDC Client**:
   - Uses Spring Security's native `oauth2Login()`.
   - Auto-discovers endpoints via OIDC discovery (`.well-known/openid-configuration`).
   - S256 PKCE authorization code flow.
2. **Context-Aware Security Principal**:
   - `SellerSecurityPrincipal` implements both `OidcUser` and `UserDetails`.
   - Automatically parses minted claims (`platform_user_id`, `scope_key`, `scope_id`, `assignment_id`, `role`, `authorities`) on successful authentication.
   - Downstream controllers inject `@AuthenticationPrincipal SellerSecurityPrincipal` and use `requiredScopeId()` and `requiredPlatformUserId()`.
3. **In-IAM Context Switching**:
   - BFF does not manually mutate tokens or access states in Redis.
   - When context needs to be switched or selected, BFF routes (`/api/v1/identity/session/contexts/switch` and `/select`) redirect to `/oauth2/authorization/seller?prompt=select_context`.
   - The authorization server prompts context selection before re-minting updated tokens.
4. **Session Management**:
   - Backed by Redis Spring Session (`@EnableRedisHttpSession`).
   - Stores authenticated user session with configurable cookie properties (`SELLER_TEST_SESSION` / `GRAB_SELLER_SESSION`).
5. **Pure RMI Downstream Boundary**:
   - Downstream services (`merchandising`, `inventory`, `checkout`, `channel`) do NOT run OAuth 2.0 Resource Servers or JWT decoders.
   - BFF controllers map HTTP requests directly to domain commands carrying `platformUserId` and `scopeId` over high-performance Java RMI ports.

## Configuration

Standard environment variables and properties:
- `grab.seller-oidc.issuer`: Public issuer URI (e.g. `http://localhost:9000`).
- `grab.seller-oidc.client-id`: Registered OAuth client ID (`seller`).
- `grab.seller-oidc.client-secret`: Client secret matching the authorization server registry.
- `grab.seller-oidc.callback`: Authorized redirect URI (`http://localhost:8080/login/oauth2/code/seller`).
- `grab.seller-oidc.ui`: Frontend UI landing URL.
- `grab.seller-oidc.post-logout`: Redirect target upon logout completion.

## Authentication & Context Flow

1. Browser requests `GET /api/v1/identity/auth/csrf` to retrieve CSRF token.
2. For registration: `GET /api/v1/identity/auth/register-url` returns the IAM registration page URL (`/register`).
3. For login: Navigate to `GET /oauth2/authorization/seller` to start authorization code flow with PKCE.
4. After callback, Spring Security creates the `SellerSecurityPrincipal` and stores the authenticated session in Redis.
5. All protected endpoints verify context via `SellerContextRequiredFilter`:
   - If no context is selected: responds with `409 Conflict` (`idt.service.auth.access_context_required`).
   - If context scope matches resource: proceeds to controller.
   - If context scope mismatches: responds with `403 Forbidden` (`idt.service.auth.access_denied`).
6. Context switch: POST or GET `/api/v1/identity/session/contexts/switch` redirects to `/oauth2/authorization/seller?prompt=select_context`.
7. Logout: POST `/api/v1/identity/auth/logout` invalidates the Spring Session.
