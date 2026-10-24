package com.pricing.domain.policy;

import com.grab.framework.id.Id;

import java.util.List;

public final class QuoteVariantPricesPolicy {

    public void validate(
            List<Id> variantIds,
            String currencyCode,
            Id salesChannelId,
            Id regionId,
            int quantity
    ) {
        if (variantIds == null) {
            throw new IllegalArgumentException("variantIds");
        }
        for (int index = 0; index < variantIds.size(); index++) {
            Id variantId = variantIds.get(index);
            String variantValue = variantId == null ? null : variantId.getValue();
            if (isBlank(variantValue)) {
                throw new IllegalArgumentException("variantIds[" + index + "]");
            }
        }
        if (isBlank(currencyCode)) {
            throw new IllegalArgumentException("currencyCode");
        }
        if (salesChannelId == null || isBlank(salesChannelId.getValue())) {
            throw new IllegalArgumentException("salesChannelId");
        }
        if (regionId == null || isBlank(regionId.getValue())) {
            throw new IllegalArgumentException("regionId");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
