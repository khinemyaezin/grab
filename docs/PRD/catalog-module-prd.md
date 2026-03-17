# Product Requirements Document: Catalog Module

## 1. Summary

This PRD defines the target scope of the catalog module for the commerce
platform described in the Business Requirements Document.

The catalog module is the source of truth for seller-owned product listings
and storefront-ready catalog data. It enables retailer, third-party seller,
and consumer-to-consumer (C2C) selling models by governing what can be listed,
how it is described, when it can be published, and how it can be discovered
by customers.

This document is intentionally broader than the current implementation. The
current codebase covers only part of this scope. This PRD defines what the
catalog module must include to satisfy the BRD.

## 2. Problem

The commerce platform needs one governed catalog capability that works across
retailer, 3P, and C2C selling models.

Without a proper catalog module:

- seller ownership of listings is unclear
- products may be listed in invalid or prohibited categories
- listings may be published without complete content or review
- storefront search and browse become inconsistent
- downstream modules cannot rely on stable listing and variant data

## 3. Users

- Platform admin
- Retail seller
- Third-party seller
- Consumer seller (C2C)
- Customer browsing the storefront

## 4. Goals

- Allow valid sellers to create and manage listings within platform policy
- Support category, product, variant, media, and description management
- Govern listing publication through validation, moderation, and admin control
- Provide storefront-ready search, browse, filter, and detail discovery
- Integrate inventory availability without making catalog the stock ledger
- Support both marketplace foundation and later C2C expansion

## 5. What The Catalog Module Is About

The catalog module owns the sellable listing definition and publication rules
for the storefront.

It is about:

- category taxonomy and listing placement
- seller-owned listings
- product and variant content
- media references and descriptive content
- listing visibility and publication governance
- moderation and policy enforcement
- storefront discovery and merchandising

It is not about:

- pricing ownership or price calculation
- stock reservation or stock deduction
- checkout, order placement, or payment handling
- fee calculation or seller settlement
- delivery and shipment workflows

## 6. In Scope

The catalog module must include:

- category and subcategory management
- seller-owned listing records tied to valid seller profiles
- product titles, descriptions, specifications, and listing metadata
- variant definitions and sellable option combinations
- media references for listing presentation
- listing lifecycle states such as draft, review, publish, archive, and
  suspension
- moderation and approval workflows where required
- category-based listing rules and prohibited product controls
- C2C-specific listing metadata such as item condition and offer eligibility
- storefront search, browse, filter, slug lookup, and product detail queries
- featured or merchandised listing support
- bulk listing creation and bulk update support for platform and seller
  operations
- audit visibility for listing status, moderation actions, and key catalog
  changes

## 7. Out of Scope

The catalog module does not include:

- seller onboarding, KYC, or payout profile management
- pricing ownership, price calculation, and price history
- stock quantities, reservations, releases, or warehouse balancing
- cart, checkout, or order lifecycle management
- Cash on Delivery payment status handling
- platform commission calculation or settlement reporting
- shipment creation, tracking, or delivery management
- promotions, vouchers, and campaign pricing
- dispute case management
- customer ratings and reviews

## 8. Main Capabilities

### 8.1 Category And Catalog Policy Management

The module must support category structures that organize listings and enforce
catalog rules.

It must support:

- category hierarchy management
- category activation or retirement
- category-specific listing rules
- prohibited or restricted product controls by category
- admin governance over taxonomy changes

### 8.2 Seller-Owned Listing Authoring

The module must support listing creation and maintenance by valid sellers.

It must support:

- linking every listing to a valid seller profile
- recording seller type such as retailer, 3P, or C2C
- draft authoring before publication
- update, archive, and removal flows that preserve business history
- seller-scoped and admin-scoped editing flows

### 8.3 Product Content And Variant Management

The module must support the content customers need to evaluate a product and
the structure downstream modules need to identify sellable variants.

It must support:

- product name and description
- structured attributes or specifications
- variant types and options
- sellable variants with stable identifiers
- rules that prevent invalid or duplicate variant definitions

### 8.4 Media And Presentation

The module must support storefront-facing media and presentation metadata.

It must support:

- image or media references
- ordering of media assets
- primary media selection
- SEO-friendly slug support

The actual media upload pipeline may be implemented by a supporting service,
but catalog must own the references used by listings.

### 8.5 Sellability And Publication

The module must support rules that determine whether a listing can be exposed
and sold through the storefront.

It must support:

- visibility rules that use listing completeness, moderation state, and
  external availability signals
- storefront sellability decisions based on publication status plus inventory
  availability

Catalog does not own pricing. Price display and transactional pricing are
provided by external pricing, deal, or order capabilities.

### 8.6 Moderation And Governance

The module must support platform governance for catalog safety and trust.

It must support:

- review-required flows for seller types or categories that need moderation
- approve, reject, suspend, and restore actions
- reason capture for moderation outcomes
- admin override and policy enforcement actions
- audit history for important governance decisions

### 8.7 Storefront Discovery

The module must provide customer-facing catalog discovery.

It must support:

- product detail pages
- category browsing
- search and filtering
- slug-based lookup
- featured or merchandised listings
- seller-aware discovery in a multi-seller storefront

### 8.8 Seller Model Support

The module must support the business differences between seller models.

It must support:

- retailer and 3P listing flows
- C2C listing flows with condition metadata
- offer-eligible listing flags for C2C items
- stricter moderation and policy controls for C2C listings where required

## 9. Business Rules

- Every listing must belong to a valid seller profile.
- Every listing must belong to a valid category.
- A listing may be published only when required content is complete.
- A listing may be published only when policy checks and moderation checks
  are satisfied.
- Published storefront visibility must depend on listing publication state and
  inventory availability.
- C2C listings must support item condition metadata.
- Buyer offer negotiation must be allowed only for eligible C2C listings.
- Category and seller policy violations must be able to hide or suspend a
  listing without deleting business history.
- Variant identifiers must remain stable enough for inventory and order
  workflows to reference sellable items correctly.

## 10. Dependencies And Integrations

The catalog module depends on other platform capabilities for full operation:

- seller profile support for seller identity, type, and status
- inventory support for stock availability by variant and location
- pricing support for list price and price presentation
- storefront/search support for customer-facing discovery experiences
- admin and policy support for governance rules
- deal management for C2C offer acceptance and negotiated pricing
- order management for order-time pricing and history preservation

## 11. Phase Alignment

### Phase 1: Marketplace Foundation

The catalog module must at minimum support:

- category management
- seller-owned listings for retailer and 3P sellers
- product and variant management
- media references and descriptions
- storefront search, browse, and detail pages
- admin governance and moderation foundations
- inventory availability integration for storefront visibility

### Phase 2: Expanded Marketplace

The catalog module must extend to support:

- C2C listing workflows
- item condition metadata
- offer-eligible listing controls
- stricter moderation for C2C and high-risk listings
- stronger bulk operation support for marketplace sellers

## 12. Success Criteria

The catalog module is successful when:

- sellers can create and manage valid listings within policy
- admins can govern categories and listings without manual data fixes
- customers can discover accurate and trustworthy listings on the storefront
- only publishable and sellable listings appear in customer-facing queries
- the platform can support retailer, 3P, and C2C catalog needs from one
  module boundary

## 13. Related Documents

- `docs/BRD/commerce-platform-brd.md`
- `docs/PRD/inventory-module-prd.md`
- `docs/features/category-management.md`
- `docs/features/product-management.md`
- `docs/features/catalog-lifecycle-integrity-and-sku-policy.md`
