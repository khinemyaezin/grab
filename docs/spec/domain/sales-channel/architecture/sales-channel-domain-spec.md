# Domain Specification: Sales Channel

> **Bounded Context:** Sales Channel
> **Primary Purpose:** Defines a selling destination, its platform or merchant ownership, and whether it is enabled for use.
> **Module Root:** `sales-channel-domain` / `sales-channel-application` / `sales-channel-adapter-persistence` / `store/saleschannel`
> **Related Domain Specs:** [Merchant](../../merchant/architecture/merchant-domain-spec.md), [Catalog Product Publication](../../catalog/architecture/product-publication-domain-spec.md), [Publishable Key](publishable-key-domain-spec.md)
> **Foundation Features:** [Default Sales Channel](../../../../features/feat_001-default_saleschannel_provisioning.md), [DiscoveryProduct](../../../../features/feat_002-discovery_product.md), [Discovery Wiring & Projection Reconciliation](../../../../features/feat_006-discovery-wiring-projection-reconciliation.md)
> **Related Region:** [Region](../../region/architecture/region-domain-spec.md)
> **Projection Tech:** [Storefront Query](../../../system/storefront-query-tech-spec.md)
> **Regional Features:** [Edge Shopping-Context Resolution](../../../../features/feat_004-edge-shopping-context-resolution.md), [Storefront API Cutover & Response Contract Refactoring](../../../../features/feat_007-storefront-api-cutover-contract-refactoring.md), [Automated Verification & Zero-Downtime Rollout](../../../../features/feat_008-automated-verification-zero-downtime-rollout.md)
> **Regional Tech:** [Shopping Context](../../../system/shopping-context-resolution-tech-spec.md), [Rollout](../../../system/contextual-discovery-rollout-tech-spec.md)
> **Discovery Workflow:** [Browse and Discover](../../../workflows/browse-and-discover-products-workflow.md)
> **Related Feature Specs:** [Sales-Channel Key Lifecycle & Authenticated Management](../../../../features/feat_003-sales-channel-key-lifecycle-management.md)
> **Related Workflows:** [Default Sales Channel Provisioning](../../../workflows/default-sales-channel-provisioning-workflow.md), [Create Sellable Product](../../../workflows/create-sellable-product-workflow.md), [Update Sellable Product](../../../workflows/update-sellable-product-workflow.md)

---

## 1. Boundary & Context Map

### Ownership

- **This aggregate OWNS:** Channel identity, name, type, ownership classification, merchant reference, enabled/disabled status, lifecycle timestamps, and enabled/default Region references.
- **This aggregate DOES NOT OWN:** Merchant lifecycle, product data, variant/channel publication records, prices, inventory locations, routing, or storefront buyability projections.

The Sales Channel bounded context also owns publishable-key records. Each `PublishableKey` is a separate aggregate root and references its channel by `salesChannelId`; it is not a child of the `SalesChannel` aggregate. Detailed aggregate specification for publishable keys is defined in the [Publishable Key domain spec](publishable-key-domain-spec.md). A key record owns its label, token digest, display prefix, creation time, and revocation time. It does not own channel status or public product data.

A Sales Channel composes a set of `ChannelRegion` value records and references Region and Merchant only by their identifiers. The SSD calls a binding an entity; the current implementation models it as a value record with channel/region identity protected by the persistence pair key. Catalog owns [ProductPublication](../../catalog/architecture/product-publication-domain-spec.md), which references this aggregate by `salesChannelId`.

### Context Map

```mermaid
flowchart TD
    subgraph ChannelContext ["Sales Channel Bounded Context"]
        Channel["Sales Channel (Root)"]
    end
    subgraph MerchantContext ["Merchant Bounded Context"]
        Merchant["Merchant (Root)"]
    end
    subgraph CatalogContext ["Catalog Bounded Context"]
        Publication["Product Publication (Root)"]
    end
    Channel -.->|"merchantId for seller-owned channels"| Merchant
    Publication -.->|"salesChannelId reference only"| Channel
```

