package com.grab.store.pricing.internal.api.rest.dto.response;

import java.math.BigDecimal;

public record CalculatedPriceSetResponse(
        String id,
        String currencyCode,
        BigDecimal calculatedAmount,
        boolean calculatedPricePriceList,
        boolean calculatedPriceTaxInclusive,
        SelectedPriceResponse calculatedPrice,
        BigDecimal originalAmount,
        boolean originalPricePriceList,
        boolean originalPriceTaxInclusive,
        SelectedPriceResponse originalPrice
) {
    public record SelectedPriceResponse(
            String id,
            String priceListId,
            String priceListType,
            Integer minQuantity,
            Integer maxQuantity
    ) {
    }
}
