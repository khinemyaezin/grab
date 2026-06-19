# Feature: Identity Management

## 1. Objective

Create the platform identity bounded context that owns commerce users, seller
approval, account status, dynamic roles, stable authorities, external identity
links, and local refresh sessions.

The module supports local email/password authentication now while preserving a
stable platform identity when credentials later move to an OAuth2/OIDC
provider. It does not implement an OAuth2 authorization server or OpenID
Connect provider.

---

## 2. Scope

### In scope

- Local customer and seller registration with email and password
- BCrypt password hashing; plaintext passwords are never persisted
- Local login, access-token issuance through a port, refresh rotation, logout
- Seller approval and account suspension/reactivation
- Stable platform user IDs used for product and inventory ownership
- Dynamic roles and many-to-many user-role assignment
- Stable platform authority catalog and role-authority assignment
- External identity links keyed by `(issuer, subject)`
- Provider-entitlement-to-platform-role mappings
- User profile and administrator management APIs
- Domain events and identity outbox integration
- Dedicated identity datasource and transaction manager

### Out of scope

- Running an OAuth2 authorization server or OIDC provider
- Keycloak deployment and provider-specific login UI
- Social login, MFA, email verification, and password reset
- User-defined authority codes; authorities are stable platform capabilities
- Arbitrary per-resource ACLs; handlers enforce ownership
- Brute-force protection, login-attempt auditing, and rate limiting

### Delivery dependency

Implement domain, persistence, seed data, resolver, and CQRS use cases first
with a mocked `TokenIssuer`. FAT-0005 then supplies local token/security beans;
local login and end-to-end registration acceptance complete when both features
are assembled. FAT-0006 depends on the resulting principal and authorities.

---

## 3. Boundaries and Shared Contracts

The framework exposes provider-neutral records and ports:

```java
public record AuthenticatedActor(
    String platformUserId,
    String issuer,
    String subject,
    String email,
    Set<String> roles,
    Set<String> authorities
) {}

public record ExternalPrincipal(
    String issuer,
    String subject,
    Optional<String> email,
    Set<String> entitlements
) {}

public interface AccessTokenAuthenticator {
    ExternalPrincipal authenticate(String bearerToken);
}

public interface PlatformIdentityResolver {
    AuthenticatedActor resolve(ExternalPrincipal principal);
}
```

Identity application code defines a local token-lifecycle port:

```java
public interface TokenIssuer {
    TokenPair issue(AuthenticatedActor actor);
    TokenPair refresh(String refreshToken);
    void revoke(String refreshToken);
}
```

`TokenIssuer` is deliberately not the access-token validation abstraction.
FAT-0005 supplies the local implementation. With Keycloak, provider endpoints
replace local login, refresh, and logout while `AuthenticatedActor` remains
unchanged.

---

## 4. Architectural Placement

```text
identity-domain/src/main/java/com/identity/domain/
├── aggregate/       User, Role
├── entity/          Authority, ExternalIdentity, ExternalEntitlementMapping,
│                    RefreshSession
├── valueobject/     Email, HashedPassword
├── enums/           UserStatus
├── repository/      domain repository ports
├── service/         PasswordHasher and domain policy interfaces
├── event/           identity domain events
└── exception/       identity domain validation errors

identity-infrastructure/src/main/java/com/identity/infrastructure/
├── config/          datasource and persistence configuration
├── entity/          JPA entities
├── mapper/jpa/      MapStruct entity mappers and assemblers
├── repository/jpa/ Spring Data repositories and domain adapters
└── outbox/           identity outbox producer integration

store/src/main/java/com/grab/store/identity/
├── IdentityModule.java
├── IdentityRootController.java
└── internal/
    ├── api/rest/{controller,dto,service,mapper,assembler}/
    ├── command/ and command/handler/
    ├── query/ and query/handler/
    ├── config/
    ├── event/
    └── exception/
```

`IdentityModule` is a Spring Modulith module with only documented shared
dependencies. Controllers delegate to command/query services, services map and
dispatch through the buses, and handlers own business logic. Cross-module
effects use events/outbox rather than direct calls into catalog or inventory.

Every REST operation has one abstract MapStruct request mapper configured with
the identity module's `CentralMapperConfig` and `IdMapper`. Command/query
records use framework `Id`; results and DTOs never expose aggregates or JPA
entities. Write handlers use `@IdentityTransactional`; read handlers use
`@IdentityReadTransactional`.

---

## 5. Domain Model