---

## 2. Ubiquitous Language

| Business Term | Domain Concept | Business Definition |
| :--- | :--- | :--- |
| **Sales Channel** | `SalesChannel` (Aggregate Root) | A destination through which products may be offered to customers. |
| **Website** | `ChannelType.WEBSITE` | A seller-owned channel associated with one merchant. |
| **Marketplace** | `ChannelType.MARKETPLACE` | A platform-owned channel without a merchant owner. |
| **Point of Sale** | `ChannelType.POS` | A seller-owned point-of-sale channel requiring a merchant reference. |
| **Seller Ownership** | `ChannelOwner.SELLER` | Ownership that restricts channel use to the referenced merchant. |
| **Platform Ownership** | `ChannelOwner.PLATFORM` | Ownership that permits any merchant to pass the channel ownership guard. |
| **Enabled** | `ChannelStatus.ENABLED` | Channel status that passes the enabled guard. |
| **Disabled** | `ChannelStatus.DISABLED` | Channel status that prevents a successful usability assertion. |
| **Usable Channel** | `CheckSalesChannelUsableResult.usable()` | A channel that exists, is enabled, and is platform-owned or belongs to the acting merchant. |
| **Publishable Key** | `PublishableKey` (Aggregate Root) | A revocable, channel-scoped credential used to select a sales channel for public storefront reads. |
| **Active Key** | `PublishableKey.isActive()` | A key whose `revokedAt` value is absent. |

---

## 3. Domain Aggregate Model

### Model Diagram

```mermaid
classDiagram
    class SalesChannel {
        +Identifier id
        +Name name
        +ChannelType type
        +ChannelOwner owner
        +Identifier merchantId
        +ChannelStatus status
        +Timestamp createdAt
        +Timestamp updatedAt
        +Revision version
        +RegionBindings supportedRegions
        +enableRegion(regionId, isDefault)
        +disableRegion(regionId)
        +servesRegion(regionId)
        +getDefaultRegion()
        +createWebsite(id, merchantId, name, now)
        +createMarketplace(id, name, now)
        +enable(now)
        +disable(now)
        +requireEnabled()
        +requireOwnedBy(actingMerchantId)
    }
```

The `supportedRegions` collection composes bindings, not Region aggregates.

Diagram attribute types describe business concepts; the implementation types are listed below. Type, owner, and merchant reference are fixed after construction, and no rename operation is currently exposed by the aggregate.

### Property & Attribute Rationale

| Property | Type | Belongs To | Business Rationale |
| :--- | :--- | :--- | :--- |
| `id` | `Id` | SalesChannel | Provides a stable reference for publications and channel lookups. |
| `name` | `String` | SalesChannel | Names the selling destination for merchants and operators. |
| `type` | `ChannelType` | SalesChannel | Distinguishes website, marketplace, and point-of-sale destinations. |
| `owner` | `ChannelOwner` | SalesChannel | Determines whether use is merchant-specific or platform-wide. |
| `merchantId` | `Id` (nullable) | SalesChannel | Identifies the seller owner and is absent for a platform marketplace. |
| `status` | `ChannelStatus` | SalesChannel | Controls whether the destination is enabled for use. |
| `createdAt` | `Instant` | SalesChannel | Records when the channel was established. |
| `updatedAt` | `Instant` | SalesChannel | Records the latest effective enable/disable transition. |
| `version` | `long` | SalesChannel | Supports detection of concurrent changes to the same channel. |
| `supportedRegions` | Set of ChannelRegion | SalesChannel | Defines the markets this selling destination serves and its optional default. |
| `salesChannelId` | Id | ChannelRegion | Identifies the channel owning the binding. |
| `regionId` | Id | ChannelRegion | References the Region authority without copying currency or country membership. |
| `enabled` | Boolean | ChannelRegion | Controls whether the channel serves that region. |
| `isDefault` | Boolean | ChannelRegion | Selects the market used when a shopper omits country. |

