# Grab Monorepo Overview

Maven multi-module workspace organized by bounded contexts (product, category, pricing) with a small shared framework and a Spring Boot storefront application.

## Modules
- `framework`: shared building blocks (DDD abstractions such as `AggregateRoot`, `Entity`, specs, ID interfaces, mapping helpers, logging).
- `product-domain`: pure domain model for products (aggregates, variants, factory, SKU/variant services); no Spring.
- `product-infrastructure`: JPA-facing adapters for product (entities, Spring Data repositories, mappers, option/variant services). Intended to stay framework-light; wire via config in the application layer.
- `product-application`: thin application entry point placeholder for the product context.
- `category-domain` / `category-infrastructure`: category bounded context split the same way as product.
- `pricing-domain` / `pricing-infrastructure`: pricing context (domain and data adapters), parented to the root POM but not listed in the top-level `<modules>` yet.
- `store`: Spring Boot application composing the bounded contexts (imports infra modules, supplies beans like `IdGenerator`).
- `docs`: diagrams (`*.drawio`) describing module relationships.
- `helper`: fixture-style helper data (e.g., `persistable_product_variants.json`).

## Build & Run
- Build core modules: `mvn clean install` (from repo root).
- Build/run store app: `mvn -pl store spring-boot:run` (ensure it depends on the published/installed modules).

## Conventions
- Domain modules stay free of Spring/web concerns; infrastructure holds persistence/mapping; application (store) wires everything.
- MapStruct/Lombok processors are configured in the parent POM; default component model for mappers can be overridden per module.
