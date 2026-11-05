package com.grab.app.merchandising.pricing.internal.query.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import com.pricing.application.model.read.QuoteVariantPricesQuery;
import com.pricing.application.model.read.QuoteVariantPricesResult;
import com.pricing.application.port.inbound.QuoteVariantPricesUseCase;
import org.springframework.stereotype.Component;

@Component
public class QuoteVariantPricesQueryHandler implements QueryHandler<QuoteVariantPricesQuery, QuoteVariantPricesResult> {

    private final QuoteVariantPricesUseCase quoteVariantPricesUseCase;

    public QuoteVariantPricesQueryHandler(QuoteVariantPricesUseCase quoteVariantPricesUseCase) {
        this.quoteVariantPricesUseCase = quoteVariantPricesUseCase;
    }

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