### Publishable Key Aggregate

`PublishableKey` is a separate aggregate root specified in detail in the [Publishable Key domain spec](publishable-key-domain-spec.md). It stores only a digest of the raw key and a short display prefix; the raw key is returned by the application only during successful creation. Its `salesChannelId` is an ID-only reference to a `SalesChannel` aggregate.

| Property | Type | Business Rationale |
| :--- | :--- | :--- |
| `id` | `Id` | Identifies the managed key record. |
| `salesChannelId` | `Id` | Limits the key to one sales channel. |
| `label` | `String` | Helps an operator recognize which storefront configuration uses the key. |
| `tokenDigest` | `String` | Supports lookup without persisting the raw key. |
| `displayPrefix` | `String` | Helps an operator identify a key without revealing its secret value. |
| `createdAt` | `Instant` | Records when the key was issued. |
| `revokedAt` | `Instant` (nullable) | Records revocation; absence means the key is active. |

---

## 4. Business Invariants & Rules

| Rule ID | Invariant Rule | Violation Outcome | Enforced By |
| :--- | :--- | :--- | :--- |
| **SC-01** | Channel name must be nonblank, and type, owner, status, and construction timestamps must be present. | Construction rejects invalid or missing values. | `SalesChannel` constructor / `requireName()` |
| **SC-02** | Website and POS channels must be seller-owned and identify a merchant. | Construction rejects invalid ownership with `MerchantRequired`; website factory rejects a null merchant. | `SalesChannel.validateOwnership()` / `createWebsite()` |
| **SC-03** | Marketplace channels must be platform-owned and have no merchant reference. | Construction rejects invalid ownership with `MarketplaceMustBePlatformOwned`. | `SalesChannel.validateOwnership()` / `createMarketplace()` |
| **SC-04** | A seller-owned channel may be used only by its owning merchant. | Ownership guard raises `ChannelOwnershipForbidden`. | `SalesChannel.requireOwnedBy()` |
| **SC-05** | A disabled channel must fail the enabled guard. | Guard raises `ChannelDisabled`. | `SalesChannel.requireEnabled()` |
| **SC-06** | Repeating enable or disable on the current status must not create another lifecycle event. | Operation returns without changing state or timestamps. | `SalesChannel.enable()` / `disable()` |

### Regional Binding Invariants and Completion Contract

| Rule ID | Invariant | Violation outcome | Enforcement / status |
| :--- | :--- | :--- | :--- |
| **SC-R01** | A channel MUST contain at most one binding per region and only its own channel references. | Reject a conflicting binding. | Constructor checks channel ownership; persistence pair key is unique, and aggregate duplicate-region validation remains required. |
| **SC-R02** | A channel MUST have at most one default region, and a disabled binding MUST NOT be default. | Invalid binding is rejected. | Constructor, ChannelRegion guard, and partial default index exist; database disabled-default check remains required. |
| **SC-R03** | Selecting a new default MUST clear the previous default atomically. | Concurrent conflicts retry or fail without two defaults. | enableRegion clears the previous flag in memory; persistence concurrency behavior requires verification. |
| **SC-R04** | Disabling a region MUST clear its default flag without selecting a replacement implicitly. | No default is returned until one is explicitly configured. | disableRegion and getDefaultRegion. |
| **SC-R05** | Operators MUST apply validated active-region bindings through an authenticated application command entry point. | Unknown/inactive regions or invalid configuration roll back the command. | ApplySalesChannelRegionBindingsService and handler exist; an operational entry point and completion verification remain required. |

Channels do not own a currency. An absent default is supported configuration state, but contextual requests without country fail as specified in the context contract.

### Publishable Key Invariants

