# Product Requirements Document: Catalog Module

## 1. Summary

This document describes the catalog module in simple product terms.

The catalog module helps admin users organize categories, create products,
manage variants, and expose valid products to the storefront. It is based on
the current business direction in the commerce platform BRD and on the current
catalog implementation.

## 2. Problem

An ecommerce platform needs a clean and reliable catalog so products can be
organized, maintained, and shown to customers correctly.

Without this module:

- products may not belong to a valid category
- invalid or incomplete products may appear on the storefront
- variant and SKU data may become inconsistent
- storefront browsing becomes harder to support

## 3. Users

- Catalog admin who manages categories and products
- Internal operator who keeps catalog data clean
- Customer who browses active products on the storefront

## 4. Goals

- Provide a clear category structure for organizing products
- Let admin users create and maintain products
- Support products with variants such as size or color
- Show only valid and active products on the storefront
- Protect the catalog from invalid data and broken relationships

## 5. In Scope

The current catalog module includes:

- category creation and category browsing
- product creation and product update
- product lifecycle control
- product variant management
- storefront product discovery
- slug-based product lookup
- catalog integrity rules between products, variants, and categories

## 6. Out of Scope

The catalog module does not own:

- seller profile management
- seller ownership of listings
- pricing and commission logic
- C2C offer negotiation
- deal management
- cart and checkout
- Cash on Delivery payment flow
- payment status tracking
- shipment and delivery
- returns and refunds
- promotions and vouchers
- inventory reservation at checkout
- media upload workflow

## 7. Main Features

### 7.1 Category Management

The module supports a product category structure for catalog organization.

Current phase:

- create category
- view category
- view category tree
- view parent category
- view child categories
- delete a category only when its subtree has no assigned products

### 7.2 Product Management

The module supports the basic product authoring flow.

Current phase:

- create product
- view product
- update product details
- archive or reactivate a product
- mark a product as featured

### 7.3 Variant Management

The module supports product variants so one product can have multiple sellable
options.

Current phase:

- create products with variants
- update a single variant
- delete a variant
- restore a deleted variant
- sync variants in bulk
- generate variant combinations

### 7.4 Storefront Discovery

The module supports basic storefront-facing discovery.

Current phase:

- search product summaries
- view product detail
- view product by slug
- list featured products
- list products by category

### 7.5 Catalog Safety Rules

The module protects catalog quality through core business rules.

Key rules:

- a product must belong to a valid category
- a product cannot become active without at least one active variant
- an active product cannot lose its last active variant
- duplicate SKU usage is blocked
- categories with assigned products cannot be deleted
- only storefront-eligible products should appear in storefront queries

## 8. Success Criteria

The catalog module is successful for the current phase when:

- admin users can create and organize categories
- admin users can create and maintain products with variants
- invalid product and category relationships are blocked
- active products can be discovered on the storefront
- inactive or unsellable products do not appear in storefront queries

## 9. Dependencies

The catalog module depends on the wider platform for future capabilities that
are not part of this scope, such as:

- seller profile support
- pricing support
- deal management
- order management
- payment handling
- inventory handling

## 10. Related Documents

- `docs/BRD/commerce-platform-brd.md`
- `docs/PRD/deal-management-prd.md`
- `docs/features/product-module-business-reference.md`
