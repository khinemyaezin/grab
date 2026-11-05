package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record CreateSellableProductWorkflowRequest(
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "SKU is required")
        String sku,
        @NotNull(message = "Price is required")
        BigDecimal price,
        @NotBlank(message = "Currency code is required")
        String currencyCode,
        @PositiveOrZero(message = "Initial stock must be zero or positive")
        int initialStock,
        String locationId,
        List<String> salesChannelIds,
        Map<String, String> attributes
) {}

