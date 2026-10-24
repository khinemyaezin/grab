package com.pricing.application.port.inbound;

import com.pricing.application.model.read.QuoteVariantPricesQuery;
import com.pricing.application.model.read.QuoteVariantPricesResult;

public interface QuoteVariantPricesUseCase {
    QuoteVariantPricesResult execute(QuoteVariantPricesQuery query);
}
