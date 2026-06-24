# ADR 012: Authentication and Authorization

**Status:** Proposed  
**Date:** 2026-06-21

---

## 1. The Problem

**What's not working?**  
The platform currently has no authentication or authorization; all REST endpoints are publicly accessible. Actor identity is passed via an unvalidated `X-Actor-Id` header, allowing any caller to impersonate any user.

**What's at stake?**  
Without verified identities and role-based access control (RBAC), any user can mutate system state, posing a catastrophic security risk. We need a design that secures the platform immediately, manages roles dynamically, and crucially, allows future migration to an external identity provider (Keycloak, Auth0) without rewriting business logic.

---

## 2. What We Decided

**The core approach:**  
Implement a provider-neutral authentication contract, dynamic database-driven roles, and Spring Security for request-level authorization.

**Key changes:**
- Introduce a provider-neutral `AuthenticatedActor` and `AccessTokenAuthenticator` to isolate business logic from identity providers.
- Implement a dedicated `identity` bounded context (with its own database) to handle user credentials, password hashing (BCrypt), and local token issuance.
- Replace the spoofable `X-Actor-Id` header across all controllers with `@AuthenticationPrincipal SecurityPrincipal` derived from Spring's `SecurityContext`.
- Use dynamic, database-driven roles and string-based authorities instead of hardcoded Java enums to support external IdP group mappings in the future.

**What stays the same:**  
Public read access for storefront product browsing remains open. The `identity` module adheres to the same CQRS and per-module datasource patterns established in `catalog` and `inventory`.

---

## 3. Why This Approach

**Primary reasons:**
1. **Provider Agnosticism:** A provider-neutral actor contract ensures that controllers and handlers never see raw JWTs or SDK types, isolating application code from future migrations to external IdPs.
2. **Dynamic Flexibility:** Database-driven roles and mapping tables (`roles`, `user_roles`, `role_authorities`) allow admins to create new roles and assign authorities at runtime without requiring code deployments.
3. **Robust Security:** Leveraging Spring Security provides battle-tested filter chains, standardized entry points for errors (HTTP 401/403), and method-level security (`@PreAuthorize`).

---

## 4. Trade-offs

| Pros | Cons |
|-------|-------|
| Every state-changing request is strictly verified. | New `identity` module adds build and deployment complexity. |
| Dynamic role-authority assignments require no code changes. | `X-Actor-Id` retirement is a breaking change for existing API clients. |
| Provider-neutral actor contract preserves the migration path to external IdPs. | Self-hosted JWT requires local secret key management and password storage responsibilities. |
| Leverages battle-tested Spring Security primitives. | Role string comparisons lose compile-time type safety compared to enums. |

---

## 5. What Needs to Change

**New components/modules to build:**
- `identity-domain` and `identity-infrastructure` modules following CQRS principles.
- `AccessTokenAuthenticator` and `PlatformIdentityResolver` interfaces to map tokens to the `AuthenticatedActor`.

**Changes to existing systems:**
- All existing controllers using `@RequestHeader("X-Actor-Id")` must migrate to `@AuthenticationPrincipal`.
- The `GlobalApiExceptionHandler` must be updated to process Spring Security exceptions (`AccessDeniedException`, `AuthenticationException`) into the existing RFC 7807 ProblemDetail format.

**Team impact:**
- Developers must update all existing controller tests to inject a mock `SecurityContext`. API consumers must stop sending `X-Actor-Id` and start passing valid Bearer tokens in the `Authorization` header.

---

## 6. Migration Plan

- **Phase 1:** Build the `identity` bounded context (tables, repositories, login/register endpoints, self-hosted JWT issuance).
- **Phase 2:** Implement the `SecurityFilterChain` to protect endpoints using stable authorities (e.g., `PRODUCT_WRITE_ANY`).
- **Phase 3:** Migrate all existing controllers from `X-Actor-Id` to `@AuthenticationPrincipal` and drop support for the spoofable header.

**Rollback strategy:**  
If critical production endpoints are unexpectedly blocked during rollout, the `SecurityFilterChain` can be temporarily relaxed using permissive matchers (e.g., `permitAll()`) while the authorization rules are debugged.

---

## 7. Related Documents

- ADR-001: System Architecture as a Modulith
- ADR-003: Exception Handling Framework
- docs/PRD/identity-module-prd.md
- docs/BRD/identity-authentication-brd.md
