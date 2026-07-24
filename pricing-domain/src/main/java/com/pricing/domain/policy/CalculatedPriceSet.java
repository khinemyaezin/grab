package com.pricing.domain.policy;

import com.grab.framework.id.Id;
import com.pricing.domain.enums.PriceListType;

import java.math.BigDecimal;

public record CalculatedPriceSet(
        Id priceSetId,
        String currencyCode,
        BigDecimal calculatedAmount,
        boolean calculatedPricePriceList,
        boolean calculatedPriceTaxInclusive,
        SelectedPrice calculatedPrice,
        BigDecimal originalAmount,
        boolean originalPricePriceList,
        boolean originalPriceTaxInclusive,
        SelectedPrice originalPrice
) {
    public record SelectedPrice(
            Id priceId,
            Id priceListId,
            PriceListType priceListType,
            Integer minQuantity,
            Integer maxQuantity
    ) {
    }
}
