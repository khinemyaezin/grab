# Identity Domain Aggregate Diagram

This diagram reflects the identity domain model for authentication, authorization,
scoped access delegation, and distributed security manifest synchronization.

## Class Diagram

### Core Identity and Access Aggregates

```mermaid
classDiagram
    direction TD

    namespace IdentityDomain {
        class User {
            <<AggregateRoot>>
            +Id id
            +Email email
            +Optional~HashedPassword~ passwordHash
            +UserStatus status
            +LocalDateTime createdAt
            +LocalDateTime updatedAt
            +createLocal(id, email, password) User
            +activate()
            +suspend()
            +reactivate()
            +isActive() boolean
            +getPasswordHash() Optional~HashedPassword~
        }

        class Role {
            <<AggregateRoot>>
            +Id id
            +String code
            +String name
            +String description
            +RoleKind kind
            +boolean active
            +boolean assignable
            +Set~Authority~ authorities
            +createCustom(id, code, name, description, authorities) Role
            +rehydrate(id, code, name, description, kind, active, assignable, authorities) Role
            +updateDetails(name, description)
            +activate()
            +deactivate()
            +assignAuthority(authority)
            +revokeAuthority(authority)
            +requireAssignable()
            +getAuthorities() Set~Authority~
        }

        class Authority {
            <<Entity>>
            +Id id
            +String code
            +String category
            +String name
            +String description
            +boolean active
            +create(id, code, category, name, description) Authority
            +from(id, moduleKey, definition) Authority
        }

        class AccessAssignment {
            <<AggregateRoot>>
            +Id id
            +Id userId
            +String roleCode
            +AccessScope scope
            +AccessAssignmentStatus status
            +Id assignedBy
            +Instant createdAt
            +Instant updatedAt
            +Instant expiresAt
            +create(id, userId, roleCode, scope, assignedBy, expiresAt) AccessAssignment
            +suspend()
            +reactivate(requestedBy)
            +revoke()
            +changeStatus(requestedStatus, requestedBy)
            +expireIfDue(instant) boolean
            +isEffectiveAt(instant) boolean
            +statusAt(instant) AccessAssignmentStatus
        }

        class AccessInvitation {
            <<AggregateRoot>>
            +Id id
            +Email inviteeEmail
            +String roleCode
            +AccessScope scope
            +String tokenHash
            +Id invitedBy
            +InvitationStatus status
            +Instant createdAt
            +Instant expiresAt
            +Id acceptedBy
            +Instant updatedAt
            +create(id, inviteeEmail, roleCode, scope, tokenHash, invitedBy, inviterEmail, expiresAt) AccessInvitation
            +accept(userId, acceptorEmail, now)
            +cancel()
        }

        class RoleDelegationRule {
            <<AuthorizationRule>>
            +Id delegatorRoleId
            +Id delegatedRoleId
        }

        class AccessScope {
            <<ValueObject>>
            +ScopeKey key
            +String scopeId
            +isGlobal() boolean
            +encompasses(target, hierarchy) boolean
            +requireEncompasses(target, hierarchy)
        }

        class ScopeKey {
            <<ValueObject>>
            +String value
            +isGlobal() boolean
        }

        class ScopeHierarchy {
            <<ValueObject>>
            +Map~String, String~ parentOf
            +Map~String, String~ ownerOf
            +checkAncestorOrSelf(actorKey, targetKey) boolean
            +register(declaration)
            +registerAll(declarations)
            +replaceModule(moduleKey, declarations)
        }

        class Email {
            <<ValueObject>>
            +String value
        }

        class HashedPassword {
            <<ValueObject>>
            +String hash
        }

        class UserStatus {
            <<enumeration>>
            ACTIVE
            PENDING_APPROVAL
            SUSPENDED
        }

        class RoleKind {
            <<enumeration>>
            SYSTEM
            CUSTOM
        }

        class AccessAssignmentStatus {
            <<enumeration>>
            ACTIVE
            SUSPENDED
            REVOKED
            EXPIRED
        }

        class InvitationStatus {
            <<enumeration>>
            PENDING
            ACCEPTED
            CANCELLED
            EXPIRED
        }
    }

    User --> Email : "identity"
    User --> HashedPassword : "local credential"
    User --> UserStatus : "lifecycle"
    User "1" ..> "*" AccessAssignment : "receives access"
    AccessAssignment --> AccessScope : "scoped to"
    AccessAssignment --> AccessAssignmentStatus : "state"
    AccessScope --> ScopeKey : "namespace"
    AccessInvitation --> AccessScope : "scoped to"
    AccessInvitation --> InvitationStatus : "state"
    Role "1" --> "*" Authority : "authorities granted"
    Role --> RoleKind : "system vs custom"
    Role "1" ..> "*" AccessAssignment : "assigned via roleCode"
    Role "1" ..> "*" AccessInvitation : "offered via roleCode"
    Role "1" --> "*" RoleDelegationRule : "delegator"
    Role "1" --> "*" RoleDelegationRule : "delegated"
```

