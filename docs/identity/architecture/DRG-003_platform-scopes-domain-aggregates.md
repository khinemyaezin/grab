# Platform-Scoped Identity Domain Aggregates

This diagram reflects the extended identity domain model introducing platform-scoped and resource-scoped RBAC, as defined in ADR-003.

## Class Diagram

### Platform Access Aggregate

```mermaid
classDiagram
    direction TD

    namespace IdentityDomain {
        class User {
            <<AggregateRoot>>
        }
        
        class Role {
            <<AggregateRoot>>
        }

        class RoleDelegationRule {
            <<AuthorizationRule>>
            +Id delegatorRoleId
            +Id delegatedRoleId
        }

        class Platform {
            <<AggregateRoot>>
            +String code
            +String name
            +boolean active
        }

        class PlatformRole {
            <<Entity>>
            +Id id
            +String platformCode
            +Id roleId
        }

        class AccessAssignment {
            <<AggregateRoot>>
            +Id id
            +Id userId
            +Id platformRoleId
            +AccessScope scope
            +AccessAssignmentStatus status
            +Id assignedBy
            +LocalDateTime createdAt
            +LocalDateTime expiresAt
            +revoke()
            +suspend()
            +isActive() boolean
        }

        class AccessInvitation {
            <<AggregateRoot>>
            +Id id
            +Email inviteeEmail
            +Id platformRoleId
            +AccessScope scope
            +Id invitedBy
            +InvitationStatus status
            +LocalDateTime expiresAt
            +accept(userId, acceptorEmail, now)
            +cancel()
        }

        class AccessScope {
            <<ValueObject>>
            +ScopeKey key
            +String scopeId
            +isGlobal() boolean
        }

        class ScopeKey {
            <<ValueObject>>
            +String value
            +isGlobal() boolean
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

    Platform "1" --> "*" PlatformRole : supports
    Role "1" --> "*" PlatformRole : mapped to
    Role "1" --> "*" RoleDelegationRule : delegates from
    Role "1" --> "*" RoleDelegationRule : delegates to
    User "1" --> "*" AccessAssignment : receives
    PlatformRole "1" --> "*" AccessAssignment : assigned via
    AccessAssignment --> AccessScope : restricted by
    AccessAssignment --> AccessAssignmentStatus : state
    AccessScope --> ScopeKey : identifies resource namespace
    AccessInvitation --> AccessScope : restricted by
    PlatformRole "1" --> "*" AccessInvitation : grants
    AccessInvitation --> InvitationStatus : state
```

## Entity Relationship

```mermaid
erDiagram
    PLATFORMS {
        varchar code PK
        varchar name
        boolean active
    }

    PLATFORM_ROLES {
        bigserial id PK
        varchar platform_code FK
        bigint role_id FK
    }

    ACCESS_ASSIGNMENTS {
        bigserial id PK
        bigint user_id FK
        bigint platform_role_id FK
        varchar scope_key
        varchar scope_id
        varchar status
        bigint assigned_by
        timestamp created_at
        timestamp expires_at
    }

    ACCESS_INVITATIONS {
        bigserial id PK
        varchar invitee_email
        bigint platform_role_id FK
        varchar scope_key
        varchar scope_id
        varchar status
        bigint invited_by
        timestamp created_at
        timestamp expires_at
    }

    ROLE_DELEGATION_RULES {
        bigserial id PK
        bigint delegator_role_id FK
        bigint delegated_role_id FK
    }

    PLATFORMS ||--o{ PLATFORM_ROLES : "supports"
    ROLES ||--o{ PLATFORM_ROLES : "defines"
    ROLES ||--o{ ROLE_DELEGATION_RULES : "delegator"
    ROLES ||--o{ ROLE_DELEGATION_RULES : "delegated role"
    USERS ||--o{ ACCESS_ASSIGNMENTS : "has"
    PLATFORM_ROLES ||--o{ ACCESS_ASSIGNMENTS : "assigns"
    PLATFORM_ROLES ||--o{ ACCESS_INVITATIONS : "offers"
```

## Domain Aggregate Descriptions

This section details the primary aggregates and value objects in the Platform-Scoped Identity model and explains their specific responsibilities.

### 1. `Platform`
Represents a distinct logical application boundary within the Grab ecosystem, such as `CUSTOMER_APP`, `SELLER_PORTAL`, or `ADMIN_CONSOLE`. It acts as a gatekeeper to ensure users only access the appropriate application interfaces.

### 2. `PlatformRole` (Mapping Entity)
A critical junction that defines which `Role`s are allowed to be used on which `Platform`. This prevents invalid configurations, such as assigning a `CUSTOMER` role to the `SELLER_PORTAL`.

### 3. `AccessScope` (Value Object)
Defines the specific business boundary that an assignment is limited to. It
consists of a namespaced key (for example `merchant.account`,
`merchant.storefront`, or `inventory.fulfillment-location`) and an ID. The
built-in `global` key uses `*` for its ID. Identity validates the reference
format while the owning bounded context defines the resource's meaning and
relationships.

### 4. `AccessAssignment`
The core aggregate replacing the traditional global "User-to-Role" mapping. It explicitly binds a `User` to a `PlatformRole` within a strict `AccessScope`. When a user logs in, the system loads only the assignments valid for the platform they are accessing.

### 5. `AccessInvitation`
Handles the workflow of onboarding staff. It records an offer for a specific email address to receive an `AccessAssignment` (Platform + Role + Scope). It ensures that pending invites have an expiration and are securely tracked until the user registers or logs in to accept them.

### 6. `RoleDelegationRule`
Defines one explicit active-role relationship that permits a delegator role to
grant or administer a delegated role. Missing relationships deny delegation;
there is no implicit administrator wildcard in domain code.


## Flowcharts

### Context-Bound Token Issuance Flow

This flowchart illustrates how a user's context is resolved into a scoped access token when they log in or switch contexts.

```mermaid
flowchart TD
    Login[User Authenticates] --> SelectPlatform[Select Platform<br/>e.g., SELLER_PORTAL]
    SelectPlatform --> LoadAssignments[Load Active AccessAssignments<br/>for User & Platform]
    LoadAssignments --> CheckMulti{Multiple<br/>Assignments?}
    CheckMulti -- No --> IssueToken[Issue Context-Bound<br/>Access Token]
    CheckMulti -- Yes --> ClientSelects[Client Prompts User<br/>to Select Context]
    ClientSelects --> VerifySelection[Verify Selection]
    VerifySelection --> IssueToken
```
