# Identity Domain Aggregate Diagram

This diagram reflects the identity domain model for authentication and
authorization with dynamic, database-driven roles.

## Class Diagram

### User Aggregate

```mermaid
classDiagram
    direction TD

    namespace UserAggregate {
        class User {
            <<AggregateRoot>>
            +Id id
            +Email email
            +Optional~HashedPassword~ passwordHash
            +Set~Role~ roles
            +UserStatus status
            +LocalDateTime createdAt
            +LocalDateTime updatedAt
            +createLocal(id, email, hashedPassword, roles) User
            +createExternal(id, email, externalIdentity, roles) User
            +assignRole(role)
            +revokeRole(role)
            +activate()
            +suspend()
            +reactivate()
            +isActive() boolean
            +isPendingApproval() boolean
            +hasRole(roleCode) boolean
            +getRoleCodes() Set~String~
        }

        class Role {
            <<AggregateRoot>>
            +Id id
            +String code
            +String name
            +String description
            +boolean active
            +LocalDateTime createdAt
            +create(id, code, name, description) Role
            +deactivate()
            +activate()
            +updateDetails(name, description)
            +assignAuthority(authority)
            +revokeAuthority(authority)
        }

        class Authority {
            <<Entity>>
            +Id id
            +String code
            +String name
            +String description
            +boolean active
        }

        class ExternalIdentity {
            <<Entity>>
            +Id id
            +String issuer
            +String subject
            +LocalDateTime linkedAt
        }

        class ExternalEntitlementMapping {
            <<Entity>>
            +Id id
            +String issuer
            +String entitlement
            +Id platformRoleId
        }

        class RefreshSession {
            <<Entity>>
            +Id id
            +String tokenHash
            +String tokenFamilyId
            +LocalDateTime expiresAt
            +LocalDateTime revokedAt
        }

        class Email {
            <<ValueObject>>
            +String value
            +of(value) Email
            +validate(value) void
        }

        class HashedPassword {
            <<ValueObject>>
            +String hash
            +of(hash) HashedPassword
        }

        class UserStatus {
            <<enumeration>>
            ACTIVE
            PENDING_APPROVAL
            SUSPENDED
        }
    }

    User "1" --> "*" Role : "has many"
    Role "*" --> "*" Authority : "grants"
    User "1" --> "*" ExternalIdentity : "linked identities"
    User "1" --> "*" RefreshSession : "local sessions"
    User --> Email : "identity"
    User --> HashedPassword : "optional local credential"
    User --> UserStatus : "lifecycle"
```

### Entity Relationship

```mermaid
erDiagram
    USERS {
        bigserial id PK
        varchar uuid UK
        varchar email UK
        varchar password_hash "nullable for external-only users"
        varchar status
        timestamp created_at
        timestamp updated_at
    }

    ROLES {
        bigserial id PK
        varchar uuid UK
        varchar code UK
        varchar name
        varchar description
        boolean active
        timestamp created_at
    }

    USER_ROLES {
        bigint user_id FK
        bigint role_id FK
        timestamp assigned_at
    }

    AUTHORITIES {
        bigserial id PK
        varchar uuid UK
        varchar code UK
        varchar name
        varchar description
        boolean active
    }

    ROLE_AUTHORITIES {
        bigint role_id FK
        bigint authority_id FK
        timestamp assigned_at
    }

    EXTERNAL_IDENTITIES {
        bigserial id PK
        bigint user_id FK
        varchar issuer
        varchar subject
        timestamp linked_at
    }

    EXTERNAL_ENTITLEMENT_MAPPINGS {
        bigserial id PK
        varchar issuer
        varchar entitlement
        bigint role_id FK
    }

    REFRESH_SESSIONS {
        bigserial id PK
        bigint user_id FK
        varchar token_hash UK
        varchar token_family_id
        timestamp expires_at
        timestamp revoked_at
        bigint replaced_by_id FK
    }

    USERS ||--o{ USER_ROLES : "has"
    ROLES ||--o{ USER_ROLES : "assigned to"
    ROLES ||--o{ ROLE_AUTHORITIES : "grants"
    AUTHORITIES ||--o{ ROLE_AUTHORITIES : "included in"
    USERS ||--o{ EXTERNAL_IDENTITIES : "linked to"
    ROLES ||--o{ EXTERNAL_ENTITLEMENT_MAPPINGS : "mapped from provider access"
    USERS ||--o{ REFRESH_SESSIONS : "has local sessions"
```

`EXTERNAL_IDENTITIES` has a unique constraint on `(issuer, subject)`.
`EXTERNAL_ENTITLEMENT_MAPPINGS` has a unique constraint on
`(issuer, entitlement, role_id)`.

### User Status Lifecycle

```mermaid
stateDiagram-v2
    [*] --> ACTIVE : register (customer)
    [*] --> PENDING_APPROVAL : register (seller role)

    PENDING_APPROVAL --> ACTIVE : admin approve
    PENDING_APPROVAL --> SUSPENDED : admin reject

    ACTIVE --> SUSPENDED : admin suspend

    SUSPENDED --> ACTIVE : admin reactivate
```

### Domain Events

