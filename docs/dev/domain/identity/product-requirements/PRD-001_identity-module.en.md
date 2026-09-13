# Product Requirements Document: Identity Module

## 1. Summary

This PRD defines the identity module scope for the commerce platform.

The identity module is the source of truth for user identity, authentication,
and authorization. It must help the platform verify who is making a request,
what role they hold, and whether they are allowed to perform the requested
action.

## 2. Problem

An ecommerce platform needs verified identity and access control so that
sellers can manage only their own resources, admins can govern the platform,
and customers can browse and purchase safely.

Without this module:

- any caller can impersonate any user via the X-Actor-Id header
- sellers can modify each other's products and inventory
- admin operations like product moderation are publicly accessible
- there is no user registration or login flow
- there is no way to distinguish admin, seller, and customer actions
- storefront endpoints cannot be selectively opened to anonymous users

## 3. Users

- Platform admin
- Seller (retailer, 3P, or C2C)
- Customer
- Anonymous visitor (unauthenticated storefront browsing)

## 4. Goals

- Provide secure user registration and authentication
- Enforce role-based access control across all API endpoints
- Support seller registration with admin approval workflow
- Keep storefront product endpoints publicly accessible
- Design for future migration to an external OAuth2/OIDC identity provider

## 5. In Scope

The identity module must include:

- user registration with email and password
- user authentication with JWT token issuance
- refresh token support with rotation
- assignment of the seeded admin, seller, and customer roles
- dynamic role creation, activation, and assignment by administrators
- mapping dynamic roles to stable platform authorities
- seller registration with pending approval status
- admin approval and rejection of seller accounts
- user status lifecycle: active, pending approval, suspended
- password hashing with BCrypt
- JWT token validation on every authenticated request
- a provider-neutral bearer-token authentication contract
- role-based endpoint authorization
- public endpoint allowlisting for storefront browsing
- user profile retrieval
- token revocation on logout
- RFC 7807 ProblemDetail error responses for 401 and 403

## 6. Out of Scope

The identity module does not include:

- social login (Google, Facebook, Apple)
- multi-factor authentication
- email verification or password reset flows
- operating the commerce application itself as an OAuth2 authorization server
  or OpenID Connect provider; an external provider may supply those functions
- user profile pictures or extended profile fields
- user-defined authorities or arbitrary per-resource ACLs; the platform owns a
  stable authority catalog and administrators map roles to those authorities
- API key management
- rate limiting or brute-force protection
- audit logging of login attempts

## 7. Main Features

### 7.1 User Registration

The module must support user registration with email and password.

It must:

- accept email and password as required fields
- accept a role selection: seller or customer
- validate email format and uniqueness
- hash the password using BCrypt before storage
- create the user with the appropriate initial status:
    - customer accounts are created as ACTIVE
    - seller accounts are created as PENDING_APPROVAL
- return a JWT token pair (access + refresh) for customer registrations
- return a confirmation without a usable token for seller registrations
  pending approval

### 7.2 User Authentication

The module must support login with email and password.

It must:

- accept email and password
- verify the password against the stored BCrypt hash
- reject login for users with non-ACTIVE status
- issue a JWT access token containing issuer, subject, platform user ID, email,
  and roles
- issue a refresh token for session extension
- return token expiry information

### 7.3 Token Management

The module must support JWT-based stateless authentication.

It must:

- generate signed JWT access tokens with configurable expiration
- include standard claims: iss, sub, aud, iat, exp
- include platform claims: platform_user_id and roles (an array of role codes)
- validate tokens on every authenticated request
- support refresh token rotation: issuing a new token pair and invalidating
  the old refresh token
- store local refresh tokens only as cryptographic hashes
- support token revocation on logout

### 7.4 Dynamic Role-Based Access Control

The module must enforce access through dynamic roles mapped to stable platform
authorities. HTTP and method-level rules use authority codes so that
administrators can introduce roles without changing endpoint code.

It must seed three roles:

- **ADMIN**: full access to all endpoints including moderation, user
  management, and bulk operations
- **SELLER**: access to product CRUD, inventory CRUD, and own profile
  management
- **CUSTOMER**: access to storefront browsing, purchasing, and own profile
  management

Role requirements:

- role codes are unique, immutable machine identifiers
- role names and descriptions can be edited
- administrators can create, activate, deactivate, and assign roles
- administrators can map roles to authorities from the platform authority
  catalog
- deactivated roles do not contribute authorities
- endpoint code checks stable authorities such as PRODUCT_WRITE_OWN,
  PRODUCT_WRITE_ANY, PRODUCT_MODERATE, INVENTORY_MANAGE_OWN,
  INVENTORY_MANAGE_ANY, SELLER_APPROVE, and ROLE_MANAGE
