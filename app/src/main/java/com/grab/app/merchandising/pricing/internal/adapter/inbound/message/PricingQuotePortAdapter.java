package com.grab.app.merchandising.pricing.internal.adapter.inbound.message;

import com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper.QuoteBatchPricesPortMapper;
import com.grab.app.merchandising.pricing.internal.adapter.inbound.message.mapper.QuotePricePortMapper;
import com.grab.app.merchandising.pricing.port.message.PricingQuotePort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.pricing.application.model.read.QuoteVariantPricesQuery;
import com.pricing.application.model.read.QuoteVariantPricesResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PricingQuotePortAdapter implements PricingQuotePort {

    private final QueryBus queryBus;
    private final QuotePricePortMapper quotePriceMapper;
    private final QuoteBatchPricesPortMapper quoteBatchPricesMapper;

    public PricingQuotePortAdapter(
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            QuotePricePortMapper quotePriceMapper,
            QuoteBatchPricesPortMapper quoteBatchPricesMapper) {
        this.queryBus = queryBus;
        this.quotePriceMapper = quotePriceMapper;
        this.quoteBatchPricesMapper = quoteBatchPricesMapper;
    }

    @Override
    public Optional<QuotedPriceResult> quote(
            Id variantId,
            String currencyCode,
            int quantity,
            Id salesChannelId,
            Id regionId) {
        if (variantId == null) {
            return Optional.empty();
        }
        QuoteVariantPricesQuery query = quotePriceMapper.toQuery(
                variantId, currencyCode, quantity, salesChannelId, regionId
        );
        QuoteVariantPricesResult result = queryBus.dispatch(query);
        if (result == null || result.quotes() == null || !result.quotes().containsKey(variantId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(quotePriceMapper.toResponse(result.quotes().get(variantId)));
    }

    @Override
    public BatchQuoteResult quoteBatch(BatchQuoteRequest message) {
        QuoteVariantPricesQuery query = quoteBatchPricesMapper.toQuery(message);
        QuoteVariantPricesResult result = queryBus.dispatch(query);
        return quoteBatchPricesMapper.toResponse(result);
    }
}
