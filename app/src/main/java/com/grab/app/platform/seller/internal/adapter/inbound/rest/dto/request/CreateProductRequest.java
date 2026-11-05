package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateProductRequest(
        @NotBlank(message = "Product name is required")
        String name,
        @NotBlank(message = "Category ID is required")
        String categoryId,
        String condition,
        String slug,
        String status,
        List<CreateVariantRequest> variants
) {
    public record CreateVariantRequest(
            @NotBlank(message = "SKU is required")
            String sku,
            Boolean manageInventory
    ) {}
}
