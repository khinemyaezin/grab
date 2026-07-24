# Product Requirements Document: Pricing Module

## 1. Summary

This PRD defines the Pricing module for the commerce platform.

Pricing is the source of truth for monetary amounts and contextual price
resolution. It stores entity-agnostic price sets, optional campaign price
lists, and tax-inclusivity preferences, and it exposes `calculatePrices` so
callers receive the amount to charge and a reference original amount for a
runtime context.

When a Catalog product/variant is created, Pricing does not receive the
variant automatically. Operators (or the seller app) create a `PriceSet`, add
base prices, and pair `variantId → priceSetId`. Storefront and checkout then
calculate using that `priceSetId` and context.

## 2. Problem

The platform must show and charge the correct price under different
currencies, regions, customer segments, quantities, and campaigns.

Without a dedicated Pricing module:

- catalog or inventory tables would accumulate price columns and campaign logic;
- volume tiers and regional overrides would be implemented inconsistently;
- storefront strikethrough (original vs calculated) would be ambiguous;
- non-product priced entities could not reuse the same engine;
- SALE versus OVERRIDE behavior would be reinvented in each consumer.

Catalog already treats pricing as outside its boundary. This module fills that
gap.

## 3. Users

- Merchant / seller operator
- Merchandising / campaign manager
- Platform admin
- Storefront / checkout systems
- Catalog or seller tooling that pairs variants to price sets

## 4. Goals

- Provide one authoritative place to manage and calculate prices.
- Keep pricing entity-agnostic so the same model can later price variants,
  shipping options, or other resources.
- Support a clear product→price pairing workflow without storing amounts on
  Catalog.
- Support base prices, quantity tiers, contextual rules, and campaign lists.
- Make SALE vs OVERRIDE behavior explicit and testable.
- Expose HATEOAS-discoverable APIs consistent with other Grab modules.

## 5. In Scope

- Create, get, and delete entity-agnostic price sets.
- Add, update, and remove base prices (currency, amount, quantity bands, rules).
- Create, update, get, list, and delete campaign price lists.
- Replace price-list rules; add or remove campaign prices that reference a
  price set.
- Create, update, get, list, and delete tax-inclusivity preferences.
- Calculate prices for one or more price sets given context.
- Document and support the application pairing flow:
  create Catalog variant → create PriceSet → store `variantId → priceSetId` →
  calculate by `priceSetId`.
- Tier-2 root, Tier-1 `get-pricing-root`, module flag, dedicated DB, outbox,
  RFC 7807 errors.

## 6. Out of Scope

- Durable remote link table or Catalog `priceSetId` column in Phase 1 — owned
  later by Catalog or Pricing link design (see ADR).
- Product/variant content — owned by Catalog.
- Stock — owned by Inventory.
- Merchant onboarding — owned by Merchant.
- Authn/authz — owned by Identity.
- Cart, order, payment, FX conversion, coupons, gift cards, full tax engines.
- Soft-delete / `deleted_at`.
- Publishing `pricing::api` / `pricing::events` until a concrete consumer needs
  them.

## 7. Ubiquitous Language

> Shared vocabulary. Business term must match how the ADR and code name the concept.

| Business term | Meaning for users / ops | Domain type or value | Notes |
|---------------|-------------------------|----------------------|-------|
| Price set | Bucket of prices for one priced thing | `PriceSet` | Opaque id; not a product |
| Base price | Default amount on a price set | `Price` (`priceListId` null) | Owned by price set |
| Campaign price | Promotional amount on a campaign | `Price` (with `priceListId`) | References `priceSetId` |
| Price rule | “Only if region/group/…” on one price | `PriceRule` | All rules must match |
| Price list | Named campaign with window and type | `PriceList` | `DRAFT` / `ACTIVE` |
| SALE | Discount campaign | `PriceListType.SALE` | `min(sale, base)` |
| OVERRIDE | Force campaign amount | `PriceListType.OVERRIDE` | Replaces base |
| Preference | Tax inclusive for region/currency | `PricePreference` | Affects flags on calculate |
| Pricing context | Currency, qty, attributes at calculate time | `PricingContext` | Currency required |
| Calculated price | Amount to charge | policy result | After ranking/synthesis |
| Original price | Strikethrough reference | policy result | Non-sale override or base |
| Pairing | Link variant to price set | app / future link | Not inside PriceSet |

## 8. Domain Concepts (Product View)

> Short narrative for readers who need the model without opening the ADR. No class diagrams here.

**Main concepts:**
- **Price set** — the priced identity other modules pair to; holds base prices.
- **Price** — a currency amount with optional quantity band and rules; base or
  campaign.
- **Price list** — a campaign that can attach promotional prices to existing
  price sets.
- **Preference** — whether amounts are tax-inclusive for a region or currency.
- **Calculation** — given price set ids and context, returns calculated and
  original amounts.

**Lifecycle (business terms):**
- Price set: created empty → priced (base prices added) → deleted.
- Price list: Draft → Active (and back) → deleted. Only Active + in-window
  lists affect calculation.
- Preference: created → updated → deleted.

**Boundaries (product language):**
- This module **is** the source of truth for: amounts, campaigns, preferences,
  and contextual calculation.
- This module **is not** responsible for: product content (Catalog), stock
  (Inventory), or who may sell (Merchant/Identity). Pairing a variant to a
  price set is an application or future-link concern in Phase 1.

