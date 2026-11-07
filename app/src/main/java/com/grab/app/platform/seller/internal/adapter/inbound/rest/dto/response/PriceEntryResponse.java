package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

import java.math.BigDecimal;

public record PriceEntryResponse(
        String id,
        String title,
        String currencyCode,
        BigDecimal amount,
        Integer minQuantity,
        Integer maxQuantity,
        String priceSetId,
        String priceListId
) {}
