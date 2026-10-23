# Domain Specification: Pricing

> **Bounded Context:** Pricing
> **Primary Purpose:** Entity-agnostic pricing engine managing base prices, volume tiers, campaign overrides, and runtime price calculation contexts.
> **Module Root:** `pricing-domain` / `pricing-application` / `pricing-adapter-persistence` / `store/pricing`
> **Baseline Feature:** [DiscoveryProduct](../../../../features/feat_002-discovery_product.md)
> **Related Features:** [Vectorized Batch Regional Pricing](../../../../features/feat_005-vectorized-batch-regional-pricing.md), [Storefront API Cutover & Response Contract Refactoring](../../../../features/feat_007-storefront-api-cutover-contract-refactoring.md), [Automated Verification & Zero-Downtime Rollout](../../../../features/feat_008-automated-verification-zero-downtime-rollout.md)
> **Related Tech:** [Batch Pricing](../../../system/batch-regional-pricing-tech-spec.md), [Shopping Context](../../../system/shopping-context-resolution-tech-spec.md), [Discovery Projection](../../../system/storefront-query-tech-spec.md), [Rollout](../../../system/contextual-discovery-rollout-tech-spec.md)
> **Discovery Workflow:** [Browse and Discover](../../../workflows/browse-and-discover-products-workflow.md)
> **Related Domain Specs:** [Region](../../region/architecture/region-domain-spec.md)
> **Related Workflows:** [Create Sellable Product](../../../workflows/create-sellable-product-workflow.md)

---

## 1. Boundary & Context Map

### Ownership

- **This aggregate OWNS:** Base prices (`PriceSet`), promotional campaigns (`PriceList`), tax inclusivity (`PricePreference`), and the calculation policy synthesizing base vs. sale overrides.
- **This aggregate DOES NOT OWN:** Products, variants, shipping methods, or merchants. Pricing is completely entity-agnostic.

### Context Map

```mermaid
flowchart TD
    subgraph PricingContext ["Pricing Bounded Context"]
        direction TB

        subgraph PriceSetAgg ["PriceSet Aggregate"]
            PriceSet["PriceSet (Root)"]
            BasePrice["Price (Entity)"]
            PriceRule["PriceRule (Entity)"]

            PriceSet --> BasePrice
            BasePrice --> PriceRule
        end

        subgraph PriceListAgg ["PriceList Aggregate"]
            PriceList["PriceList (Root)"]
            CampaignPrice["Price (Entity)"]
            PriceListRule["PriceListRule (Entity)"]

            PriceList --> CampaignPrice
            PriceList --> PriceListRule
        end

        subgraph PrefAgg ["PricePreference Aggregate"]
            Preference["PricePreference (Root)"]
        end

        CampaignPrice -.->|"References by priceSetId"| PriceSet
    end

    subgraph CatalogContext ["Catalog Bounded Context"]
        Variant["ProductVariant"]
    end

    Variant -.->|"Paired via link table"| PriceSet
```

---

## 2. Ubiquitous Language

| Business Term | Domain Concept | Business Definition |
| :--- | :--- | :--- |
| **Price Set** | `PriceSet` (Aggregate Root) | An opaque container of candidate base prices for one priced thing (e.g., a variant). |
| **Base Price** | `Price` | The default, non-promotional amount owned by a `PriceSet` (`priceListId` is null). |
| **Campaign Price** | `Price` | A promotional amount owned by a `PriceList` that overrides or discounts a target `PriceSet`. |
| **Price List** | `PriceList` (Aggregate Root) | A promotional campaign grouping with a type (SALE or OVERRIDE), a time window, and target rules. |
| **Pricing Context** | `PricingContext` (VO) | The runtime parameters (currency, region, group, quantity) used to evaluate price rules and synthesize the final price. |
| **Calculated Price** | `CalculatePricesPolicy` Result | The final amount to charge the customer after all rules and campaigns are ranked and applied. |
| **Original Price** | `CalculatePricesPolicy` Result | The non-sale reference price used for strikethrough display (`~100~ 80`). |

---

## 3. Domain Aggregate Model

### Model Diagram

```mermaid
classDiagram
    direction LR

    class PriceSet {
        +Id id
        +create(id, now) PriceSet$
        +addPrice(price, now)
        +replacePrice(price, now)
        +removePrice(priceId, now)
    }

    class Price {
        +Id id
        +CurrencyCode currencyCode
        +MoneyAmount amount
        +Integer minQuantity
        +Integer maxQuantity
        +Id priceSetId
        +Id priceListId
    }

    class PriceRule {
        +String attribute
        +String value
        +PriceRuleOperator operator
    }

    class PriceList {
        +Id id
        +String title
        +PriceListStatus status
        +PriceListType type
        +Instant startsAt
        +Instant endsAt
        +isActiveAt(now) boolean
    }

    class PricePreference {
        +Id id
        +String attribute
        +String value
        +boolean taxInclusive
    }

    PriceSet *-- "0..*" Price : owns base prices
    PriceList *-- "0..*" Price : owns campaign prices
    Price *-- "0..*" PriceRule : contains
```

