package com.grab.app.productmerchandising.pricing.internal.api.rest.dto.response;

import java.util.List;

public record PriceSetResponse(
        String id,
        List<PriceResponse> prices
) {
}
