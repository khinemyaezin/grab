package com.grab.store.pricing.internal.api.rest.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PriceResponse(
        String id,
        String title,
        String currencyCode,
        BigDecimal amount,
        Integer minQuantity,
        Integer maxQuantity,
        String priceSetId,
        String priceListId,
        List<PriceRuleResponse> rules
) {
}