Technical aggregate design, attributes, invariants, and CQRS flow: see Related
Documents → ADR.

## 9. Main Features

### 9.1 Pair a Catalog product with pricing

After a product/variant exists in Catalog, operators create pricing and link
the ids.

It must:
- allow creating a price set independently of Catalog;
- allow adding base prices to that set;
- allow the seller app (or later Catalog/link) to remember
  `variantId → priceSetId`;
- allow storefront/checkout to resolve price by looking up `priceSetId` then
  calling calculate — never by sending `variantId` into Pricing in Phase 1.

### 9.2 Manage base prices

Operators maintain default amounts, tiers, and rules on a price set.

It must:
- create an empty price set and return a Location;
- require currency and a non-negative amount;
- reject invalid quantity ranges;
- support per-price rules (`EQ`, `GT`, `GTE`, `LT`, `LTE`);
- support get, update, remove price, and delete price set.

### 9.3 Manage campaign price lists

Campaign managers run SALE or OVERRIDE promotions against existing price sets.

It must:
- create lists in Draft so they do not affect live prices until Active;
- support type, status, and optional start/end dates;
- replace list-level audience rules;
- require campaign prices to reference an existing price set;
- ignore Draft or out-of-window lists during calculation.

### 9.4 Calculate prices

Clients resolve charge and strikethrough amounts for many price sets at once.

It must:
- require `currencyCode`;
- treat omitted quantity as effective quantity `1`;
- apply filtering, ranking, SALE/OVERRIDE synthesis, and preference tax flags;
- return calculated and original amounts plus selected price metadata per set.

### 9.5 Manage preferences

Admins configure tax-inclusivity by region or currency.

It must:
- support create, update, get, list, and delete;
- prefer `region_id` matches over `currency_code` when calculating flags.

## 10. Business Rules

> Every rule that the product insists on. Tie each rule to domain enforcement.

| ID | Business statement | Domain enforcement | User-visible effect |
|----|--------------------|--------------------|---------------------|
| BR1 | Pricing does not store product or variant ids inside a price set | Aggregate boundary / API | Pairing happens outside Pricing in Phase 1 |
| BR2 | Currency is required to calculate | `CalculatePricesQueryHandler` / `CurrencyRequired` | Client error if currency missing |
| BR3 | Amounts cannot be negative | `MoneyAmount` | Validation / domain error |
| BR4 | Min quantity cannot exceed max quantity | `Price.replaceDetails` | Domain error on save |
| BR5 | Draft campaigns never change live prices | `CalculatePricesPolicy` + `PriceListStatus` | Draft list ignored on calculate |
| BR6 | Only in-window Active lists apply | `CalculatePricesPolicy` / `isActiveAt` | Expired/future campaigns ignored |
| BR7 | All price rules must match for a price to apply | `CalculatePricesPolicy` / `matchesRules` | Partial match excluded |
| BR8 | SALE uses the lower of sale and base | `CalculatePricesPolicy.resolveCalculated` | Calculated amount ≤ base when both exist |
| BR9 | OVERRIDE replaces base even if higher | `CalculatePricesPolicy.resolveCalculated` | Calculated equals override amount |
| BR10 | Original price is non-sale override or else base | `CalculatePricesPolicy.resolveOriginal` | Strikethrough shows list/base correctly |
| BR11 | Ranking prefers list prices, then more specific rules, then lower amount | `CalculatePricesPolicy.rank` | Cheaper equal-specificity list price wins |
| BR12 | Campaign prices must reference an existing price set | `AddPriceToPriceListCommandHandler` | Not-found if price set missing |

## 11. Success Criteria

The module or feature is successful when:
- a seller can create a Catalog variant, create a price set, pair the ids, and
  see the correct calculated price on storefront;
- campaigns can be drafted and activated without mutating base prices;
- Catalog remains free of price amount columns as system of truth;
- SALE, OVERRIDE, tiers, context rules, and date windows behave as specified
  and are covered by domain tests;
- Pricing can be disabled with `pricing.enabled=false` without breaking other
  modules.

## 12. Technical Considerations & Constraints

> Constraints only — not architecture design. Link the ADR for structure.

- Must expose RFC 7807 error responses with pricing-prefixed codes.
- Feature flag `pricing.enabled` for additive rollout.
- Dedicated pricing datasource and transaction manager.
- Context attribute keys use snake_case (`region_id`, `customer_group_id`).
- Phase 1 calculate accepts `priceSetIds` only, not `variantId`.
- Architecture, aggregates, and APIs: see Related Documents → ADR.

## 13. Dependencies

- Framework CQRS, Id generation, and exception types — dispatch and identity.
- Shared security — authenticated API access.
- Module-scoped outbox — domain event publication.
- PostgreSQL pricing database — isolated persistence.
- Catalog (workflow only) — creates variants that the app pairs to price sets;
  no runtime dependency inside Pricing Phase 1.
- Future checkout — consumer of `calculatePrices`.

## 14. Related Documents

- Platform BRD: `docs/Commerce_Platform.md`
- Owning ADR: `docs/pricing/architecture/ADR_001-Pricing_module_architecture.md`
- Aggregate PRD: `docs/pricing/product-requirements/PRD_002-Pricing_aggregate.md`
- Catalog PRD: `docs/catalog/product-requirements/catalog-module-prd.md`
- System ADR: `docs/system/ADR_001-System_architecture.md`
- Code: `pricing-domain/…`, `store/…/pricing/…`
