package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;

public record UpdateProductVariantWorkflowRequest(
        @NotBlank(message = "Title is required")
        String title,
        @NotNull(message = "Price is required")
        BigDecimal price,
        @NotBlank(message = "Currency code is required")
        String currencyCode,
        int stockDelta,
        Map<String, String> optionValues
) {}