- application handlers use the authenticated platform user ID to enforce
  seller resource ownership after the authority check

### 7.5 Public Endpoint Access

The module must allow unauthenticated access to specific endpoints.

Public endpoints:

- user registration
- user login
- token refresh
- API root discovery
- product detail by ID
- product detail by slug
- product search
- category browsing
- OpenAPI documentation (Swagger UI)

### 7.6 Seller Approval Workflow

The module must support an admin-controlled seller activation flow.

It must:

- register sellers with PENDING_APPROVAL status
- prevent login for pending sellers
- allow admins to approve a seller, changing status to ACTIVE
- allow admins to reject or suspend a seller
- emit domain events for seller status changes

### 7.7 User Status Lifecycle

The module must support a defined lifecycle for user accounts.

Statuses:

- **ACTIVE**: user can authenticate and perform role-appropriate actions
- **PENDING_APPROVAL**: seller awaiting admin activation; cannot authenticate
- **SUSPENDED**: user temporarily blocked from authentication

Status transitions:

- customer registration → ACTIVE
- seller registration → PENDING_APPROVAL
- admin approval → ACTIVE
- admin suspension → SUSPENDED
- admin reactivation → ACTIVE

### 7.8 Error Responses

The module must return security errors in the existing RFC 7807 ProblemDetail
format.

Error types:

- 401 Unauthorized: missing token, expired token, invalid token, inactive
  account
- 403 Forbidden: authenticated user lacks the required authority

Error responses must include: status, title, detail, code, traceId, path,
timestamp, and module fields consistent with the existing
GlobalApiExceptionHandler format.

## 8. Business Rules

- Email addresses must be unique across all user accounts.
- Passwords must be hashed with BCrypt before storage; plaintext passwords
  must never be persisted.
- Only users with ACTIVE status can authenticate successfully.
- Seller accounts require explicit admin approval before first login.
- JWT access tokens must have a configurable expiration (default: 1 hour).
- Refresh tokens must have a configurable expiration (default: 7 days).
- Refresh token rotation must invalidate the previous refresh token.
- Admin role cannot be self-registered; admin accounts must be created through
  a seeding process or by another admin.
- Role changes must not be possible through self-service endpoints.
- New roles receive no access until an administrator assigns authorities.
- Role codes cannot be renamed after creation.
- Public endpoints must remain accessible without any authentication token.
- All non-public endpoints must reject requests without a valid bearer access
  token.

## 9. Success Criteria

The identity module is successful when:

- users can register as customer or seller
- customers can login and receive a JWT immediately after registration
- sellers receive a pending status and cannot login until admin approves
- admins can approve, reject, and suspend seller accounts
- all non-public endpoints reject unauthenticated requests with 401
- authenticated users cannot access operations beyond their effective
  authorities with 403
- storefront product browsing works without authentication
- existing inventory controllers no longer rely on X-Actor-Id header
- the authentication mechanism can be replaced with an external IdP by
  replacing credential and token infrastructure while preserving the
  authenticated actor, platform authorization, and ownership contracts
- error responses follow the existing ProblemDetail format

## 10. External Identity Provider Compatibility

- The platform must retain a local platform user ID after adopting an external
  identity provider.
- External identities must be linked by the unique pair (issuer, subject), not
  by email.
- The external provider owns credentials and token lifecycle; the commerce
  platform remains authoritative for seller approval, suspension, platform
  roles, authorities, and resource ownership.
- Configurable provider mapping must support Keycloak realm roles, client
  roles, groups, scopes, or introspection fields without exposing
  provider-specific data to controllers.
- `AccessTokenAuthenticator` must normalize locally issued JWTs, external
  OIDC/OAuth2 JWTs validated through discovery and JWKS, and OAuth2 opaque
  tokens validated through introspection into the same `ExternalPrincipal`.
- `PlatformIdentityResolver` must convert `ExternalPrincipal` into the stable
  `AuthenticatedActor` used by controllers and handlers.
- Framework and application contracts must not expose raw JWT claims,
  introspection responses, or provider SDK types.
- Protected APIs must accept OAuth2 access tokens only; OIDC ID tokens must
  not be treated as API access tokens.

## 11. Dependencies

The identity module depends on the wider platform for:

- framework module primitives (AggregateRoot, Id, ErrorCategory, CQRS bus)
- GlobalApiExceptionHandler for consistent error formatting
- PostgreSQL for user persistence
- Spring Security OAuth2 resource-server support for bearer-token processing,
  JWT validation, opaque-token introspection, and authorization primitives

## 12. Related Documents

- `docs/BRD/commerce-platform-brd.md`
- `docs/architecture/decisions/ADR-012-authentication-authorization.md`
- `docs/architecture/diagrams/identity-security-architecture.md`
- `docs/architecture/diagrams/identity-domain-aggregates.md`
