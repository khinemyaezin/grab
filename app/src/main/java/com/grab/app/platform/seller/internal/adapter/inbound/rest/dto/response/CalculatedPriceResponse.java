package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

import java.math.BigDecimal;

public record CalculatedPriceResponse(
        String id,
        String currencyCode,
        BigDecimal calculatedAmount,
        boolean calculatedPricePriceList,
        boolean calculatedPriceTaxInclusive,
        BigDecimal originalAmount,
        boolean originalPricePriceList,
        boolean originalPriceTaxInclusive
) {}
