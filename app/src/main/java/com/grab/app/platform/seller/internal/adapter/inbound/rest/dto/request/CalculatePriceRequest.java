package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public record CalculatePriceRequest(
        @NotEmpty(message = "PriceSet IDs cannot be empty")
        List<String> priceSetIds,
        @NotBlank(message = "Currency code is required")
        String currencyCode,
        Integer quantity,
        Map<String, String> attributes
) {}
