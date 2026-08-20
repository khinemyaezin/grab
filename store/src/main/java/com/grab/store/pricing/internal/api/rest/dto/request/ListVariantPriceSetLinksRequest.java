package com.grab.store.pricing.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ListVariantPriceSetLinksRequest(
        @NotEmpty List<String> variantIds
) {
}