| Rule ID | Invariant Rule | Violation Outcome | Enforced By |
| :--- | :--- | :--- | :--- |
| **PK-01** | A key references exactly one sales channel by ID and has a nonblank label of at most 100 characters. | Construction rejects invalid values. | `PublishableKey` constructor |
| **PK-02** | A key record contains a token digest and display prefix, never the raw token. | Invalid digest or prefix is rejected; raw token is not a domain property. | `PublishableKey` constructor / token generator boundary |
| **PK-03** | Token digests are unique across keys. | A digest collision fails creation so a new key can be generated. | `PublishableKeyRepository` and persistence uniqueness constraint |
| **PK-04** | A key is active exactly when `revokedAt` is absent; revocation time cannot precede creation time. | Invalid construction or revocation time is rejected. | `PublishableKey.isActive()` / `revoke()` |
| **PK-05** | Repeating revocation of an already revoked key does not change its timestamp or emit another event. | Operation is a no-op. | `PublishableKey.revoke()` |
| **PK-06** | A merchant may manage keys only for its own seller-owned channels; an administrator may manage keys for any channel. | The operation is forbidden when the caller does not own the seller channel and is not an administrator. | `PublishableKeyManagementPolicy.requireAllowed()` |

### Cross-Aggregate Uniqueness Guards

| Business Rule | Guard | Violation Outcome |
| :--- | :--- | :--- |
| One website channel per merchant. | `SalesChannelUniquenessService.requireWebsiteAvailable(merchantId)` | `DuplicateWebsite` when a website already exists, including a disabled one. |
| One marketplace channel across the platform. | `SalesChannelUniquenessService.requireMarketplaceAvailable()` | `DuplicateMarketplace` when a marketplace already exists, including a disabled one. |

These repository-backed guards are separate from the aggregate factories. Current production application services do not invoke them, so factory construction alone does not establish cross-channel uniqueness; no POS uniqueness guard is defined.

The workflow usability check reads a `SalesChannelView` through `SalesChannelQueryPort` and evaluates existence, enabled status, and ownership without loading or mutating the aggregate.

---

## 5. State Lifecycle & Transitions

### State Machine

```mermaid
stateDiagram-v2
    [*] --> ENABLED : create website or marketplace
    ENABLED --> DISABLED : disable
    DISABLED --> ENABLED : enable
    ENABLED --> ENABLED : repeat enable is a no-op
    DISABLED --> DISABLED : repeat disable is a no-op
```

### Transition Matrix

| Current State | Trigger / Event | Next State | Guard Condition / Prerequisite |
| :--- | :--- | :--- | :--- |
| New | `createWebsite()` | `ENABLED` | Nonblank name, merchant reference, and creation time are supplied. |
| New | `createMarketplace()` | `ENABLED` | Nonblank name and creation time are supplied; ownership is platform-wide. |
| `ENABLED` | `disable(now)` | `DISABLED` | Updates `updatedAt` and emits `SalesChannelDisabledEvent`. |
| `DISABLED` | `enable(now)` | `ENABLED` | Updates `updatedAt` and emits `SalesChannelEnabledEvent`. |
| `ENABLED` | `enable(now)` | `ENABLED` | No state change, timestamp change, or event. |
| `DISABLED` | `disable(now)` | `DISABLED` | No state change, timestamp change, or event. |

The constructor can reconstitute either supported status without emitting events. POS is a supported type with constructor ownership validation, but has no dedicated creation factory; no draft, archive, or deletion lifecycle is modeled.

Disabling a channel does not delete Catalog publication records. Its integration event causes Storefront Query to update channel eligibility in the buyable-offer projection.

Publishable keys have a separate lifecycle and are not enabled or disabled with their channel. A key remains active until revoked; Storefront Query still fails closed when the associated sales channel is disabled.

### Regional Binding State