| Concept | Type | Responsibility |
|---|---|---|
| `User` | Aggregate Root | Platform identity, lifecycle, role assignments |
| `Email` | Value Object | Normalized and validated profile/login email |
| `HashedPassword` | Value Object | Optional BCrypt credential for local users |
| `Role` | Aggregate Root | Dynamic role with immutable code and editable display data |
| `Authority` | Entity/reference data | Stable platform capability |
| `ExternalIdentity` | Entity | Links provider `(issuer, subject)` to platform user |
| `ExternalEntitlementMapping` | Entity | Maps provider role/group/scope to platform role |
| `RefreshSession` | Entity | Hashed, rotating local refresh token family |
| `UserStatus` | Enum | `ACTIVE`, `PENDING_APPROVAL`, `SUSPENDED` |

### User operations

```java
User.createLocal(id, email, hashedPassword, initialRoleIds)
User.createExternal(id, email, externalIdentity, initialRoleIds)
user.assignRole(roleId)
user.revokeRole(roleId)
user.activate()
user.suspend()
user.reactivate()
user.linkExternalIdentity(externalIdentity)
```

### Invariants

- Platform user UUID and normalized email are unique.
- `(issuer, subject)` identifies at most one platform user.
- Local users require a BCrypt password hash; external-only users do not.
- Registration accepts only the seeded `CUSTOMER` or `SELLER` role.
- Customer registration creates an `ACTIVE` user.
- Seller registration creates a `PENDING_APPROVAL` user.
- `ADMIN` and other dynamic roles cannot be self-selected.
- Only `ACTIVE` users resolve to an authenticated actor.
- Role codes and authority codes are unique, uppercase machine identifiers.
- Role codes are immutable; role name and description are editable.
- A new or deactivated role contributes no authority unless it is active and
  has active authority mappings.
- Role and authority changes are administrator-only operations.
- Raw refresh tokens are returned once and never persisted; only hashes are
  stored.
- Refresh rotation is atomic and invalidates the previous token.
- Reuse of a rotated token revokes its token family.

---

## 6. Seed Data

Seed stable authorities before roles. Initial authorities include:

| Authority | Purpose |
|---|---|
| `PRODUCT_WRITE_OWN` | Create and update the actor's products |
| `PRODUCT_WRITE_ANY` | Manage products for any seller |
| `PRODUCT_MODERATE` | Approve, reject, suspend, restore products |
| `PRODUCT_BULK_WRITE` | Execute bulk catalog operations |
| `INVENTORY_MANAGE_OWN` | Manage the actor's inventory resources |
| `INVENTORY_MANAGE_ANY` | Manage inventory for any seller |
| `SELLER_APPROVE` | Approve pending sellers |
| `USER_READ` | Read administrative user views |
| `USER_SUSPEND` | Suspend and reactivate accounts |
| `ROLE_MANAGE` | Create roles and manage authority mappings |

Seed `ADMIN`, `SELLER`, and `CUSTOMER` roles and their default mappings. Seed
operations must be idempotent and must not overwrite administrator changes on
subsequent startup.

---

## 7. Persistence Model

Required tables:

```text
users
  id, uuid, email, password_hash nullable, status, created_at, updated_at

roles
  id, uuid, code unique immutable, name, description, active, created_at

authorities
  id, uuid, code unique immutable, name, description, active

user_roles
  user_id, role_id, assigned_at, unique(user_id, role_id)

role_authorities
  role_id, authority_id, assigned_at, unique(role_id, authority_id)

external_identities
  id, user_id, issuer, subject, linked_at, unique(issuer, subject)

external_entitlement_mappings
  id, issuer, entitlement, role_id,
  unique(issuer, entitlement, role_id)

refresh_sessions
  id, user_id, token_hash unique, token_family_id, expires_at,
  revoked_at nullable, replaced_by_id nullable, created_at, last_used_at
```

The identity module has its own datasource, entity manager factory, Flyway
history, and transaction manager. Refresh rotation uses a row lock or atomic
compare-and-update so two uses of the same token cannot both succeed.

---

## 8. Application Use Cases

### Commands

