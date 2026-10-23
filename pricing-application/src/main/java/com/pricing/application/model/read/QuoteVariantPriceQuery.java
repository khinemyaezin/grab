package com.pricing.application.model.read;

import com.grab.framework.id.Id;

public record QuoteVariantPriceQuery(
        String variantId,
        String currencyCode,
        int quantity,
        String salesChannelId,
        Id regionId
) {
}
