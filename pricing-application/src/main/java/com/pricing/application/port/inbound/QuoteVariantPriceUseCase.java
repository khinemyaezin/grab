package com.pricing.application.port.inbound;

import com.pricing.application.model.read.QuoteVariantPriceQuery;
import com.pricing.application.model.read.QuoteVariantPriceResult;

import java.util.Optional;

public interface QuoteVariantPriceUseCase {
    Optional<QuoteVariantPriceResult> execute(QuoteVariantPriceQuery query);
}
