package com.grab.app.productmerchandising.pricing.internal.api.adapter;

import com.grab.app.productmerchandising.pricing.internal.api.adapter.mapper.PricingQuoteQueryMapper;
import com.grab.app.productmerchandising.pricing.internal.config.PricingReadTransactional;
import com.grab.app.productmerchandising.pricing.port.PricingQuoteQuery;
import com.pricing.application.model.read.QuoteVariantPriceQuery;
import com.pricing.application.model.read.QuoteVariantPriceResult;
import com.pricing.application.model.read.QuoteVariantPricesQuery;
import com.pricing.application.model.read.QuoteVariantPricesResult;
import com.pricing.application.port.inbound.QuoteVariantPriceUseCase;
import com.pricing.application.port.inbound.QuoteVariantPricesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PricingQuoteQueryAdapter implements PricingQuoteQuery {

    private final QuoteVariantPriceUseCase quoteVariantPriceUseCase;
    private final QuoteVariantPricesUseCase quoteVariantPricesUseCase;
    private final PricingQuoteQueryMapper mapper;

    @Override
    @PricingReadTransactional
    public Optional<QuotedPrice> quote(
            String variantId,
            String currencyCode,
            int quantity,
            String salesChannelId,
            String regionId
    ) {
        QuoteVariantPriceQuery query = mapper.toQuery(
                variantId,
                currencyCode,
                quantity,
                salesChannelId,
                regionId
        );
        Optional<QuoteVariantPriceResult> result = quoteVariantPriceUseCase.execute(query);
        return result.map(mapper::toQuotedPrice);
    }

    @Override
    @PricingReadTransactional
    public BatchQuoteResult quoteBatch(BatchQuoteRequest request) {
        QuoteVariantPricesQuery query = mapper.toBatchQuery(request);
        QuoteVariantPricesResult result = quoteVariantPricesUseCase.execute(query);
        return mapper.toBatchResult(result);
    }

}
