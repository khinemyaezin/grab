# Seller HAL contract

This document is the public Seller BFF contract for the `app` module. The
authoritative implementation sources are the seller root controllers,
`Seller*ApiLinks` facades, and representation assemblers under
`app/src/main/java/com/grab/app/platform/seller`. The `store` module is not a
source for this contract.

Relation names are project-specific HAL relation types unless stated
otherwise. `self` is the standard HAL relation. Other relations use the R9
action-first kebab-case convention: `get-*`, `list-*`, `search-*`,
`create-*`, `update-*`, `delete-*`, and action-qualified resource names.

Start with `GET /api/v1` and request `Accept: application/hal+json`. Follow a resource-root relation, then select the collection, item, or action relation from its `_links` object. The API root and the identity, catalog, inventory, merchant, pricing, sales-channel, and workflows roots support public discovery. The region root requires authentication.

Use each link's `href` as supplied. A link marked `templated: true` uses URI Template syntax. Expand the path variables shown in the template, such as `merchantId`, `productId`, `variantId`, or `workflowId`, and supply optional query variables only when needed. A relation can contain a single link object or an array. Read both shapes and treat an absent relation as unavailable in that representation.

Consult the matching operation in `GET /v3/api-docs` for the HTTP method, body schema, validation, and headers. Supply the bearer token for protected operations. Where the operation declares `X-Merchant-Id` or `X-Actor-Id`, use the current authorized seller context. Link presence provides navigation. The operation still enforces authorization and business rules.

## Examples

The discovery response includes links such as:

```json
{"_links":{"self":{"href":"https://seller.example/api/v1"},"get-catalog-root":{"href":"https://seller.example/api/v1/catalog"}}}
```

Catalog can return a templated detail link:

```json
{"_links":{"get-variant":{"href":"https://seller.example/api/v1/products/{productId}/variants/{variantId}","templated":true}}}
```

A filtered product page retains the current query in `self`. Even an empty page advertises the default list and creation operations:

```json
{"_links":{"self":{"href":"https://seller.example/api/v1/products?query=shoe&page=0&size=20"},"list-products":{"href":"https://seller.example/api/v1/products?page=0&size=20"},"create-product":{"href":"https://seller.example/api/v1/products"}},"page":{"size":20,"totalElements":0,"totalPages":0,"number":0}}
```

A product GET uses `self` for the canonical product URI.

```json
{"id":"p1","_links":{"self":{"href":"https://seller.example/api/v1/products/p1"}}}
```

A workflow start returns a concrete status link. Follow it with GET to poll the workflow.

```json
{"workflowId":"w1","_links":{"get-create-sellable-product":{"href":"https://seller.example/api/v1/workflows/create-sellable-product/w1"}}}
```

## Frontend migration

Update relation lookups together with this backend change.

| Previous relation | Canonical relation |
| --- | --- |
| `allocate` | `allocate-stock` |
| `deallocate` | `deallocate-stock` |
| `refresh-token` | `refresh` |
| `location` | `get-location` |
| `inventory-item` | `get-inventory-item` |
| `latest-movement` | `get-inventory-movement-latest` |
| `reorder-suggestions` | `list-reorder-suggestions` |
| `list-my-merchants` | `list-merchants` |
| `edit-merchant-application` | `update-merchant-profile` |
| `accept-invitation` | `accept-merchant-member-invitation` |
| `change-role` | `update-merchant-member-role` |
| `remove-member` | `remove-merchant-member` |
| `list-variant-options` | `search-variant-options` |
| `select-access-context` | `select-context` |
| `get-current-merchant` | `get-merchant` with `{merchantId}` expanded from the current merchant-scoped access context |

Use `list-products` for the default collection and `search-products` for filtering. For command responses, follow the relevant `get-*` relation when available. Merchant member and variant option items link to their collections via `list-merchant-members` and `search-variant-options`.

