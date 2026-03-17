# Feature: Revised Catalog Plan: Amazon-Style Hybrid Listing Model

## Summary

Revise the catalog plan from a seller-owned product aggregate to an Amazon-style hybrid model:

- `ProductFamily` is the shared catalog record customers discover on the storefront.
- `SellerListing` is the seller-owned publication record attached to a `ProductFamily`.
- Product content is parent-first with variant overrides and fallback resolution.
- Pricing remains outside catalog.
- Generic product update does **not** replace descriptions/media. The server loads the current aggregate on write, but omitted content fields mean `no change`.

This keeps the BRD boundary intact while aligning the listing model to a marketplace pattern closer to Amazon: shared product page, seller-specific listing/publication, external pricing.

## Target Domain Model

### 1. Shared catalog layer

Introduce `ProductFamily` as the catalog aggregate root for shared product information.

It owns:
- `productFamilyId`
- category assignment
- canonical title and structured attributes
- parent descriptions as child entities with stable IDs
- parent media items as child entities with stable IDs and ordering
- family variants with stable IDs
- variant content overrides with stable IDs
- family slug
- family governance status: `DRAFT`, `READY`, `BLOCKED`, `ARCHIVED`

Rules:
- family content is shared across sellers
- variant IDs are catalog-stable and seller-independent
- descriptions/media are aggregate-owned child entities with non-null IDs generated in the domain
- media child entities reference an asset path/reference; the asset itself is not the child entity

### 2. Seller listing layer

Introduce `SellerListing` as the seller-owned aggregate root for publication and sellability.

It owns:
- `sellerListingId`
- `sellerId`
- `sellerType`
- `productFamilyId`
- listing publication lifecycle: `DRAFT`, `IN_REVIEW`, `ACTIVE`, `REJECTED`, `SUSPENDED`, `ARCHIVED`
- moderation note/history reference
- offered variant selections
- seller SKU per offered variant
- C2C fields: `condition`, `offerEligible`, seller note
- seller-scoped publication flags and audit trail

Rules:
- a listing cannot exist without a valid `ProductFamily`
- a listing cannot activate if the family is `BLOCKED` or incomplete
- seller SKU is listing-scoped, not catalog-global
- current global SKU invariant moves out of `ProductFamily`

### 3. Content model

Descriptions:
- Move to aggregate-owned child entities if partial patching is required.
- Each description has `descriptionId`, `name`, `title`, `body`, `sortOrder`.

Media:
- Move to aggregate-owned child entities.
- Each media item has `mediaId`, `assetRef/path`, `type`, `sortOrder`, `isPrimary`, `altText`.

Variant content:
- Parent fallback is the default.
- A variant may override selected descriptions/media.
- Storefront resolves effective content as `variant override -> parent family content`.

## Update Semantics

### Product update contract

`PATCH /products/{productFamilyId}` updates only family metadata:
- title
- category
- structured attributes
- family status transitions where allowed
- slug controls

It must **not** clear or replace descriptions/media when those fields are absent.

Behavior:
- field absent: no change
- explicit content endpoints handle descriptions/media
- server still loads the current aggregate with child content to enforce invariants

### Content update contract

Use explicit content APIs.

Family content:
- `PUT /products/{id}/descriptions` = full replace
- `PATCH /products/{id}/descriptions` = patch by child ID
- `PUT /products/{id}/media` = full replace
- `PATCH /products/{id}/media` = patch by child ID
- `POST /products/{id}/media/reorder` = reorder
- `POST /products/{id}/media/{mediaId}/make-primary` = primary selection

Variant overrides:
- `PATCH /products/{id}/variants/{variantId}/content`
- `DELETE /products/{id}/variants/{variantId}/content/descriptions/{descriptionId}`
- `DELETE /products/{id}/variants/{variantId}/content/media/{mediaId}`
- `POST /products/{id}/variants/{variantId}/content/clear-description-override`
- `POST /products/{id}/variants/{variantId}/content/clear-media-override`

Listing update:
- `PATCH /listings/{listingId}` updates seller-owned listing metadata only
- `POST /listings/{listingId}/submit-review`
- `POST /listings/{listingId}/approve`
- `POST /listings/{listingId}/reject`
- `POST /listings/{listingId}/suspend`
- `POST /listings/{listingId}/restore`

## Storefront Model

Storefront discovery shifts from seller listing pages to family pages.

