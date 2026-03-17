# Catalog Domain Aggregate Diagram

This diagram reflects the current catalog domain model in code.

## Class Diagram

```mermaid
classDiagram
    direction LR

    namespace Product Aggregate {
        class Product {
            +Id id
            +String name
            +Id categoryId
            +ProductStatus status
            +String slug
            +boolean featured
            +Id sellerId
            +SellerType sellerType
            +ListingCondition condition
            +boolean offerEligible
            +String moderationNote
            +List~ProductVariant~ variants
            +List~Description~ descriptions
            +List~ProductMedia~ medias
            +addVariant(variant) boolean
            +update(...)
            +replaceDescriptions(descriptions)
            +patchDescriptions(patches)
            +replaceMedias(medias)
            +patchMedias(patches)
            +changeStatus(newStatus)
            +submitForReview()
            +approve()
            +reject(reason)
            +suspend(reason)
            +restore()
            +delete()
        }

        class ProductVariant {
            +Id id
            +String sku
            +Set~ProductVariation~ variations
            +ProductVariantStatus status
            +markAsDeleted()
            +activate()
        }

        class Description {
            +Id id
            +String name
            +String title
            +String description
        }

        class ProductMedia {
            +Id id
            +String type
            +String path
        }

        class ProductVariation {
            +String optionName
            +Id optionId
            +String typeName
            +Id typeId
        }

        class ProductStatus {
            <<enumeration>>
            DRAFT
            IN_REVIEW
            ACTIVE
            ARCHIVED
            SUSPENDED
        }

        class ProductVariantStatus {
            <<enumeration>>
            ACTIVE
            DELETED
        }

        class SellerType {
            <<enumeration>>
            RETAILER
            THIRD_PARTY
            C2C
        }

        class ListingCondition {
            <<enumeration>>
            NEW
            USED
            REFURBISHED
        }
    }

    namespace Category Aggregate {
        class Category {
            +Id id
            +String name
            +Id parentId
            +boolean active
            +boolean listingAllowed
            +boolean reviewRequired
            +boolean c2cAllowed
            +isRoot() boolean
        }
    }

    namespace Variant Reference Model {
        class VariantType {
            +Id id
            +String name
            +Set~VariantOption~ options
            +addOption(option)
        }

        class VariantOption {
            +Id id
            +String name
            +VariantType variantType
        }
    }

    Product *-- "0..*" ProductVariant : owns
    Product *-- "0..*" Description : owns
    Product *-- "0..*" ProductMedia : owns
    Product --> ProductStatus
    Product --> SellerType
    Product --> ListingCondition

    ProductVariant *-- "1..*" ProductVariation
    ProductVariant --> ProductVariantStatus

    Product ..> Category : references by categoryId
    Category --> "0..1" Category : parentId

    VariantType *-- "0..*" VariantOption
    ProductVariation ..> VariantType : references typeId/typeName
    ProductVariation ..> VariantOption : references optionId/optionName
```

## Notes

- `Product` and `Category` are the current aggregate roots in the catalog domain.
- `ProductVariant`, `Description`, and `ProductMedia` are owned by `Product`.
- `ProductVariation` is a value object stored inside `ProductVariant`.
- `VariantType` and `VariantOption` support variation-combination logic and are referenced by `ProductVariation`; they are not persisted inside the `Product` aggregate.
- Products reference categories by `categoryId`; there is no direct cross-aggregate navigation.
