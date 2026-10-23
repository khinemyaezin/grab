package com.grab.store.pricing.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.pricing.internal.config.PricingReadTransactional;
import com.pricing.application.model.read.QuoteVariantPricesQuery;
import com.pricing.application.model.read.QuoteVariantPricesResult;
import com.pricing.application.port.inbound.QuoteVariantPricesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuoteVariantPricesQueryHandler implements QueryHandler<QuoteVariantPricesQuery, QuoteVariantPricesResult> {

    private final QuoteVariantPricesUseCase quoteVariantPricesUseCase;

    @Override
    @PricingReadTransactional
    public QuoteVariantPricesResult handle(QuoteVariantPricesQuery query) {
        return quoteVariantPricesUseCase.execute(query);
    }

    @Override
    public Class<QuoteVariantPricesQuery> getQueryType() {
        return QuoteVariantPricesQuery.class;
    }
}