| Current state | Trigger | Next state | Guard |
| :--- | :--- | :--- | :--- |
| Absent or disabled | enableRegion | Enabled, optionally default | Referenced region must be active at the application boundary. |
| Enabled | enableRegion as default | Enabled default | Clear every previous default within the same channel transaction. |
| Enabled/default | disableRegion | Disabled and nondefault | No implicit replacement default. |

Region changes currently emit no aggregate domain events. Context resolution must read current bindings; cache-based propagation is not assumed.

### Publishable Key State

| Current State | Trigger | Next State | Guard Condition / Prerequisite |
| :--- | :--- | :--- | :--- |
| New | `PublishableKey.create()` | Active | Channel ID, label, digest, prefix, and creation time are valid. |
| Active | `revoke(now)` | Revoked | Revocation time is not before creation time. |
| Revoked | `revoke(now)` | Revoked | No state, timestamp, or event change. |

---

## 6. Domain Events

### Emitted Events (What Happened)

| Event Name | Trigger | Key Attributes | Target Consumers |
| :--- | :--- | :--- | :--- |
| `SalesChannelCreatedEvent` | Website or marketplace factory creates the aggregate. | Channel ID, name, type, owner, merchant ID, status, occurrence time | No current creation integration bridge or listener. |
| `SalesChannelEnabledEvent` | Effective transition from disabled to enabled. | Channel ID, type, merchant ID, occurrence time | Sales Channel integration bridge |
| `SalesChannelDisabledEvent` | Effective transition from enabled to disabled. | Channel ID, type, merchant ID, occurrence time | Sales Channel integration bridge |
| `PublishableKeyCreatedEvent` | A key aggregate is created. | Key ID, channel ID, occurrence time | No current cross-module integration consumer. |
| `PublishableKeyRevokedEvent` | An active key is effectively revoked. | Key ID, channel ID, occurrence time | No current cross-module integration consumer. |

`SalesChannelRepositoryAdapter.save()` persists the aggregate and passes its pulled events to `DomainEventProducer`. Durable dispatch follows the [Transactional Outbox specification](../../../system/transactional-outbox-tech-spec.md).

### Store Integration Events

| Domain Event | Integration Record | Consumer / Business Impact |
| :--- | :--- | :--- |
| `SalesChannelEnabledEvent` | `SalesChannelEnabledIntegrationEvent` | Storefront Query refreshes channel eligibility for affected offers. |
| `SalesChannelDisabledEvent` | `SalesChannelDisabledIntegrationEvent` | Storefront Query marks affected offers as ineligible through the disabled channel. |

`SalesChannelIntegrationEventPublisher` adds an event version and preserves channel ID, type, merchant ID, and occurrence time. Creation has no corresponding integration record in the current bridge.

R17 requires cross-module integration records under `store/src/main/java/com/grab/store/shared/events/saleschannel/`; the lifecycle records currently reside under `store/.../saleschannel/events/`, an existing implementation deviation. Security manifest publication is a separate module capability and does not represent SalesChannel aggregate creation or status changes.

### Workflow Signals

| Consumed Request | Read-Only Action | Emitted Signal |
| :--- | :--- | :--- |
| `RequestAssertChannelEvent` | Check that the channel exists, is enabled, and is usable by the merchant. | `ChannelAssertedEvent` on success. |
| `RequestAssertChannelEvent` | Usability assertion fails or its execution raises an exception. | `PublishProductStepFailedEvent` with step `assert-channel`. |

The Sales Channel workflow listener consumes requests via `@EventListener` and dispatches `CheckSalesChannelUsableQuery` through `QueryBus`. These workflow records carry workflow correlation and remain separate from aggregate domain events; they do not enable, disable, or publish products to the channel.

### Consumed Events (Cross-Domain Signals)

The SalesChannel aggregate consumes no cross-domain events directly. Workflow requests above are handled at the store boundary, and shared-interface consumers use `SalesChannelQuery`, whose provider adapter delegates to an inbound read use case under `@SalesChannelReadTransactional`.
