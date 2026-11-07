package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInventoryItemRequest(
        @NotBlank(message = "SKU is required")
        String sku,
        String variantId,
        @NotBlank(message = "Location ID is required")
        String locationId,
        @Min(value = 0, message = "Initial quantity cannot be negative")
        int initialQuantity,
        Integer safetyStock,
        Integer reorderPoint,
        Integer reorderQuantity,
        Integer maxStock
) {}