Queries:
- search/browse returns `ProductFamily` cards with seller-offer summary projection
- slug lookup resolves `ProductFamily` detail page
- family detail query returns:
  - effective family/variant content
  - available variants
  - active seller listing summaries
  - no prices, only listing references for external pricing resolution

Listing summaries exposed from catalog:
- `sellerListingId`
- `sellerId`
- `sellerType`
- condition
- offerEligible
- availability flag
- default fulfillment badge inputs if needed
- external price reference key only

## Persistence Refactor

Replace the current replace-all content persistence model.

### Tables/entities

Keep physical table renames optional for the first cut, but target semantics are:
- `product` becomes `product_family` logically
- add `seller_listing`
- add `seller_listing_variant`
- replace shared `media` many-to-many with owned content tables:
  - `product_family_media`
  - `product_family_description`
  - `product_variant_media_override`
  - `product_variant_description_override`

Rules:
- no shared `MediaEntity` across product and variant
- no many-to-many for aggregate-owned listing media
- child rows use stable UUIDs generated before persistence

### Migration defaults

Backfill:
- existing `product` rows become `ProductFamily`
- existing seller-owned fields on `product` move into a new default `SellerListing`
- existing product status moves to listing publication status
- existing `slug` stays on family
- existing descriptions/media get generated child IDs if missing

## Public Interface Changes

### Commands/types

Add:
- `PatchProductFamilyCommand`
- `ReplaceFamilyDescriptionsCommand`
- `PatchFamilyDescriptionsCommand`
- `ReplaceFamilyMediaCommand`
- `PatchFamilyMediaCommand`
- `PatchVariantContentCommand`
- `CreateSellerListingCommand`
- `PatchSellerListingCommand`
- `ModerateSellerListingCommand`

Change:
- current `UpdateProductCommand` becomes metadata-only family patch
- current product status command moves to listing lifecycle
- current global variant SKU enforcement is removed from family commands

### Read models

Add:
- `GetProductFamilyQuery`
- `GetProductFamilyBySlugQuery`
- `SearchProductFamiliesQuery`
- `GetSellerListingQuery`
- `GetProductFamilyOffersQuery`
- `GetSellerListingAuditQuery`

Change:
- storefront queries return family-centric projections
- admin/seller management queries return listing-centric projections

## Implementation Sequence

1. Update PRD and ADRs to change the target boundary from seller-owned product to `ProductFamily + SellerListing`.
2. Introduce new aggregate types and child entity types in the domain.
3. Move content ownership from mutable bag objects to aggregate-owned child entities with generated IDs.
4. Refactor persistence away from shared media many-to-many and replace-all merges.
5. Add explicit content commands/endpoints and make generic product update metadata-only.
6. Introduce seller listing aggregate, persistence, moderation flow, and seller-variant mapping.
7. Rework storefront read models to family-first discovery with seller listing summaries.
8. Migrate existing data into family + default listing structure.
9. Deprecate old product status semantics and old global SKU rules.
10. Add targeted regression tests, then update feature docs and integration tests.

## Test Cases And Scenarios

- updating product metadata without descriptions/media leaves existing content unchanged
- explicit replace descriptions removes omitted items and preserves provided child IDs
- patch descriptions updates only targeted child entities
- patch media can add, update, reorder, and mark primary without replacing the whole collection
- variant override content falls back to parent when override is absent
- clearing a variant override restores parent fallback behavior
- two new child content entities with generated IDs do not compare equal
- listing activation fails when family is blocked or incomplete
- listing moderation transitions follow allowed lifecycle only
- global duplicate seller SKU across different sellers is allowed if listing-scoped uniqueness is chosen
- duplicate seller SKU within the same listing is rejected
- storefront family query returns family content plus active seller listing summaries
- C2C listing requires condition before review or activation
- existing migrated products produce one default seller listing each

## Assumptions And Defaults

- Chosen boundary: hybrid Amazon-style catalog, not a full separate offer module.
- Chosen content behavior: patch + explicit replace, not implicit replace on generic update.
- Chosen content resolution: parent fallback with variant overrides.
- Pricing stays outside catalog and is resolved by an external pricing capability.
- Search/PDP become family-centric rather than listing-centric.
- C2C defaults to dedicated families unless later normalization is introduced.
- SKU becomes seller-listing-scoped; catalog family variants keep stable platform IDs instead.
- The write side continues loading the current aggregate from persistence before applying updates.