| Command | Required authority | Result |
|---|---|---|
| `RegisterUserCommand` | Public | Customer token pair or pending seller |
| `LoginCommand` | Public | Local token pair for active user |
| `RefreshTokenCommand` | Public possession check | Rotated local token pair |
| `LogoutCommand` | Authenticated | Current refresh session revoked |
| `ApproveSellerCommand` | `SELLER_APPROVE` | Pending seller becomes active |
| `SuspendUserCommand` | `USER_SUSPEND` | User becomes suspended; sessions revoked |
| `ReactivateUserCommand` | `USER_SUSPEND` | Suspended user becomes active |
| `AssignUserRoleCommand` | `ROLE_MANAGE` | Dynamic role assigned to user |
| `RevokeUserRoleCommand` | `ROLE_MANAGE` | Role removed from user |
| `CreateRoleCommand` | `ROLE_MANAGE` | Inactive or empty dynamic role created |
| `UpdateRoleCommand` | `ROLE_MANAGE` | Display data or active state updated |
| `AssignRoleAuthorityCommand` | `ROLE_MANAGE` | Stable authority assigned to role |
| `MapExternalEntitlementCommand` | `ROLE_MANAGE` | Provider entitlement mapped to role |
| `LinkExternalIdentityCommand` | Controlled provisioning | Provider subject linked to user |

### Queries

| Query | Required authority |
|---|---|
| `GetOwnProfileQuery` | Authenticated |
| `ListUsersQuery` | `USER_READ` |
| `GetUserByIdQuery` | `USER_READ` |
| `ListRolesQuery` | `ROLE_MANAGE` |
| `GetRoleQuery` | `ROLE_MANAGE` |
| `ListAuthoritiesQuery` | `ROLE_MANAGE` |

### Platform identity resolution

`PlatformIdentityResolver.resolve()` performs these steps:

1. Find `ExternalIdentity` by `(issuer, subject)`.
2. For the local issuer only, allow `subject` to resolve directly to the
   platform user established at registration.
3. Fail closed when no link exists unless an explicit provisioning policy is
   configured; never auto-link by email.
4. Require `UserStatus.ACTIVE`.
5. Combine directly assigned roles with mapped provider entitlements.
6. Ignore inactive roles, authorities, and mappings.
7. Return distinct role and authority codes in `AuthenticatedActor`.

---

## 9. HTTP API

### Public local-authentication endpoints

| Method | Path | Result |
|---|---|---|
| POST | `/api/v1/identity/auth/register` | `201`; active customer token pair or pending seller |
| POST | `/api/v1/identity/auth/login` | `200`; local token pair |
| POST | `/api/v1/identity/auth/refresh` | `200`; rotated local token pair |

### Authenticated endpoints

| Method | Path | Authority |
|---|---|---|
| POST | `/api/v1/identity/auth/logout` | Authenticated |
| GET | `/api/v1/identity/profile` | Authenticated |
| GET | `/api/v1/identity/admin/users` | `USER_READ` |
| GET | `/api/v1/identity/admin/users/{id}` | `USER_READ` |
| POST | `/api/v1/identity/admin/users/{id}/approve` | `SELLER_APPROVE` |
| POST | `/api/v1/identity/admin/users/{id}/suspend` | `USER_SUSPEND` |
| POST | `/api/v1/identity/admin/users/{id}/reactivate` | `USER_SUSPEND` |
| PUT/DELETE | `/api/v1/identity/admin/users/{id}/roles/{roleCode}` | `ROLE_MANAGE` |
| GET/POST | `/api/v1/identity/admin/roles` | `ROLE_MANAGE` |
| PATCH | `/api/v1/identity/admin/roles/{roleCode}` | `ROLE_MANAGE` |
| PUT/DELETE | `/api/v1/identity/admin/roles/{roleCode}/authorities/{code}` | `ROLE_MANAGE` |
| GET | `/api/v1/identity/admin/authorities` | `ROLE_MANAGE` |

Responses expose `roles` as an array. Administrative role responses include
effective authority codes. Password hashes, refresh hashes, and provider raw
claims are never serialized.

Controllers return `EntityModel<T>` for single resources and `PagedModel` for
administrative collections. `IdentityRootController` produces HAL and exposes
action-explicit links such as `get-identity-root`, `get-profile`,
`list-users`, and `list-roles`. `ApiRootController` links the new bounded
context with `get-identity-root`. Assemblers use `linkTo(methodOn(...))`, use
`self` only for canonical GET representations, and omit actions the current
actor cannot perform.

---

## 10. Events and Side Effects

- `UserRegisteredEvent`
- `UserStatusChangedEvent`
- `UserRoleChangedEvent`
- `RoleCreatedEvent`
- `RoleAuthorityChangedEvent`
- `ExternalIdentityLinkedEvent`

Suspension revokes all local refresh sessions. Access tokens are also rejected
on subsequent resolution because the platform status check occurs for every
protected request. Events use the outbox pattern in the identity transaction.

---

## 11. Detailed Implementation Plan

