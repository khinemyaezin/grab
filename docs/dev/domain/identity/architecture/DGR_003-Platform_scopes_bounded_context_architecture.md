# Platform-Scoped Identity Domain Aggregates

This diagram reflects the extended identity domain model introducing platform-scoped and resource-scoped RBAC, as defined in ADR-003 and implemented in the codebase.

## Class Diagram

### Scoped Platform Access Aggregate

```mermaid
classDiagram
    direction TD

    namespace IdentityDomain {
        class User {
            <<AggregateRoot>>
            +Id id
            +Email email
            +UserStatus status
        }
        
        class Role {
            <<AggregateRoot>>
            +Id id
            +String code
            +String name
            +RoleKind kind
            +boolean active
            +boolean assignable
            +Set~Authority~ authorities
        }

        class RoleDelegationRule {
            <<AuthorizationRule>>
            +Id delegatorRoleId
            +Id delegatedRoleId
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
            +revoke()
            +suspend()
            +reactivate(requestedBy)
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
            +accept(userId, acceptorEmail, now)
            +cancel()
        }

        class AccessScope {
            <<ValueObject>>
            +ScopeKey key
            +String scopeId
            +isGlobal() boolean
            +encompasses(target, hierarchy) boolean
        }

        class ScopeKey {
            <<ValueObject>>
            +String value
            +isGlobal() boolean
        }

        class ScopeHierarchy {
            <<ValueObject>>
            +checkAncestorOrSelf(actorKey, targetKey) boolean
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

    User "1" ..> "*" AccessAssignment : "receives"
    Role "1" ..> "*" AccessAssignment : "assigned via roleCode"
    Role "1" ..> "*" AccessInvitation : "offered via roleCode"
    Role "1" --> "*" RoleDelegationRule : "delegator"
    Role "1" --> "*" RoleDelegationRule : "delegated"
    AccessAssignment --> AccessScope : "restricted by"
    AccessAssignment --> AccessAssignmentStatus : "state"
    AccessScope --> ScopeKey : "namespaced resource key"
    AccessScope ..> ScopeHierarchy : "hierarchical evaluation"
    AccessInvitation --> AccessScope : "restricted by"
    AccessInvitation --> InvitationStatus : "state"
```

---

## Entity Relationship

```mermaid
erDiagram
    USERS {
        bigserial id PK
        varchar uuid UK
        varchar email UK
        varchar status
    }

    ROLES {
        bigserial id PK
        varchar uuid UK
        varchar code UK
        varchar name
        varchar role_kind
        boolean active
        boolean assignable
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

    ROLE_DELEGATION_RULES {
        bigserial id PK
        bigint delegator_role_id FK
        bigint delegated_role_id FK
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

    REFRESH_SESSIONS {
        bigserial id PK
        bigint user_id FK
        varchar token_hash UK
        varchar token_family_id
        timestamp expires_at
        varchar assignment_uuid
        varchar scope_key
        varchar scope_id
    }

    USERS ||--o{ ACCESS_ASSIGNMENTS : "has"
    ROLES ||--o{ ACCESS_ASSIGNMENTS : "grants role"
    ROLES ||--o{ ACCESS_INVITATIONS : "offers role"
    ROLES ||--o{ ROLE_DELEGATION_RULES : "delegator"
    ROLES ||--o{ ROLE_DELEGATION_RULES : "delegated"
    USERS ||--o{ REFRESH_SESSIONS : "owns sessions"
```

---

## Domain Aggregate & Value Object Descriptions

### 1. `AccessScope` (Value Object)
Defines the boundary of an access grant. It consists of:
- A namespaced `ScopeKey` (for example, `merchant.account`, `merchant.storefront`, `inventory.fulfillment-location`, or `global`).
- A `scopeId` representing the specific business aggregate identifier (or `*` for global scope).

### 2. `ScopeHierarchy` (In-Memory Tree & Dynamic Catalog)
Maintains the graph of scope relationships (e.g., `merchant.account` is the parent of `merchant.storefront`).
When an actor operates with a parent scope, `AccessScope.encompasses(target)` allows managing descendants automatically. Cross-module verification of resource links is performed via `ScopeOwnershipPort`.

### 3. `AccessAssignment` (Aggregate Root)
Binds a `User` to a `Role` within an `AccessScope`.
Replaces static user-to-role mappings. A single user can have separate assignments:
- `CUSTOMER` role in `global` scope.
- `MERCHANT_OWNER` in scope `merchant.account` with ID `merchant-123`.
- `STORE_MANAGER` in scope `merchant.storefront` with ID `store-456`.

### 4. `AccessInvitation` (Aggregate Root)
Secures staff onboarding. Records an invitation for an email address to receive a role within a specific scope.
Stores a cryptographic `tokenHash` and expires after a defined TTL. Upon acceptance by the intended user, creates an `AccessAssignment`.

### 5. `RoleDelegationRule` (Authorization Rule)
Explicit active-role relationship declaring whether a delegator role can assign or invite another role. Evaluated by `RoleDelegationPolicy` during access granting and invitation creation.

---

## Flowcharts

### Context-Bound Token Issuance Flow

```mermaid
flowchart TD
    Login[User Authenticates with Email & Password] --> LoadAssignments[Load Active AccessAssignments for User]
    LoadAssignments --> GroupScopes[Group Assignments by Scope: scopeKey + scopeId]
    GroupScopes --> CheckMulti{Distinct Scopes Count?}
    
    CheckMulti -- 0 --> FreeToken0[Issue Context-Free Token Pair]
    CheckMulti -- 1 Scope --> AutoSelect[Auto-select Scope & Combine Effective Roles]
    CheckMulti -- >1 Scopes --> SelectionToken[Issue Context-Free Token Pair]
    
    SelectionToken --> PromptSelect[Client Calls GET /identity/access-contexts]
    PromptSelect --> ClientSelects[User Selects Target Merchant/Store Scope]
    ClientSelects --> PostSelect[POST /identity/access-contexts/{assignmentId}/select]
    PostSelect --> CombineRoles[Combine Effective Roles in Selected Scope]
    
    AutoSelect --> IssueBound[Issue Context-Bound Access & Refresh Token]
    CombineRoles --> IssueBound
    IssueBound --> SetCookies[Set HttpOnly Cookies & Return 200 OK]
```
