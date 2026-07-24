package com.grab.store.pricing.internal.api.rest.dto.response;

import java.time.Instant;
import java.util.List;

public record PriceListResponse(
        String id,
        String title,
        String description,
        String status,
        String type,
        Instant startsAt,
        Instant endsAt,
        List<PriceListRuleResponse> rules,
        List<PriceResponse> prices
) {
}