```mermaid
classDiagram
    direction LR

    class UserRegisteredEvent {
        <<Event>>
        +Id userId
        +String email
        +Set~String~ roleCodes
        +UserStatus status
        +LocalDateTime occurredAt
    }

    class UserStatusChangedEvent {
        <<Event>>
        +Id userId
        +UserStatus previousStatus
        +UserStatus newStatus
        +String changedBy
        +LocalDateTime occurredAt
    }

    class UserRoleChangedEvent {
        <<Event>>
        +Id userId
        +String roleCode
        +String action
        +String changedBy
        +LocalDateTime occurredAt
    }

    class RoleCreatedEvent {
        <<Event>>
        +Id roleId
        +String code
        +String name
        +LocalDateTime occurredAt
    }

    class RoleAuthorityChangedEvent {
        <<Event>>
        +Id roleId
        +String authorityCode
        +String action
        +String changedBy
        +LocalDateTime occurredAt
    }
```

## Domain Services

### PasswordHasher

```mermaid
classDiagram
    direction LR

    class PasswordHasher {
        <<interface>>
        +hash(rawPassword String) HashedPassword
        +verify(rawPassword String, hashedPassword HashedPassword) boolean
    }

    class BcryptPasswordHasher {
        +hash(rawPassword String) HashedPassword
        +verify(rawPassword String, hashedPassword HashedPassword) boolean
    }

    PasswordHasher <|.. BcryptPasswordHasher : implements
```

## Repository Interfaces

```mermaid
classDiagram
    direction LR

    class UserRepository {
        <<interface>>
        +findById(id Id) Optional~User~
        +findByEmail(email Email) Optional~User~
        +existsByEmail(email Email) boolean
        +save(user User) User
    }

    class RoleRepository {
        <<interface>>
        +findById(id Id) Optional~Role~
        +findByCode(code String) Optional~Role~
        +findAllActive() List~Role~
        +findByCodes(codes Set~String~) Set~Role~
        +existsByCode(code String) boolean
        +save(role Role) Role
    }

    class AuthorityRepository {
        <<interface>>
        +findByCode(code String) Optional~Authority~
        +findAllActive() List~Authority~
        +findByRoleCodes(roleCodes Set~String~) Set~Authority~
    }

    class ExternalIdentityRepository {
        <<interface>>
        +findByIssuerAndSubject(issuer String, subject String) Optional~ExternalIdentity~
        +save(identity ExternalIdentity) ExternalIdentity
    }


    class ExternalEntitlementMappingRepository {
        <<interface>>
        +findRoleCodes(issuer String, entitlements Set~String~) Set~String~
        +save(mapping ExternalEntitlementMapping) ExternalEntitlementMapping
    }

    class RefreshSessionRepository {
        <<interface>>
        +findByTokenHash(tokenHash String) Optional~RefreshSession~
        +save(session RefreshSession) RefreshSession
        +revokeAllByUserId(userId Id)
    }
```

## External IdP Compatibility

```mermaid
flowchart LR
    subgraph now["Self-Hosted (Now)"]
        DB["roles table"]
        JWT1["JWT: issuer, subject, roles[]"]
        DB --> JWT1
    end

    subgraph later["External IdP (Later)"]
        KC["Keycloak realm roles / client roles / groups"]
        TOKEN["JWT claims or introspection response"]
        KC --> TOKEN
    end

    JWT1 --> Auth["AccessTokenAuthenticator"]
    TOKEN --> Auth
    Auth --> EP["ExternalPrincipal"]
    EP --> Resolver["PlatformIdentityResolver"]
    Resolver --> Link["Resolve (issuer, subject) to platformUserId"]
    Link --> Policy["Local roles to stable authorities"]
    Policy --> AU["AuthenticatedActor"]

    AU --> SC["SecurityContext"]
    SC --> Auth["hasAuthority('PRODUCT_MODERATE')"]
```

## Notes

- The User aggregate holds a `Set<Role>` for many-to-many role assignment.
- Role is an **Aggregate Root** because it has an independent lifecycle and
  owns its authority assignments. Users reference assigned role identities.
- Role codes are immutable strings, not enums, enabling runtime creation.
- Authorities are stable platform capabilities. Dynamic roles become useful by
  mapping them to authorities without changing endpoint code.
- Three seed roles (`ADMIN`, `SELLER`, `CUSTOMER`) are created at startup but
  are not hardcoded.
- `AccessTokenAuthenticator` normalizes local JWTs, OIDC JWTs, or OAuth2
  introspection responses into `ExternalPrincipal`.
- `AuthenticatedActor` carries normalized roles and authorities and never
  exposes provider-specific claims or SDK types to application code.
- External accounts link to stable platform users by `(issuer, subject)`;
  email is not used as the durable identity key.
- Email is a self-validating value object that enforces format constraints.
- HashedPassword wraps the BCrypt hash; the domain never stores plaintext. It
  is optional for users whose credentials exist only at an external provider.
- Local refresh tokens are stored only as hashes and rotated through
  `RefreshSession`; external providers own refresh sessions after migration.
- UserStatus controls authentication eligibility: only ACTIVE users can log in.
- Sellers register as PENDING_APPROVAL and require admin activation.
- Seller ownership checks use `AuthenticatedActor.platformUserId` in
  application handlers after request-level authority checks.
- Domain events are accumulated via `addEvent()` and published through the
  outbox pattern.
- The PasswordHasher interface lives in the domain; the BCrypt implementation
  lives in infrastructure.
