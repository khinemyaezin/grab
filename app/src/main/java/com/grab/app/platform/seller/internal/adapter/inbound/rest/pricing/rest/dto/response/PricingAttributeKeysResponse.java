package com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response;

import java.util.List;

public record PricingAttributeKeysResponse(
        List<String> wellKnownKeys,
        List<String> taxPreferenceLookupOrder
) {
}