1. **Create shared contracts**
   - Add `AuthenticatedActor`, `ExternalPrincipal`,
     `AccessTokenAuthenticator`, and `PlatformIdentityResolver` to framework.
   - Add `UNAUTHORIZED` and `FORBIDDEN` error categories.
   - Add contract tests for null handling, immutable sets, and duplicate
     normalization.

2. **Build the identity domain**
   - Implement value objects, user lifecycle, roles, authorities, external
     links, entitlement mappings, refresh sessions, and domain events.
   - Keep role codes immutable and local credentials optional.
   - Unit-test every valid and invalid transition.

3. **Create schema and persistence adapters**
   - Add migrations in dependency order: users, roles/authorities, join tables,
     external mappings, refresh sessions, outbox.
   - Implement JPA entities, assemblers, repositories, pagination, and
     transaction meta-annotations.
   - Add repository integration tests for uniqueness and refresh locking.

4. **Seed authorities and base roles**
   - Implement idempotent startup seeding.
   - Verify default role-authority mappings and preservation of later edits.

5. **Implement registration and local sessions**
   - Normalize email, enforce uniqueness, hash with BCrypt, resolve permitted
     registration role, persist user, and publish the event.
   - Implement login against active local users through the `TokenIssuer` port.
   - Implement hashed refresh rotation, family reuse detection, and logout.

6. **Implement platform identity resolution**
   - Resolve local and external subjects, status, roles, entitlement mappings,
     and effective authorities.
   - Add a short-lived cache only behind the resolver; invalidate it on user,
     role, authority, or mapping changes.

7. **Implement management CQRS handlers and APIs**
   - Add seller lifecycle, user-role, role-authority, and provider-mapping
     commands with authority guards.
   - Add profiles, administrative queries, HATEOAS assemblers, validation, and
     RFC 7807 errors.
   - Create one DTO record and one abstract request mapper per operation;
     services only map, dispatch, and map results.
   - Return paginated users/roles through `PagedResourcesAssembler` and add
     `list-users`/`list-roles` links to each `PagedModel`.

8. **Wire assembly and configuration**
   - Register modules, dependencies, datasource settings, Flyway, local issuer,
     BCrypt encoder, and identity discovery link.
   - FAT-0005 supplies the local `TokenIssuer` and bearer authenticator beans.

9. **Test end to end**
   - Run domain, repository, handler, API, concurrency, and outbox tests.
   - Verify no secret or token hash appears in logs or responses.

---

## 12. Test Plan

Test methods follow `{functionName}_{input}_{expectedBehavior}`. Required
suites include domain invariant tests, JPA repository integration tests,
refresh concurrency tests, CQRS handler tests, controller slice tests,
HATEOAS link tests, Modulith verification, outbox tests, and end-to-end local
registration/login tests.

Representative cases:

- `createLocal_withSellerRole_shouldCreatePendingUser`
- `assignRole_withInactiveRole_shouldRejectAssignment`
- `rotate_withConcurrentReuse_shouldAllowOnlyOneReplacement`
- `resolve_withMappedEntitlement_shouldReturnEffectiveAuthorities`
- `resolve_withEmailMatchButNoIdentityLink_shouldRejectAuthentication`
- `listUsers_withUserReadAuthority_shouldReturnPagedHalModel`
- `suspendUser_withActiveSessions_shouldRevokeAllRefreshSessions`

---

## 13. Acceptance Criteria

- [ ] Customer registration creates an active user and returns a local token pair.
- [ ] Seller registration creates a pending user and returns no usable token.
- [ ] Admin and arbitrary dynamic roles cannot be self-registered.
- [ ] Passwords and refresh tokens are stored only as hashes.
- [ ] A refresh token succeeds once, rotates atomically, and cannot be replayed.
- [ ] Replay of a rotated refresh token revokes the whole token family.
- [ ] Suspension blocks identity resolution and revokes local refresh sessions.
- [ ] Role codes cannot be renamed; display fields can be edited.
- [ ] A new role grants no access until stable authorities are assigned.
- [ ] Active role-authority mappings determine effective authorities at runtime.
- [ ] `(issuer, subject)` uniquely resolves an external account to one platform user.
- [ ] External identities are never auto-linked by matching email.
- [ ] Provider roles, groups, or scopes can map to local dynamic roles.
- [ ] External-only users can exist without a local password hash.
- [ ] All administrative APIs require their documented authority.
- [ ] Events are written to the outbox in the same identity transaction.
- [ ] RFC 7807 responses cover duplicate email, invalid credentials, inactive
      account, invalid transition, missing mapping, and access denial.
- [ ] Domain, persistence, CQRS, API, concurrency, and integration tests pass.