---

### Security Manifest and Dynamic Catalog Model

```mermaid
classDiagram
    direction LR

    namespace SecurityManifestModel {
        class SecurityManifest {
            <<ValueObject>>
            +int schemaVersion
            +String moduleKey
            +int securityRevision
            +List~ScopeDeclaration~ scopes
            +List~AuthorityDefinition~ authorities
            +List~SecurityDependency~ dependencies
            +contentDigest() String
        }

        class ScopeDeclaration {
            <<ValueObject>>
            +String scopeKey
            +String parentScopeKey
            +Lifecycle lifecycle
        }

        class AuthorityDefinition {
            <<ValueObject>>
            +String code
            +String name
            +String description
            +String category
            +Lifecycle lifecycle
        }

        class SecurityDependency {
            <<ValueObject>>
            +String scopeKey
            +int minimumRevision
        }

        class SecurityManifestCandidateStatus {
            <<enumeration>>
            APPLIED
            SUPERSEDED
            WAITING_DEPENDENCY
            QUARANTINED
        }
    }

    SecurityManifest "1" *-- "*" ScopeDeclaration
    SecurityManifest "1" *-- "*" AuthorityDefinition
    SecurityManifest "1" *-- "*" SecurityDependency
```

---

## Entity Relationship

```mermaid
erDiagram
    USERS {
        bigserial id PK
        varchar uuid UK
        varchar email UK
        varchar password_hash "nullable"
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
        varchar role_kind
        boolean active
        boolean assignable
    }

    AUTHORITIES {
        bigserial id PK
        varchar uuid UK
        varchar code UK
        varchar category
        varchar name
        varchar description
        boolean active
    }

    ROLE_AUTHORITIES {
        bigint role_id FK
        bigint authority_id FK
    }

    ROLE_DELEGATION_RULES {
        bigserial id PK
        bigint delegator_role_id FK
        bigint delegated_role_id FK
    }

    ACCESS_ASSIGNMENTS {
        bigserial id PK
        varchar uuid UK
        bigint user_id FK
        bigint role_id FK
        varchar scope_key
        varchar scope_id
        varchar status
        varchar assigned_by
        timestamp created_at
        timestamp updated_at
        timestamp expires_at
    }

    ACCESS_INVITATIONS {
        bigserial id PK
        varchar uuid UK
        varchar invitee_email
        bigint role_id FK
        varchar scope_key
        varchar scope_id
        varchar token_hash UK
        varchar invited_by
        varchar status
        timestamp created_at
        timestamp updated_at
        timestamp expires_at
        varchar accepted_by
    }

    REFRESH_SESSIONS {
        bigserial id PK
        bigint user_id FK
        varchar token_hash UK
        varchar token_family_id
        timestamp expires_at
        timestamp revoked_at
        bigint replaced_by_id FK
        timestamp created_at
        timestamp last_used_at
        varchar assignment_uuid
        varchar scope_key
        varchar scope_id
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

    SECURITY_SCOPE_DEFINITIONS {
        bigserial id PK
        varchar module_key
        varchar scope_key UK
        varchar parent_scope_key
        int manifest_version
        boolean active
        bigint row_version
    }

    SECURITY_MANIFEST_MODULE {
        varchar module_key PK
        int applied_revision
        varchar applied_digest
        bigint row_version
    }

    SECURITY_MANIFEST_INBOX {
        varchar event_id PK
        varchar module_key
        int revision
        varchar content_digest
        timestamp processed_at
        varchar status
        varchar error_code
    }

    SECURITY_CATALOG_STATE {
        bigint id PK
        bigint catalog_revision
        bigint row_version
    }

    USERS ||--o{ ACCESS_ASSIGNMENTS : "has"
    ROLES ||--o{ ACCESS_ASSIGNMENTS : "grants role"
    ROLES ||--o{ ROLE_AUTHORITIES : "has"
    AUTHORITIES ||--o{ ROLE_AUTHORITIES : "mapped to"
    ROLES ||--o{ ROLE_DELEGATION_RULES : "delegator"
    ROLES ||--o{ ROLE_DELEGATION_RULES : "delegated"
    ROLES ||--o{ ACCESS_INVITATIONS : "offered role"
    USERS ||--o{ REFRESH_SESSIONS : "owns sessions"
    USERS ||--o{ EXTERNAL_IDENTITIES : "linked to"
    ROLES ||--o{ EXTERNAL_ENTITLEMENT_MAPPINGS : "mapped from provider"
```

