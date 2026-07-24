package com.grab.store.pricing.internal.query;

import java.math.BigDecimal;

public record CalculatedPriceSetResult(
        String id,
        String currencyCode,
        BigDecimal calculatedAmount,
        boolean calculatedPricePriceList,
        boolean calculatedPriceTaxInclusive,
        SelectedPriceResult calculatedPrice,
        BigDecimal originalAmount,
        boolean originalPricePriceList,
        boolean originalPriceTaxInclusive,
        SelectedPriceResult originalPrice
) {
    public record SelectedPriceResult(
            String id,
            String priceListId,
            String priceListType,
            Integer minQuantity,
            Integer maxQuantity
    ) {
    }
}
