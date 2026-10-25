package com.grab.app.productmerchandising.pricing.internal.api.rest.dto.response;

import java.util.List;

public record PricingAttributeKeysResponse(
        List<String> wellKnownKeys,
        List<String> taxPreferenceLookupOrder
) {
}