---

## User Status Lifecycle

```mermaid
stateDiagram-v2
    [*] --> ACTIVE : register (customer / direct)
    [*] --> PENDING_APPROVAL : applicant registration

    PENDING_APPROVAL --> ACTIVE : admin approve
    PENDING_APPROVAL --> SUSPENDED : admin reject

    ACTIVE --> SUSPENDED : admin suspend
    SUSPENDED --> ACTIVE : admin reactivate
```

---

## Domain Events

```mermaid
classDiagram
    direction LR

    class UserRegisteredEvent {
        <<DomainEvent>>
        +Id userId
        +String email
        +UserStatus status
        +LocalDateTime occurredAt
    }

    class UserStatusChangedEvent {
        <<DomainEvent>>
        +Id userId
        +UserStatus previousStatus
        +UserStatus newStatus
        +LocalDateTime occurredAt
    }

    class RoleCreatedEvent {
        <<DomainEvent>>
        +Id roleId
        +String code
        +String name
        +String description
        +boolean active
        +Set~String~ authorityCodes
        +LocalDateTime occurredAt
    }

    class RoleStatusChangedEvent {
        <<DomainEvent>>
        +Id roleId
        +boolean active
        +LocalDateTime occurredAt
    }

    class RoleDetailsUpdatedEvent {
        <<DomainEvent>>
        +Id roleId
        +String name
        +String description
        +LocalDateTime occurredAt
    }

    class RoleAuthorityChangedEvent {
        <<DomainEvent>>
        +Id roleId
        +String authorityCode
        +boolean assigned
        +LocalDateTime occurredAt
    }

    class AccessAssignmentChangedEvent {
        <<DomainEvent>>
        +Id assignmentId
        +Id userId
        +String roleCode
        +String scopeKey
        +String scopeId
        +AccessAssignmentStatus previousStatus
        +AccessAssignmentStatus newStatus
        +Instant occurredAt
    }

    class AccessInvitationChangedEvent {
        <<DomainEvent>>
        +Id invitationId
        +String inviteeEmail
        +String roleCode
        +String scopeKey
        +String scopeId
        +InvitationStatus previousStatus
        +InvitationStatus newStatus
        +Instant occurredAt
    }
```