### Property & Attribute Rationale

| Property | Type | Belongs To | Business Rationale |
| :--- | :--- | :--- | :--- |
| `currencyCode` | `CurrencyCode` | Price | Prices are strictly currency-specific; no auto-conversion occurs in the domain. |
| `amount` | `MoneyAmount` | Price | The monetary value to charge or promote. |
| `minQuantity` | `Integer` | Price | Supports tiered pricing (e.g., "$10 each when buying 5+"). |
| `priceSetId` | `Id` | Price | Associates a campaign price to its target base price set. |
| `type` | `PriceListType` | PriceList | Dictates calculation strategy (`SALE` = min(sale, base), `OVERRIDE` = replace base). |
| `startsAt`/`endsAt` | `Instant` | PriceList | Defines the time window during which the campaign is active. |

---

## 4. Business Invariants & Rules

| Rule ID | Invariant Rule | Violation Outcome | Enforced By |
| :--- | :--- | :--- | :--- |
| **INV-01** | Price amount must be non-negative. | Operation rejected. | `MoneyAmount` |
| **INV-02** | Base prices must belong to a set and cannot specify a `priceListId`. | Domain error. | `Price.createBase()` |
| **INV-03** | Campaign prices must belong to a list and MUST reference a target `priceSetId`. | Domain error. | `Price.createCampaign()` |
| **INV-04** | For volume tiers, `minQuantity` must be ≤ `maxQuantity` if both are set. | Update rejected. | `Price.replaceDetails()` |
| **INV-05** | Only `ACTIVE` price lists whose `startsAt`/`endsAt` window contains the current time are evaluated. | Campaign ignored during calculation. | `CalculatePricesPolicy` |
| **INV-06** | Fully matching channel+region candidates MUST outrank region-only, then channel-only, then base candidates. | Less-specific tiers are excluded before price synthesis. | CalculatePricesPolicy.mostSpecificCandidates / marketSpecificity; implemented. |
| **INV-07** | Region identity MUST remain distinct from currency, even when two regions share USD. | A candidate with another region rule cannot match. | matchesPriceRules / matchesPriceListRules; implemented and covered by unit-test source. |
| **INV-08** | Currency, quantity, complete rule matching, list status and time eligibility MUST be evaluated before specificity ranking. | Ineligible candidates are ignored, not used as cross-market fallback. | CalculatePricesPolicy.calculate; implemented. |

The domain calculation supports multiple price sets, but the published variant quote interface remains single-variant. Feature 005 adds vectorized variant mapping without replacing the existing policy; domain amounts are not FX-converted.

---

## 5. State Lifecycle & Transitions

### State Machine (PriceList)

```mermaid
stateDiagram-v2
    [*] --> DRAFT : create()
    DRAFT --> ACTIVE : update(status=ACTIVE)
    ACTIVE --> DRAFT : update(status=DRAFT)
```

### Transition Matrix (PriceList)

| Current State | Trigger / Event | Next State | Guard Condition / Prerequisite |
| :--- | :--- | :--- | :--- |
| `DRAFT` | `update(status=ACTIVE)` | `ACTIVE` | Status is set through `replaceMetadata()`; the time window is checked at calculation time. |
| `ACTIVE` | `update(status=DRAFT)` | `DRAFT` | Status is set through `replaceMetadata()` and the list is excluded from calculations. |

---

## 6. Domain Events

### Emitted Events (What Happened)

| Event Name | Trigger | Key Attributes | Target Consumers |
| :--- | :--- | :--- | :--- |
| `PriceSetCreatedEvent` | Initial creation | `priceSetId` | Pricing Search / Downstream |
| `PriceSetUpdatedEvent` | Price set content changes | `priceSetId`, time | Pricing projections and downstream bridges |
| `PriceListCreatedEvent` | Price list created | `priceListId`, time | Pricing projections |
| `PriceListUpdatedEvent` | Price list metadata, rules, or prices change | `priceListId`, time | Pricing projections and downstream bridges |
| `PricePreferenceCreatedEvent` / `PricePreferenceUpdatedEvent` | Preference lifecycle | `pricePreferenceId`, time | Pricing projections |

### Consumed Events (Cross-Domain Signals)

*Note: Pricing does not directly listen to domain events for lifecycle management. Pricing links are managed by the application via synchronous commands (e.g., Workflow orchestrators creating Price Sets for new Variants).*

These are aggregate domain events written to the module outbox. Workflow acknowledgements such as `VariantPriceCreatedEvent` and `VariantPriceSyncedEvent` are separate store-level signals and are not emitted by the Pricing aggregates.

The store publisher maps `PriceSetCreatedEvent` and `PriceSetUpdatedEvent` to the separate `PriceSetChangedIntegrationEvent` record. Pricing workflow requests and acknowledgements remain a third event category.

Implementation references: `pricing-domain/src/main/java/com/pricing/domain/aggregate/PriceSet.java`, `PriceList.java`, and `PricePreference.java`.
