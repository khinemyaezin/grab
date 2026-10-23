package com.pricing.application.policy;

import com.grab.framework.id.Id;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.model.read.QuoteVariantPricesQuery;

public final class QuoteVariantPricesPolicy {
    public void validate(QuoteVariantPricesQuery query) {
        if (query == null) {
            throw invalid("query");
        }
        if (query.variantIds() == null) {
            throw invalid("variantIds");
        }
        for (int index = 0; index < query.variantIds().size(); index++) {
            Id variantId = query.variantIds().get(index);
            String variantValue = variantId == null ? null : variantId.getValue();
            if (isBlank(variantValue)) {
                throw invalid("variantIds[" + index + "]");
            }
        }
        if (isBlank(query.currencyCode())) {
            throw invalid("currencyCode");
        }
        if (query.salesChannelId() == null || isBlank(query.salesChannelId().getValue())) {
            throw invalid("salesChannelId");
        }
        if (query.regionId() == null || isBlank(query.regionId().getValue())) {
            throw invalid("regionId");
        }
        if (query.quantity() <= 0) {
            throw invalid("quantity");
        }
    }

    private PricingServiceException invalid(String field) {
        return new PricingServiceException(
                new PricingServiceError.InvalidBatchQuote(field),
                "Invalid batch quote field: " + field
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
