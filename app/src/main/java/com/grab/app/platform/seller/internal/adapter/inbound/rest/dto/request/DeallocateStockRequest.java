package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DeallocateStockRequest(
        @NotBlank(message = "SKU is required")
        String sku,
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,
        @NotBlank(message = "Order ID is required")
        String orderId
) {}