The app contract does not currently expose every relation still referenced by
the web migration. The following remain legacy web-only capabilities and are
not canonical app relations: inventory search and nested facility relations,
inventory item lifecycle actions, product media and publication actions,
variant-image batching, `list-variant-price-links`, and `event-stream`.
Do not rename these relations speculatively; migrate them only when the app
publishes an owning endpoint and relation.

The `get-merchant` relation is templated. The web client must obtain the
`scopeId` from the current non-global access context and expand
`{merchantId}` before following the link. A global or missing access context
does not identify a merchant and must be treated as unavailable.

## Relation reference

The route column shows the operation path. Product pagination adds `prev` and `next` links when those pages exist. Query variables appear in the emitted links where applicable. Assemblers may publish a retrieval or collection link as `self` on its canonical GET representation.

| Resource owner | Relation | Method and route | Where it appears |
| --- | --- | --- | --- |
| Allocation | `allocate-stock` | `POST /api/v1/inventory/allocations` | Inventory root, item or command result |
| Allocation | `deallocate-stock` | `POST /api/v1/inventory/allocations/deallocate` | Inventory root, item or command result |
| Auth | `login` | `POST /api/v1/identity/auth/login` | Auth root, item or command result |
| Auth | `logout` | `POST /api/v1/identity/auth/logout` | Auth root, item or command result |
| Auth | `refresh` | `POST /api/v1/identity/auth/refresh` | Auth root, item or command result |
| Auth | `register` | `POST /api/v1/identity/auth/register` | Auth root |
| Category | `create-category` | `POST /api/v1/categories` | Collection |
| Category | `delete-category` | `DELETE /api/v1/categories/{categoryId}` | Item or command result |
| Category | `get-category` | `GET /api/v1/categories/{categoryId}` | Item or command result |
| Category | `get-category-tree` | `GET /api/v1/categories/tree` | Catalog root, item or command result |
| Category | `list-category-children` | `GET /api/v1/categories/{categoryId}/children` | Collection |
| Category | `search-category-leaves` | `GET /api/v1/categories/leaf-nodes` | Catalog root, collection |
| Discovery | `get-catalog-root` | `GET /api/v1/catalog` | API root, Catalog root |
| Discovery | `get-identity-root` | `GET /api/v1/identity` | API root, Auth root |
| Discovery | `get-inventory-root` | `GET /api/v1/inventory` | API root, Inventory root |
| Discovery | `get-merchant-root` | `GET /api/v1/merchants/root` | API root, Merchant root |
| Discovery | `get-pricing-root` | `GET /api/v1/pricing` | API root, Pricing root |
| Discovery | `get-region-root` | `GET /api/v1/regions` | API root, Region root |
| Discovery | `get-sales-channel-root` | `GET /api/v1/sales-channels/root` | API root, SalesChannel root |
| Discovery | `get-workflows-root` | `GET /api/v1/workflows/root` | API root, Workflow root |
| Discovery | `self` | `GET /api/v1` | API root |
| Facility | `activate-bin` | `PATCH /api/v1/facilities/bins/{binId}/activate` | Item or command result |
| Facility | `activate-location` | `PATCH /api/v1/facilities/locations/{locationId}/activate` | Item or command result |
| Facility | `activate-zone` | `PATCH /api/v1/facilities/zones/{zoneId}/activate` | Item or command result |
| Facility | `create-location` | `POST /api/v1/facilities/locations` | Inventory root |
| Facility | `deactivate-bin` | `PATCH /api/v1/facilities/bins/{binId}/deactivate` | Item or command result |
| Facility | `deactivate-location` | `PATCH /api/v1/facilities/locations/{locationId}/deactivate` | Item or command result |
| Facility | `deactivate-zone` | `PATCH /api/v1/facilities/zones/{zoneId}/deactivate` | Item or command result |
| Facility | `delete-bin` | `DELETE /api/v1/facilities/bins/{binId}` | Item or command result |
| Facility | `delete-location` | `DELETE /api/v1/facilities/locations/{locationId}` | Item or command result |
| Facility | `delete-zone` | `DELETE /api/v1/facilities/zones/{zoneId}` | Item or command result |
| Facility | `get-bin` | `GET /api/v1/facilities/bins/{binId}` | Item or command result |
| Facility | `get-location` | `GET /api/v1/facilities/locations/{locationId}` | Inventory root, item or command result |
| Facility | `get-zone` | `GET /api/v1/facilities/zones/{zoneId}` | Item or command result |
| Inventory | `create-inventory-item` | `POST /api/v1/inventory/items` | Inventory root |
| Inventory | `get-inventory-item` | `GET /api/v1/inventory/items/{itemId}` | Inventory root, item or command result |
| Inventory | `get-inventory-movement-latest` | `GET /api/v1/inventory/items/{itemId}/movements/latest` | Inventory root, item or command result |
| Merchant | `get-merchant` | `GET /api/v1/merchants/{merchantId}` | Merchant root, item or command result |
| Merchant | `list-merchant-review-queue` | `GET /api/v1/identity/admin/merchants` | collection |
| Merchant | `list-merchants` | `GET /api/v1/merchants` | Merchant root, collection |
| Merchant | `update-merchant-profile` | `PUT /api/v1/merchants/{merchantId}/profile` | Item or command result |
| MerchantApplication | `create-c2c-application` | `POST /api/v1/merchants/applications/c2c` | Merchant root |
| MerchantApplication | `create-first-party-retailer-application` | `POST /api/v1/merchants/applications/first-party` | Merchant root |
| MerchantApplication | `create-merchant-application` | `POST /api/v1/merchants/applications` | Merchant root, item or command result, collection |
| MerchantApplication | `get-c2c-application` | `GET /api/v1/merchants/applications/c2c` | Merchant root, item or command result |
| MerchantApplication | `get-first-party-retailer-application` | `GET /api/v1/merchants/applications/first-party` | Merchant root, item or command result |
| MerchantApplication | `submit-merchant-application` | `PUT /api/v1/merchants/applications/{applicationId}/submit` | Item or command result |
| MerchantMember | `accept-merchant-member-invitation` | `POST /api/v1/merchants/{merchantId}/members/{memberId}/accept` | Item or command result |
| MerchantMember | `invite-merchant-member` | `POST /api/v1/merchants/{merchantId}/members/invite` | Collection |
| MerchantMember | `list-merchant-members` | `GET /api/v1/merchants/{merchantId}/members` | collection |
| MerchantMember | `remove-merchant-member` | `DELETE /api/v1/merchants/{merchantId}/members/{memberId}` | Item or command result |
| MerchantMember | `update-merchant-member-role` | `PATCH /api/v1/merchants/{merchantId}/members/{memberId}/role` | Item or command result |
| Pricing | `calculate-prices` | `POST /api/v1/pricing/price-sets/calculate` | Pricing root, item or command result |
| Pricing | `create-price-list` | `POST /api/v1/pricing/price-lists` | Pricing root, item or command result, collection |
| Pricing | `create-price-preference` | `POST /api/v1/pricing/preferences` | Pricing root |
| Pricing | `create-price-set` | `POST /api/v1/pricing/price-sets` | Pricing root |
| Pricing | `get-price-list` | `GET /api/v1/pricing/price-lists/{priceListId}` | Item or command result |
| Pricing | `get-price-preference` | `GET /api/v1/pricing/preferences/{preferenceId}` | Pricing root, item or command result |
| Pricing | `get-price-set` | `GET /api/v1/pricing/price-sets/{priceSetId}` | Item or command result |
| Pricing | `list-price-lists` | `GET /api/v1/pricing/price-lists` | Pricing root, collection |
| Product | `create-product` | `POST /api/v1/products` | Catalog root, item or command result, collection |
| Product | `delete-product` | `DELETE /api/v1/products/{productId}` | Item or command result |
| Product | `get-product` | `GET /api/v1/products/{productId}` | Catalog root, item or command result |
| Product | `get-product-by-slug` | `GET /api/v1/products/by-slug/{slug}` | Item or command result |
| Product | `get-variant` | `GET /api/v1/products/{productId}/variants/{variantId}` | Catalog root, item or command result |
| Product | `list-products` | `GET /api/v1/products` | Catalog root, item or command result, collection |
| Product | `search-products` | `GET /api/v1/products` | Catalog root, collection |
| Product | `update-product` | `PUT /api/v1/products/{productId}` | Item or command result |
| Product | `update-product-status` | `PATCH /api/v1/products/{productId}/status` | Item or command result |
| PublishableKey | `create-publishable-key` | `POST /api/v1/sales-channels/{channelId}/publishable-keys` | Item or command result |
| PublishableKey | `resolve-publishable-key` | `GET /api/v1/sales-channels/publishable-keys/resolve` | SalesChannel root |
| PublishableKey | `revoke-publishable-key` | `DELETE /api/v1/sales-channels/{channelId}/publishable-keys/{keyId}` | Item or command result |
| Region | `get-region` | `GET /api/v1/regions/{regionId}` | Region root, item or command result |
| Region | `list-region-countries` | `GET /api/v1/regions/countries` | Merchant root, Region root, collection |
| ReorderSuggestion | `list-reorder-suggestions` | `GET /api/v1/inventory/reorder-suggestions` | Inventory root, collection |
| SalesChannel | `create-merchant-website-channel` | `POST /api/v1/sales-channels/merchant-website` | SalesChannel root, item or command result, collection |
| SalesChannel | `get-sales-channel` | `GET /api/v1/sales-channels/{salesChannelId}` | SalesChannel root, item or command result |
| SalesChannel | `list-sales-channels` | `GET /api/v1/sales-channels` | SalesChannel root, collection |
| Session | `get-profile` | `GET /api/v1/identity/session` | Auth root, item or command result |
| Session | `list-access-contexts` | `GET /api/v1/identity/session/contexts` | Auth root, collection |
| Session | `select-context` | `POST /api/v1/identity/session/contexts/{assignmentId}/select` | Item or command result |
| Storefront | `activate-storefront` | `PATCH /api/v1/merchants/{merchantId}/storefronts/{storefrontId}/activate` | Item or command result |
| Storefront | `close-storefront` | `PATCH /api/v1/merchants/{merchantId}/storefronts/{storefrontId}/close` | Item or command result |
| Storefront | `create-storefront` | `POST /api/v1/merchants/{merchantId}/storefronts` | Merchant root, item or command result, collection |
| Storefront | `get-storefront` | `GET /api/v1/merchants/{merchantId}/storefronts/{storefrontId}` | Merchant root, item or command result |
| Storefront | `list-storefronts` | `GET /api/v1/merchants/{merchantId}/storefronts` | Merchant root, collection |
| Storefront | `update-storefront` | `PUT /api/v1/merchants/{merchantId}/storefronts/{storefrontId}` | Item or command result |
| VariantOption | `search-variant-options` | `GET /api/v1/variant-options` | Catalog root, collection |
| VariantOption | `search-variant-types` | `GET /api/v1/variant-options/types` | Catalog root, collection |
| Workflow | `create-sellable-product` | `POST /api/v1/workflows/create-sellable-product` | Catalog root, Workflow root, item or command result |
| Workflow | `get-create-sellable-product` | `GET /api/v1/workflows/create-sellable-product/{workflowId}` | Workflow root, item or command result |
| Workflow | `get-update-product-variant` | `GET /api/v1/workflows/update-product-variant/{workflowId}` | Workflow root, item or command result |
| Workflow | `get-update-sellable-product` | `GET /api/v1/workflows/update-sellable-product/{workflowId}` | Workflow root, item or command result |
| Workflow | `get-workflow-status` | `GET /api/v1/workflows/{workflowId}` | Item or command result |
| Workflow | `list-recent-workflows` | `GET /api/v1/workflows` | collection |
| Workflow | `update-product-variant` | `POST /api/v1/workflows/update-product-variant` | Catalog root, Workflow root, item or command result |
| Workflow | `update-sellable-product` | `POST /api/v1/workflows/update-sellable-product` | Catalog root, Workflow root, item or command result |