---

## Repository Ports (Domain Layer)

```mermaid
classDiagram
    direction LR

    class UserRepository {
        <<interface>>
        +findById(id Id) Optional~User~
        +findByEmail(email Email) Optional~User~
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
        +findCodesByModule(moduleKey String) Set~String~
        +findCodesOwnedByOtherModules(moduleKey String) Set~String~
        +upsertAll(authorities List~Authority~)
        +retireCodes(moduleKey String, codes Set~String~)
    }

    class AccessAssignmentRepository {
        <<interface>>
        +findById(id Id) Optional~AccessAssignment~
        +findEffectiveByUser(userId Id, instant Instant) List~AccessAssignment~
        +findByUserAndScope(userId Id, scope AccessScope) List~AccessAssignment~
        +existsCurrent(userId Id, roleCode String, scope AccessScope) boolean
        +save(assignment AccessAssignment) AccessAssignment
    }

    class AccessInvitationRepository {
        <<interface>>
        +findById(id Id) Optional~AccessInvitation~
        +findByTokenHash(tokenHash String) Optional~AccessInvitation~
        +findPendingByEmail(email Email) List~AccessInvitation~
        +save(invitation AccessInvitation) AccessInvitation
    }

    class RoleDelegationRuleRepository {
        <<interface>>
        +existsActiveRule(actorRoleCodes Set~String~, requestedRoleCode String) boolean
        +findDelegableRoleCodes(actorRoleCodes Set~String~) Set~String~
    }

    class ScopeManifestRepository {
        <<interface>>
        +apply(moduleKey String, manifestVersion int, scopes List~ScopeDeclaration~) boolean
        +loadGraph() Map~String, String~
        +loadOwners() Map~String, String~
        +loadActive() List~ScopeDeclaration~
    }

    class SecurityManifestInboxRepository {
        <<interface>>
        +find(eventId String) Optional~SecurityManifestReceipt~
        +alreadyProcessed(eventId String) boolean
        +findByModuleRevision(moduleKey String, revision int) Optional~SecurityManifestReceipt~
        +appliedRevision(moduleKey String) int
        +recordOutcome(eventId String, moduleKey String, revision int, digest String, status SecurityManifestCandidateStatus, errorCode String)
    }

    class SecurityCatalogLock {
        <<interface>>
        +lockNowait()
        +recordActivation()
    }
```

---

## Core Architectural Rules & Invariants

1. **User Aggregate Decoupling**: The `User` aggregate no longer holds direct references or collections of `Role`. All user access is expressed through independent `AccessAssignment` aggregates.
2. **Platform & Scoped Access Model**: Applications (`CUSTOMER_APP`, `SELLER_PORTAL`, `ADMIN_CONSOLE`) and scopes (`merchant.account`, `merchant.storefront`, `inventory.fulfillment-location`) are governed by namespaced `ScopeKey` values rather than an isolated `PLATFORM_ROLES` table.
3. **Role Kinds & System Immutability**: Roles are categorized by `RoleKind` (`SYSTEM` vs `CUSTOM`). `SYSTEM` roles (such as seeded system roles) cannot be updated, activated/deactivated, or have their authorities modified through runtime APIs.
4. **Hierarchical Scope Encompassing**: An actor possessing access in an ancestor scope (e.g., `merchant.account`) can administer or encompass child scopes (e.g., `merchant.storefront`) via `AccessScope.encompasses()` evaluated against `ScopeHierarchy`. Cross-scope ownership verification is delegated to `ScopeOwnershipPort`.
5. **Decentralized Security Manifest Synchronization**: Bounded contexts autonomously declare their scopes, authorities, and version dependencies at startup. The Identity module validates SHA-256 digests, ensures backward compatibility, rejects scope cycles, and maintains the authoritative security catalog.
