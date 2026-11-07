package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateProductRequest(
        @Valid Product product,
        @Valid List<VariantType> variantTypes,
        String name,
        String categoryId,
        String condition,
        String slug,
        String status,
        List<CreateVariantRequest> variants
) {
    public String resolveName() {
        return product != null && product.name() != null ? product.name() : name;
    }

    public String resolveCategoryId() {
        return product != null && product.categoryId() != null ? product.categoryId() : categoryId;
    }

    public String resolveCondition() {
        return product != null && product.condition() != null ? product.condition() : condition;
    }

    public String resolveSlug() {
        return product != null && product.slug() != null ? product.slug() : slug;
    }

    public String resolveStatus() {
        return product != null && product.status() != null ? product.status() : status;
    }

    public List<CreateVariantRequest> resolveVariants() {
        if (variants != null && !variants.isEmpty()) {
            return variants;
        }
        if (product != null && product.variants() != null) {
            return product.variants().stream()
                    .map(v -> new CreateVariantRequest(v.sku(), v.manageInventory()))
                    .toList();
        }
        return List.of();
    }

    public record Product(
            @NotBlank(message = "Product name is required")
            String name,
            @NotBlank(message = "Category ID is required")
            String categoryId,
            String condition,
            String slug,
            String status,
            @Valid List<Variant> variants
    ) {}

    public record Description(
            @NotBlank String name,
            String title,
            @NotBlank String description
    ) {}

    public record Media(
            String type,
            @NotBlank String path
    ) {}

    public record VariantType(
            @NotBlank String typeId,
            @Valid List<VariantOption> options
    ) {}

    public record VariantOption(
            @NotBlank String optionId
    ) {}

    public record Variant(
            @NotBlank(message = "SKU is required")
            String sku,
            @Valid List<Variation> variations,
            Boolean manageInventory
    ) {
        public Variant(String sku, List<Variation> variations) {
            this(sku, variations, null);
        }
    }

    public record Variation(
            @NotBlank String optionId,
            @NotBlank String typeId
    ) {}

    public record CreateVariantRequest(
            @NotBlank(message = "SKU is required")
            String sku,
            Boolean manageInventory
    ) {}
}
